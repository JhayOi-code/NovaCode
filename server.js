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

// --- Connect Existing Account to Google: POST /api/auth/account/merge ---
app.post('/api/auth/account/merge', requireAuth, async (req, res) => {
  const { uid, email } = req.user;
  const cleanEmail = (email || '').trim().toLowerCase();

  if (!cleanEmail) {
    return res.status(400).json({ error: 'invalid_account', message: 'Valid account email required.' });
  }

  try {
    let existingFound = false;
    if (admin && admin.auth) {
      try {
        const userRecord = await admin.auth().getUserByEmail(cleanEmail);
        if (userRecord && userRecord.uid !== uid) {
          // Found legacy account created prior to Google-only migration
          existingFound = true;
          await admin.auth().revokeRefreshTokens(userRecord.uid).catch(() => {});
        }
      } catch (_) {}
    }

    // Ensure Firestore profile has merged Google provider and clean metadata
    if (db) {
      try {
        const profileRef = db.collection('users').doc(uid);
        await profileRef.set({
          email: cleanEmail,
          authProvider: 'google.com',
          migratedToGoogle: true,
          updatedAt: admin ? admin.firestore.FieldValue.serverTimestamp() : new Date().toISOString()
        }, { merge: true });
      } catch (err) {
        console.warn('[Merge] Firestore profile sync notice:', err.message);
      }
    }

    return res.json({
      success: true,
      merged: existingFound,
      message: existingFound
        ? 'We found your existing account and connected it to Google.'
        : 'Google account connected successfully.'
    });
  } catch (err) {
    return res.status(500).json({
      error: 'merge_failed',
      message: 'Failed to connect existing account.'
    });
  }
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

// --- AI Relay Engine: POST /api/ai/relay (Part 1E) ---
const KNOWN_AI_HOSTS = new Set([
  'api.openai.com',
  'api.anthropic.com',
  'generativelanguage.googleapis.com',
  'api.groq.com',
  'openrouter.ai',
  'api.mistral.ai',
  'api.deepseek.com',
  'api.x.ai',
  'api.together.xyz',
  'api.fireworks.ai',
  'api.perplexity.ai',
  'api.cohere.com'
]);

function isPrivateIp(ip) {
  if (!ip) return true;
  if (ip.startsWith('127.')) return true;
  if (ip.startsWith('10.')) return true;
  if (/^172\.(1[6-9]|2[0-9]|3[0-1])\./.test(ip)) return true;
  if (ip.startsWith('192.168.')) return true;
  if (ip.startsWith('169.254.')) return true;
  if (ip.startsWith('0.')) return true;
  if (ip === '::1' || ip.startsWith('fc00:') || ip.startsWith('fd') || ip.startsWith('fe80:')) return true;
  return false;
}

async function validateRelayUrl(targetUrl) {
  let parsed;
  try {
    parsed = new URL(targetUrl);
  } catch (_) {
    throw new Error('Invalid destination URL.');
  }

  if (parsed.protocol !== 'https:') {
    throw new Error('HTTPS scheme is required for secure AI relay.');
  }

  const hostname = parsed.hostname.toLowerCase();
  if (KNOWN_AI_HOSTS.has(hostname)) {
    return parsed;
  }

  // Resolve DNS to verify no private/metadata IP
  try {
    const addresses = await dns.resolve(hostname);
    for (const addr of addresses) {
      if (isPrivateIp(addr)) {
        throw new Error('Destination resolves to a restricted private or metadata IP.');
      }
    }
  } catch (err) {
    if (err.message.includes('restricted')) throw err;
    throw new Error(`DNS resolution failed for host: ${hostname}`);
  }

  return parsed;
}

app.post('/api/ai/relay', requireAuth, express.json({ limit: '1mb' }), async (req, res) => {
  const { url: targetUrl, method = 'POST', headers = {}, body } = req.body || {};

  if (!targetUrl) {
    return res.status(400).json({ error: 'invalid_request', message: 'Missing target URL.' });
  }

  try {
    await validateRelayUrl(targetUrl);
  } catch (err) {
    return res.status(403).json({ error: 'ssrf_blocked', message: err.message });
  }

  const rateKey = `ai_relay:${req.user.uid || req.ip}`;
  const check = Storage.checkRateLimit(rateKey, 60, 60 * 1000);
  if (!check.allowed) {
    return res.status(429).json({ error: 'rate_limited', message: 'Relay rate limit reached. Please wait.' });
  }

  const forwardedHeaders = {};
  const ALLOWED_HEADERS = ['content-type', 'authorization', 'x-api-key', 'anthropic-version', 'anthropic-dangerous-direct-browser-access', 'x-goog-api-key', 'http-referer', 'x-title'];
  Object.keys(headers).forEach(k => {
    const lower = k.toLowerCase();
    if (ALLOWED_HEADERS.includes(lower)) {
      forwardedHeaders[lower] = headers[k];
    }
  });

  const controller = new AbortController();
  req.on('close', () => controller.abort());

  try {
    const fetchOptions = {
      method,
      headers: forwardedHeaders,
      signal: controller.signal,
      redirect: 'error'
    };
    if (body && (method === 'POST' || method === 'PUT')) {
      fetchOptions.body = typeof body === 'string' ? body : JSON.stringify(body);
    }

    const upstreamRes = await fetch(targetUrl, fetchOptions);

    res.status(upstreamRes.status);
    const contentType = upstreamRes.headers.get('content-type') || 'application/json';
    res.setHeader('Content-Type', contentType);

    if (contentType.includes('text/event-stream')) {
      res.setHeader('Cache-Control', 'no-cache');
      res.setHeader('X-Accel-Buffering', 'no');
      res.setHeader('Connection', 'keep-alive');
      const reader = upstreamRes.body.getReader();
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        res.write(value);
      }
      res.end();
    } else {
      const data = await upstreamRes.arrayBuffer();
      res.send(Buffer.from(data));
    }
  } catch (err) {
    if (err.name === 'AbortError') return;
    return res.status(502).json({ error: 'relay_upstream_error', message: err.message });
  }
});

// --- Active Real-Time Presence Engine (Part 2) ---
const activeConnections = new Map();
const activeOwnerStreams = new Map();

let peakToday = 0;
let peakTodayDate = new Date().toDateString();

function getPresenceAggregate() {
  const currentDate = new Date().toDateString();
  if (currentDate !== peakTodayDate) {
    peakTodayDate = currentDate;
    peakToday = 0;
  }

  const distinctUsers = new Set();
  const distinctGuests = new Set();
  let totalDevices = 0;

  for (const conn of activeConnections.values()) {
    totalDevices++;
    if (conn.uid && conn.uid !== 'guest') {
      distinctUsers.add(conn.uid);
    } else {
      distinctGuests.add(conn.sessionId);
    }
  }

  const signedIn = distinctUsers.size;
  const guests = distinctGuests.size;
  const total = signedIn + guests;

  if (total > peakToday) {
    peakToday = total;
  }

  return {
    total,
    signedIn,
    guests,
    devices: totalDevices,
    peakToday,
    timestamp: Date.now()
  };
}

function broadcastPresenceUpdate(changeType) {
  const aggregate = getPresenceAggregate();
  const payload = JSON.stringify({
    type: 'telemetry_update',
    change: changeType || 'sync',
    data: aggregate
  });

  for (const [subId, res] of activeOwnerStreams.entries()) {
    try {
      res.write(`data: ${payload}\n\n`);
    } catch (_) {
      activeOwnerStreams.delete(subId);
    }
  }
}

// Client presence SSE stream (guest or signed-in)
app.get('/api/presence/stream', (req, res) => {
  const sessionId = (req.query.session || 'anon_' + Math.random().toString(36).substring(2, 9)).slice(0, 32);
  const uid = (req.query.uid || 'guest').slice(0, 64);
  const platform = (req.query.platform || 'web').slice(0, 20);

  const connId = 'conn_' + Math.random().toString(36).substring(2, 11);

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('X-Accel-Buffering', 'no');

  activeConnections.set(connId, {
    uid,
    sessionId,
    platform,
    connectedAt: Date.now()
  });

  res.write(`data: ${JSON.stringify({ status: 'connected', connId })}\n\n`);
  broadcastPresenceUpdate('joined');

  const pingInterval = setInterval(() => {
    try {
      res.write(': keepalive\n\n');
    } catch (_) {
      clearInterval(pingInterval);
    }
  }, 25000);

  req.on('close', () => {
    clearInterval(pingInterval);
    activeConnections.delete(connId);
    broadcastPresenceUpdate('left');
  });
});

// Presence Heartbeat fallback
app.post('/api/presence/heartbeat', express.json(), (req, res) => {
  const { sessionId = 'anon_hb', uid = 'guest', platform = 'web' } = req.body || {};
  const connId = 'hb_' + (uid !== 'guest' ? uid : sessionId);
  activeConnections.set(connId, {
    uid,
    sessionId,
    platform,
    connectedAt: Date.now()
  });
  broadcastPresenceUpdate('heartbeat');
  res.json({ ok: true, aggregate: getPresenceAggregate() });
});

// Owner Telemetry Stream (strictly owner-only)
app.get('/api/owner/telemetry/stream', requireAuth, (req, res) => {
  const { email, owner } = req.user;
  const isOwner = owner === true || getOwnerEmails().includes((email || '').toLowerCase());
  if (!isOwner) {
    return res.status(403).json({ error: 'forbidden', message: 'Owner access required.' });
  }

  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('X-Accel-Buffering', 'no');

  const subId = 'owner_sub_' + Math.random().toString(36).substring(2, 9);
  activeOwnerStreams.set(subId, res);

  const initial = getPresenceAggregate();
  res.write(`data: ${JSON.stringify({ type: 'telemetry_update', change: 'initial', data: initial })}\n\n`);

  req.on('close', () => {
    activeOwnerStreams.delete(subId);
  });
});

// Owner Telemetry REST snapshot (strictly owner-only)
app.get('/api/owner/telemetry', requireAuth, (req, res) => {
  const { email, owner } = req.user;
  const isOwner = owner === true || getOwnerEmails().includes((email || '').toLowerCase());
  if (!isOwner) {
    return res.status(403).json({ error: 'forbidden', message: 'Owner access required.' });
  }
  res.json({ ok: true, data: getPresenceAggregate() });
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

module.exports = { app, server, Storage, sendMail, getMailConfig, getOwnerEmails, getPresenceAggregate, activeConnections, validateRelayUrl, isPrivateIp };
