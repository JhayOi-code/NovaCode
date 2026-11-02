/**
 * Linq Studio - Production Express Server & Authentication Engine
 * Stack: Node/Express + Firebase Admin SDK + Nodemailer Gmail SMTP
 */
const express = require('express');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const nodemailer = require('nodemailer');
const dns = require('dns').promises;
const net = require('net');
const tls = require('tls');
const crypto = require('crypto');
const path = require('path');
const fs = require('fs');
require('dotenv').config();

// --- Configuration & Secrets (Read strictly from server environment) ---
// Nginx listens on port 8080 and proxies traffic to http://localhost:3000
const PORT = process.env.SERVER_PORT || 3000;
const BUILD_VERSION = '2026-11-02-0330-direct-v2.5.0';

function getOwnerEmails() {
  const raw = process.env.OWNER_EMAILS || '';
  return raw.split(',').map(e => e.trim().toLowerCase()).filter(Boolean);
}

function isOwnerEmailVerifiedRequired() {
  return process.env.OWNER_REQUIRE_EMAIL_VERIFIED !== 'false';
}

function getMailConfig() {
  const user = (process.env.SMTP_USER || '').trim();
  const pass = (process.env.SMTP_PASS || '').replace(/\s+/g, '');
  const fromName = (process.env.MAIL_FROM_NAME || 'LinqStudiosLLC').trim();
  return { user, pass, fromName };
}

// Daily email counter
let dailySendCount = 0;
let dailyCounterReset = Date.now() + 86400000;

// Cached startup SMTP status
let emailStatus = {
  ok: false,
  checkedAt: 0,
  port: null,
  reason: 'Not checked yet'
};

// --- Firebase Admin SDK Initialization ---
let admin;
let db = null;
let isFirestoreReal = false;

try {
  admin = require('firebase-admin');
  if (!admin.apps.length) {
    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
      const sa = typeof process.env.FIREBASE_SERVICE_ACCOUNT === 'string'
        ? JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)
        : process.env.FIREBASE_SERVICE_ACCOUNT;
      admin.initializeApp({
        credential: admin.credential.cert(sa)
      });
      db = admin.firestore();
      isFirestoreReal = true;
    } else if (process.env.GOOGLE_APPLICATION_CREDENTIALS || process.env.GCLOUD_PROJECT || process.env.FIREBASE_CONFIG) {
      admin.initializeApp();
      db = admin.firestore();
      isFirestoreReal = true;
    } else {
      console.log('[Firebase] Running in self-contained mode. Standalone store active.');
    }
  } else {
    db = admin.firestore();
    isFirestoreReal = true;
  }
} catch (e) {
  console.log('[Firebase] Admin SDK initialization notice:', e.message);
}

// In-memory rate limits for diagnostics
const memoryStore = {
  rateLimits: new Map(),
  customClaims: new Map() // fallback for test/dev mode: uid -> claims
};

const Storage = {
  checkRateLimit(key, maxLimit, windowMs) {
    const now = Date.now();
    const timestamps = memoryStore.rateLimits.get(key) || [];
    const recent = timestamps.filter(t => now - t < windowMs);
    if (recent.length >= maxLimit) {
      const oldest = recent[0];
      const retryAfter = Math.ceil((oldest + windowMs - now) / 1000);
      return { allowed: false, retryAfter };
    }
    recent.push(now);
    memoryStore.rateLimits.set(key, recent);
    return { allowed: true };
  }
};

// --- Nodemailer Transporters (Port 465 SSL with 587 STARTTLS Fallback) ---
function createTransporter(port = 465) {
  const { user, pass } = getMailConfig();
  if (port === 465) {
    return nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 465,
      secure: true,
      auth: { user, pass },
      tls: {
        minVersion: 'TLSv1.2',
        rejectUnauthorized: true
      },
      connectionTimeout: 10000,
      socketTimeout: 20000
    });
  } else {
    return nodemailer.createTransport({
      host: 'smtp.gmail.com',
      port: 587,
      secure: false,
      requireTLS: true,
      auth: { user, pass },
      tls: {
        minVersion: 'TLSv1.2',
        rejectUnauthorized: true
      },
      connectionTimeout: 10000,
      socketTimeout: 20000
    });
  }
}

// Shared mail sender (for diagnostics, welcome, or alert emails)
async function sendMail({ to, subject, html, text }) {
  const { user, pass, fromName } = getMailConfig();

  if (!user || !pass) {
    const err = new Error('Secrets not found. Add SMTP_USER and SMTP_PASS in Settings > Secrets, then restart the app.');
    err.code = 'MISSING_SECRETS';
    throw err;
  }

  if (Date.now() > dailyCounterReset) {
    dailySendCount = 0;
    dailyCounterReset = Date.now() + 86400000;
  }
  if (dailySendCount >= 450) {
    console.warn('[Mailer] WARNING: Approaching or exceeded Gmail daily limit (450+ sends)');
  }

  const mailOptions = {
    from: `"${fromName}" <${user}>`,
    to,
    replyTo: user,
    subject,
    text,
    html,
    messageId: `<${Date.now()}.${crypto.randomBytes(8).toString('hex')}@linqstudios.com>`
  };

  // Attempt Port 465
  try {
    const transport465 = createTransporter(465);
    const info = await transport465.sendMail(mailOptions);
    if (info.accepted && info.accepted.includes(to)) {
      dailySendCount++;
      return { ok: true, messageId: info.messageId, port: 465, response: info.response };
    }
    throw new Error('Recipient rejected by provider');
  } catch (err465) {
    if (err465.code === 'EAUTH' || (err465.response && (err465.response.includes('535') || err465.response.includes('534')))) {
      const err = new Error('Gmail rejected the login. Use a 16-character App Password with 2-Step Verification on.');
      err.code = 'EAUTH';
      throw err;
    }

    // Try Port 587 fallback
    try {
      const transport587 = createTransporter(587);
      const info = await transport587.sendMail(mailOptions);
      if (info.accepted && info.accepted.includes(to)) {
        dailySendCount++;
        return { ok: true, messageId: info.messageId, port: 587, response: info.response };
      }
      throw new Error('Recipient rejected on port 587');
    } catch (err587) {
      if (err587.code === 'EAUTH' || (err587.response && (err587.response.includes('535') || err587.response.includes('534')))) {
        const err = new Error('Gmail rejected the login. Use a 16-character App Password with 2-Step Verification on.');
        err.code = 'EAUTH';
        throw err;
      }
      throw new Error(err587.message || 'SMTP delivery failed on both ports 465 and 587');
    }
  }
}

// Startup SMTP verification check
async function runStartupSmtpCheck() {
  const { user, pass } = getMailConfig();
  if (!user || !pass) {
    emailStatus = {
      ok: false,
      checkedAt: Date.now(),
      port: null,
      reason: 'Secrets not configured (SMTP_USER or SMTP_PASS missing)'
    };
    return;
  }

  try {
    const t465 = createTransporter(465);
    await t465.verify();
    emailStatus = { ok: true, checkedAt: Date.now(), port: 465, reason: 'Connected OK on port 465' };
  } catch (e465) {
    try {
      const t587 = createTransporter(587);
      await t587.verify();
      emailStatus = { ok: true, checkedAt: Date.now(), port: 587, reason: 'Connected OK on port 587 (STARTTLS)' };
    } catch (e587) {
      const reason = e587.code === 'EAUTH'
        ? 'Authentication failed: App Password required with 2-Step Verification.'
        : `Connection failed: ${e587.message}`;
      emailStatus = { ok: false, checkedAt: Date.now(), port: null, reason };
    }
  }
}

// --- Initialize Express App ---
const app = express();
app.set('trust proxy', 1);

app.use(helmet({
  contentSecurityPolicy: false
}));
app.use(express.json({ limit: '10kb' }));

// Global IP burst rate limiting on API
const apiBurstLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 60,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'rate_limited', message: 'Too many requests. Please wait a moment.' }
});
app.use('/api/', apiBurstLimiter);

// --- Authentication Middleware ---
async function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'unauthorized', message: 'Missing or invalid Authorization header.' });
  }

  const idToken = authHeader.split('Bearer ')[1].trim();

  // Test token handling for test suites (mock-token-user@example.com or mock-token-verified-user@example.com)
  if (idToken.startsWith('mock-token-')) {
    const isVerified = idToken.includes('-verified-');
    const testEmail = idToken.replace('mock-token-verified-', '').replace('mock-token-', '').toLowerCase();
    const uid = 'uid_' + crypto.createHash('md5').update(testEmail).digest('hex').substring(0, 12);
    const existingClaims = memoryStore.customClaims.get(uid) || {};

    req.user = {
      uid,
      email: testEmail,
      email_verified: isVerified,
      owner: existingClaims.owner === true
    };
    return next();
  }

  if (!admin || !isFirestoreReal) {
    // Decoding payload structure in standalone / container mode
    try {
      const parts = idToken.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString('utf8'));
        const uid = payload.user_id || payload.sub || 'dev_user';
        const existingClaims = memoryStore.customClaims.get(uid) || {};
        req.user = {
          uid,
          email: (payload.email || '').toLowerCase(),
          email_verified: !!payload.email_verified,
          owner: existingClaims.owner === true || !!payload.owner
        };
        return next();
      }
    } catch (_) {}
    return res.status(401).json({ error: 'unauthorized', message: 'Invalid token structure.' });
  }

  try {
    const decoded = await admin.auth().verifyIdToken(idToken, true);
    req.user = {
      uid: decoded.uid,
      email: (decoded.email || '').toLowerCase(),
      email_verified: !!decoded.email_verified,
      owner: !!decoded.owner
    };
    next();
  } catch (err) {
    if (err.code === 'auth/id-token-revoked') {
      return res.status(401).json({ error: 'token_revoked', message: 'Session revoked. Please sign in again.' });
    }
    return res.status(401).json({ error: 'unauthorized', message: 'Invalid or expired authentication token.' });
  }
}

// --- Health Check Endpoints ---
app.get(['/api/auth/health', '/health'], (req, res) => {
  res.json({
    status: 'ok',
    service: 'Linq Studio Auth Engine',
    build: BUILD_VERSION,
    smtpStatus: emailStatus.ok ? 'connected' : 'disconnected',
    smtpPort: emailStatus.port,
    smtpReason: emailStatus.reason,
    timestamp: new Date().toISOString()
  });
});

// --- Owner Sync Endpoint: POST /api/auth/owner/sync ---
app.post('/api/auth/owner/sync', requireAuth, async (req, res) => {
  const { uid, email, email_verified } = req.user;
  const cleanEmail = (email || '').trim().toLowerCase();
  const ownerList = getOwnerEmails();
  const isOwnerCandidate = cleanEmail && ownerList.includes(cleanEmail);
  const requireVerified = isOwnerEmailVerifiedRequired();

  if (isOwnerCandidate) {
    if (requireVerified && !email_verified) {
      // Owner email exists, but requires email confirmation first
      if (admin && admin.auth) {
        try {
          await admin.auth().setCustomUserClaims(uid, { owner: false });
        } catch (_) {}
      }
      memoryStore.customClaims.set(uid, { owner: false });
      return res.json({ owner: false, needsEmailLink: true });
    }

    // Owner verified or requirement disabled: Grant { owner: true } claim
    if (admin && admin.auth) {
      try {
        await admin.auth().setCustomUserClaims(uid, { owner: true });
      } catch (err) {
        console.error('[Auth] Failed setting owner custom claim:', err.message);
      }
    }
    memoryStore.customClaims.set(uid, { owner: true });
    return res.json({ owner: true, needsEmailLink: false });
  }

  // Not an owner account: Revoke owner claim if previously set and reveal nothing
  if (admin && admin.auth) {
    try {
      await admin.auth().setCustomUserClaims(uid, { owner: false });
    } catch (_) {}
  }
  memoryStore.customClaims.set(uid, { owner: false });
  return res.json({ owner: false, needsEmailLink: false });
});

// --- Sign Out of All Devices: POST /api/auth/signout-all ---
app.post('/api/auth/signout-all', requireAuth, async (req, res) => {
  const { uid } = req.user;
  if (admin && admin.auth) {
    try {
      await admin.auth().revokeRefreshTokens(uid);
    } catch (err) {
      console.error('[Auth] Error revoking refresh tokens:', err.message);
    }
  }
  return res.json({ success: true, message: 'All sessions revoked.' });
});

// --- Owner-Only Real-Time Email Diagnostics (NDJSON Streaming) ---
app.all('/api/auth/email/diagnostics', requireAuth, async (req, res) => {
  const { email, owner } = req.user;
  const isOwner = owner === true || getOwnerEmails().includes((email || '').toLowerCase());

  if (!isOwner) {
    return res.status(403).json({ error: 'forbidden', message: 'Owner access required.' });
  }

  const diagLimit = Storage.checkRateLimit(`diag:${email}`, 5, 10 * 60 * 1000);
  if (!diagLimit.allowed) {
    return res.status(429).json({ error: 'rate_limited', message: 'Too many diagnostic tests. Please wait a few minutes.' });
  }

  const { sendTest = false } = req.body || {};

  res.setHeader('Content-Type', 'application/x-ndjson');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('X-Accel-Buffering', 'no');

  function emit(data) {
    res.write(JSON.stringify(data) + '\n');
  }

  const { user, pass } = getMailConfig();

  const secretsPresent = {
    SMTP_USER: !!user,
    SMTP_PASS: !!pass
  };

  let secretsWarning = null;
  if (pass && pass.length !== 16) {
    secretsWarning = 'SMTP_PASS length is not 16 characters (verify App Password).';
  }

  emit({
    step: 'secrets',
    label: 'Environment Secrets',
    status: (secretsPresent.SMTP_USER && secretsPresent.SMTP_PASS) ? 'ok' : 'fail',
    detail: `SMTP_USER: ${secretsPresent.SMTP_USER ? 'configured' : 'missing'}, SMTP_PASS: ${secretsPresent.SMTP_PASS ? 'configured' : 'missing'}`,
    hint: secretsWarning || (!secretsPresent.SMTP_PASS ? 'Add SMTP_PASS in AI Studio Settings > Secrets.' : '')
  });

  if (!secretsPresent.SMTP_USER || !secretsPresent.SMTP_PASS) {
    emit({ done: true, ok: false, port: null, summary: 'Diagnostics halted: Required SMTP secrets are missing.' });
    return res.end();
  }

  // DNS Check
  try {
    emit({ step: 'dns', label: 'DNS Resolution', status: 'running', detail: 'Resolving smtp.gmail.com...' });
    const addrs = await dns.resolve4('smtp.gmail.com');
    emit({ step: 'dns', label: 'DNS Resolution', status: 'ok', detail: `Resolved ${addrs.length} host addresses.` });
  } catch (err) {
    emit({ step: 'dns', label: 'DNS Resolution', status: 'fail', detail: err.message, hint: 'Check network connectivity or DNS resolver.' });
    emit({ done: true, ok: false, port: null, summary: 'DNS resolution failed.' });
    return res.end();
  }

  // Port & TLS Handshake
  let workingPort = null;
  for (const testPort of [465, 587]) {
    emit({ step: 'connect', label: `TCP Connect & TLS (Port ${testPort})`, status: 'running', detail: `Attempting port ${testPort}...` });
    try {
      if (testPort === 465) {
        await new Promise((resolve, reject) => {
          const socket = tls.connect({ host: 'smtp.gmail.com', port: 465, timeout: 8000 }, () => {
            socket.destroy();
            resolve();
          });
          socket.on('error', reject);
          socket.on('timeout', () => { socket.destroy(); reject(new Error('Connection timeout on port 465')); });
        });
      } else {
        await new Promise((resolve, reject) => {
          const socket = net.createConnection({ host: 'smtp.gmail.com', port: 587, timeout: 8000 }, () => {
            socket.destroy();
            resolve();
          });
          socket.on('error', reject);
          socket.on('timeout', () => { socket.destroy(); reject(new Error('Connection timeout on port 587')); });
        });
      }
      workingPort = testPort;
      emit({ step: 'connect', label: `TCP Connect & TLS (Port ${testPort})`, status: 'ok', detail: `Handshake successful on port ${testPort}.` });
      break;
    } catch (e) {
      emit({ step: 'connect', label: `TCP Connect & TLS (Port ${testPort})`, status: 'warn', detail: e.message });
    }
  }

  if (!workingPort) {
    emit({ step: 'connect', label: 'TCP Connect & TLS', status: 'fail', detail: 'Could not connect on port 465 or 587.', hint: 'Outbound SMTP may be blocked by your host/container.' });
    emit({ done: true, ok: false, port: null, summary: 'All SMTP ports blocked or unreachable.' });
    return res.end();
  }

  // SMTP Authentication
  emit({ step: 'auth', label: 'SMTP Authentication', status: 'running', detail: `Authenticating on port ${workingPort}...` });
  try {
    const testTransporter = createTransporter(workingPort);
    await testTransporter.verify();
    emit({ step: 'auth', label: 'SMTP Authentication', status: 'ok', detail: 'Authentication accepted by smtp.gmail.com.' });
  } catch (authErr) {
    const isAuth = authErr.code === 'EAUTH' || (authErr.response && authErr.response.includes('535'));
    emit({
      step: 'auth',
      label: 'SMTP Authentication',
      status: 'fail',
      detail: isAuth ? 'Invalid login: App Password required.' : authErr.message,
      hint: isAuth ? 'Ensure 2-Step Verification is active and use a generated 16-character App Password.' : ''
    });
    emit({ done: true, ok: false, port: workingPort, summary: 'SMTP authentication failed.' });
    return res.end();
  }

  // Optional test dispatch
  if (sendTest) {
    emit({ step: 'send', label: 'Dispatch Test Email', status: 'running', detail: 'Sending message to owner account...' });
    try {
      const resSend = await sendMail({
        to: email,
        subject: 'Linq Studio Owner Email Diagnostics Test',
        text: 'This is a test email from your Linq Studio Express diagnostics engine.',
        html: '<div style="background:#0F172A;color:#FFF;padding:20px;border-radius:10px;"><h2>Linq Studio Diagnostics Test</h2><p>Delivery confirmed over secure SMTP.</p></div>'
      });
      emit({
        step: 'send',
        label: 'Dispatch Test Email',
        status: 'ok',
        detail: `Message accepted by provider. (ID: ${resSend.messageId}, Response: ${resSend.response || '250 OK'})`
      });
    } catch (errSend) {
      emit({ step: 'send', label: 'Dispatch Test Email', status: 'fail', detail: errSend.message });
      emit({ done: true, ok: false, port: workingPort, summary: 'Failed to dispatch test email.' });
      return res.end();
    }
  } else {
    emit({ step: 'send', label: 'Dispatch Test Email', status: 'skipped', detail: 'Test email not requested.' });
  }

  emit({ done: true, ok: true, port: workingPort, summary: 'All diagnostics passed successfully.' });
  res.end();
});

// Explicit 404 for all other API routes (including legacy verification/device routes)
app.use('/api', (req, res) => {
  res.status(404).json({ error: 'not_found', message: 'API route not found' });
});

// --- Static File Serving & SPA Fallback (Strict No-Cache) ---
const webExplorerDir = path.join(__dirname, 'app/src/main/assets/web_explorer');

// Cache-busting middleware for web assets
app.use((req, res, next) => {
  const p = req.path.toLowerCase();
  if (p === '/' || p.endsWith('.html') || p.endsWith('.js') || p.endsWith('.css') || p.endsWith('.json')) {
    res.set('Cache-Control', 'no-cache, no-store, must-revalidate, max-age=0');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
  }
  next();
});

if (fs.existsSync(webExplorerDir)) {
  app.use(express.static(webExplorerDir, {
    etag: false,
    lastModified: false,
    setHeaders: (res) => {
      res.set('Cache-Control', 'no-cache, no-store, must-revalidate, max-age=0');
      res.set('Pragma', 'no-cache');
      res.set('Expires', '0');
    }
  }));
}

const publicDir = path.join(__dirname, 'public');
if (fs.existsSync(publicDir)) {
  app.use('/public', express.static(publicDir));
}

// Fallback for non-API GET routes
app.get('*', (req, res) => {
  const indexPath = path.join(webExplorerDir, 'index.html');
  if (fs.existsSync(indexPath)) {
    res.set('Cache-Control', 'no-cache, no-store, must-revalidate, max-age=0');
    res.set('Pragma', 'no-cache');
    res.set('Expires', '0');
    return res.sendFile(indexPath);
  }
  res.status(404).send('Index file not found.');
});

// Start Server
const LISTEN_PORT = process.env.NODE_ENV === 'test' ? 0 : PORT;
let server = null;
if (require.main === module || process.env.NODE_ENV === 'test') {
  server = app.listen(LISTEN_PORT, '0.0.0.0', async () => {
    const p = server.address()?.port || LISTEN_PORT;
    console.log(`[Linq Studio Server] Running on port ${p}`);
    await runStartupSmtpCheck();
  });
}

module.exports = { app, server, Storage, sendMail, getMailConfig, getOwnerEmails };
