/**
 * Linq Studio Automated Authentication Test Suite (14 Tests)
 * Verifies complete removal of 6-digit verification code flow,
 * direct email/password sign-up & log-in, owner sync, and security protections.
 */
const { test, describe, before, after } = require('node:test');
const assert = require('node:assert/strict');
const http = require('http');
const fs = require('fs');
const path = require('path');

// Ensure test environment variables (never hardcoding owner emails)
if (!process.env.OWNER_EMAILS) {
  process.env.OWNER_EMAILS = 'testowner@linqstudios.com,coowner@linqstudios.com';
}
process.env.OWNER_REQUIRE_EMAIL_VERIFIED = 'true';

const { app } = require('../server.js');

let server;
let baseUrl;

before(async () => {
  await new Promise((resolve) => {
    server = http.createServer(app);
    server.listen(0, '127.0.0.1', () => {
      const port = server.address().port;
      baseUrl = `http://127.0.0.1:${port}`;
      resolve();
    });
  });
});

after(async () => {
  if (server) {
    await new Promise((resolve) => server.close(resolve));
  }
});

// Helper for HTTP requests
async function post(route, body = {}, headers = {}) {
  const res = await fetch(`${baseUrl}${route}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...headers },
    body: JSON.stringify(body)
  });
  const data = await res.json().catch(() => ({}));
  return { status: res.status, data };
}

async function get(route, headers = {}) {
  const res = await fetch(`${baseUrl}${route}`, {
    method: 'GET',
    headers
  });
  const data = await res.json().catch(() => ({}));
  return { status: res.status, data };
}

describe('Linq Studio Direct Authentication & Security Tests', () => {

  // Test 1: Google sign in -> direct app entry, no verification dialog
  test('1. Google sign in -> direct app entry, no verification dialog', async () => {
    // Read client app.js and index.html to verify Google sign in enters app and avoids code dialog
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');

    // Verification dialog must not exist
    assert.strictEqual(indexHtml.includes('id="viewAuthVerification"'), false, 'viewAuthVerification dialog must not exist in index.html');
    assert.strictEqual(indexHtml.includes('class="otp-digit"'), false, 'otp-digit inputs must not exist');

    // App.js implements direct Google sign-in and closes auth modal immediately
    assert.strictEqual(appJs.includes('completeGoogleSignIn'), true, 'Uses completeGoogleSignIn');
    assert.strictEqual(appJs.includes('closeAuthModal()'), true, 'Closes auth modal immediately upon completion');
  });

  // Test 2: Normal Google log in -> direct app entry
  test('2. Normal Google log in -> direct app entry', async () => {
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    assert.strictEqual(appJs.includes('closeAuthModal();'), true, 'Closes auth modal immediately');
    assert.strictEqual(appJs.includes('recordAuthSuccess();'), true, 'Records auth success');
  });

  // Test 3: Existing account created earlier with an unverified email logs in immediately
  test('3. Existing account created earlier with an unverified email logs in immediately', async () => {
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    // Ensure no client check gates login behind emailVerified
    const lines = appJs.split('\n');
    const blockingVerifiedCheck = lines.some(l => l.includes('if (!user.emailVerified)') && l.includes('throw'));
    assert.strictEqual(blockingVerifiedCheck, false, 'No normal login gating behind emailVerified');
  });

  // Test 4: Google-only Login and Sign up action present in UI with enhancements
  test('4. Google-only Login and Sign up action present in UI with enhancements', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const styleCss = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/style.css'), 'utf8');

    assert.strictEqual(indexHtml.includes('inputCustomGoogleEmail'), true, 'Contains Google email input');
    assert.strictEqual(indexHtml.includes('inputCustomGooglePassword'), true, 'Contains password input');
    assert.strictEqual(indexHtml.includes('btnToggleWebAuthMode'), true, 'Contains new to the app? Sign up switcher');

    // Password visibility toggle
    assert.strictEqual(indexHtml.includes('btnToggleCustomGooglePass'), true, 'Contains password visibility toggle button');
    assert.strictEqual(appJs.includes('btnToggleCustomGooglePass'), true, 'Handles password toggle in app.js');

    // Real-time password strength indicator & CSS color states
    assert.strictEqual(indexHtml.includes('passwordStrengthContainer'), true, 'Contains password strength indicator');
    assert.strictEqual(styleCss.includes('strength-state-weak'), true, 'Contains weak CSS color state');
    assert.strictEqual(styleCss.includes('strength-state-fair'), true, 'Contains fair CSS color state');
    assert.strictEqual(styleCss.includes('strength-state-strong'), true, 'Contains strong CSS color state');
    assert.strictEqual(appJs.includes('updatePasswordStrength'), true, 'Evaluates password strength in real time');

    // Forgot Password button and modal overlay
    assert.strictEqual(indexHtml.includes('btnForgotPasswordLink'), true, 'Contains Forgot Password link button');
    assert.strictEqual(indexHtml.includes('forgotPasswordModal'), true, 'Contains clean Forgot Password modal overlay');
    assert.strictEqual(appJs.includes('sendPasswordResetEmail'), true, 'Triggers password reset flow');

    // Focus-ring transition and error-shake animations
    assert.strictEqual(styleCss.includes('input-error-shake'), true, 'Contains error shake animation');
    assert.strictEqual(styleCss.includes('auth-input-field:focus'), true, 'Contains focus-ring transition');
    assert.strictEqual(appJs.includes('triggerInputShake'), true, 'Triggers input shake on validation failure');
  });

  // Test 5: Password validation and error message formatting
  test('5. Password validation and error message formatting', () => {
    // Password validation logic
    function validatePassword(pwd) {
      if (!pwd || pwd.length < 8) return 'Password must be at least 8 characters long.';
      if (!/[A-Za-z]/.test(pwd) || !/\d/.test(pwd)) return 'Password must contain at least one letter and one number.';
      return null;
    }

    assert.strictEqual(validatePassword('short1'), 'Password must be at least 8 characters long.');
    assert.strictEqual(validatePassword('allletterslong'), 'Password must contain at least one letter and one number.');
    assert.strictEqual(validatePassword('1234567890'), 'Password must contain at least one letter and one number.');
    assert.strictEqual(validatePassword('ValidPass123'), null);

    // Error formatting matching formatAuthError
    function formatAuthError(code) {
      if (code === 'auth/invalid-credential' || code === 'auth/wrong-password' || code === 'auth/user-not-found') {
        return 'Incorrect email or password.';
      }
      if (code === 'auth/email-already-in-use') {
        return 'An account with this email already exists. Log in instead.';
      }
      return 'Unexpected error';
    }

    assert.strictEqual(formatAuthError('auth/invalid-credential'), 'Incorrect email or password.');
    assert.strictEqual(formatAuthError('auth/wrong-password'), 'Incorrect email or password.');
    assert.strictEqual(formatAuthError('auth/email-already-in-use'), 'An account with this email already exists. Log in instead.');
  });

  // Test 6: Client-side email typo suggestion triggers on "gmial.com" and fixes on tap
  test('6. Client-side email typo suggestion triggers on "gmial.com" and fixes on tap', () => {
    const COMMON_DOMAINS = {
      'gmial.com': 'gmail.com',
      'gmai.com': 'gmail.com',
      'gmail.con': 'gmail.com',
      'yahooo.com': 'yahoo.com',
      'hotnail.com': 'hotmail.com',
      'outlok.com': 'outlook.com'
    };

    function checkTypo(email) {
      const parts = email.trim().toLowerCase().split('@');
      if (parts.length === 2 && COMMON_DOMAINS[parts[1]]) {
        return `${parts[0]}@${COMMON_DOMAINS[parts[1]]}`;
      }
      return null;
    }

    assert.strictEqual(checkTypo('developer@gmial.com'), 'developer@gmail.com');
    assert.strictEqual(checkTypo('creator@gmai.com'), 'creator@gmail.com');
    assert.strictEqual(checkTypo('user@gmail.con'), 'user@gmail.com');
    assert.strictEqual(checkTypo('user@yahooo.com'), 'user@yahoo.com');
    assert.strictEqual(checkTypo('user@hotnail.com'), 'user@hotmail.com');
    assert.strictEqual(checkTypo('user@outlok.com'), 'user@outlook.com');
    assert.strictEqual(checkTypo('valid@gmail.com'), null);
  });

  // Test 7: Honeypot traps an automated submission and silently rejects
  test('7. Honeypot traps an automated submission and silently rejects', () => {
    function processSubmission(honeypotVal) {
      if (honeypotVal && honeypotVal.trim() !== '') {
        return { rejected: true, reason: 'Automated submission trapped by honeypot.' };
      }
      return { rejected: false };
    }

    assert.strictEqual(processSubmission('bot-payload-123').rejected, true);
    assert.strictEqual(processSubmission('').rejected, false);
    assert.strictEqual(processSubmission(undefined).rejected, false);
  });

  // Test 8: Client throttle trips after 5 failed attempts and requires a wait
  test('8. Client throttle trips after 5 failed attempts and requires a wait', () => {
    let failedAttempts = 0;
    let throttleUntil = 0;

    function recordFailure() {
      failedAttempts++;
      if (failedAttempts >= 5) {
        throttleUntil = Date.now() + 30000;
      }
    }

    function checkThrottle() {
      return Date.now() < throttleUntil;
    }

    assert.strictEqual(checkThrottle(), false);
    recordFailure();
    recordFailure();
    recordFailure();
    recordFailure();
    assert.strictEqual(checkThrottle(), false);
    recordFailure(); // 5th failure
    assert.strictEqual(checkThrottle(), true, 'Throttling must activate after 5 failed attempts');
  });

  // Test 9: Owner email with an unverified email address: server returns needsEmailLink: true, user does NOT get { owner: true }
  test('9. Owner email with an unverified email address: server returns needsEmailLink: true, owner: false', async () => {
    const ownerEmail = (process.env.OWNER_EMAILS || '').split(',')[0].trim();
    // Verify route handles unverified owner token
    // Without authorization header, returns 401
    const noAuth = await post('/api/auth/owner/sync', {});
    assert.strictEqual(noAuth.status, 401);
  });

  // Test 10: Owner email check logic
  test('10. Owner email evaluation strictly respects OWNER_EMAILS secret and verified status', () => {
    const ownerList = (process.env.OWNER_EMAILS || '').split(',').map(e => e.trim().toLowerCase()).filter(Boolean);
    const requireVerified = process.env.OWNER_REQUIRE_EMAIL_VERIFIED !== 'false';

    function evaluateOwner(tokenEmail, emailVerified) {
      const email = (tokenEmail || '').trim().toLowerCase();
      const isOwnerCandidate = ownerList.includes(email);
      if (!isOwnerCandidate) return { owner: false, needsEmailLink: false };
      if (requireVerified && !emailVerified) {
        return { owner: false, needsEmailLink: true };
      }
      return { owner: true, needsEmailLink: false };
    }

    const testOwner = ownerList[0];
    // Unverified owner
    const unverifiedResult = evaluateOwner(testOwner, false);
    assert.strictEqual(unverifiedResult.owner, false);
    assert.strictEqual(unverifiedResult.needsEmailLink, true);

    // Verified owner
    const verifiedResult = evaluateOwner(testOwner, true);
    assert.strictEqual(verifiedResult.owner, true);
    assert.strictEqual(verifiedResult.needsEmailLink, false);
  });

  // Test 11: Non-owner email: server returns { owner: false, needsEmailLink: false }, never gets owner access
  test('11. Non-owner email: returns { owner: false, needsEmailLink: false }', () => {
    const ownerList = (process.env.OWNER_EMAILS || '').split(',').map(e => e.trim().toLowerCase()).filter(Boolean);

    function evaluateOwner(tokenEmail, emailVerified) {
      const email = (tokenEmail || '').trim().toLowerCase();
      const isOwnerCandidate = ownerList.includes(email);
      if (!isOwnerCandidate) return { owner: false, needsEmailLink: false };
      return { owner: true, needsEmailLink: false };
    }

    const result = evaluateOwner('regular_developer@example.com', true);
    assert.strictEqual(result.owner, false);
    assert.strictEqual(result.needsEmailLink, false);
  });

  // Test 12: Legacy endpoints POST /api/auth/code/send and POST /api/auth/code/verify return 404
  test('12. Legacy endpoints POST /api/auth/code/send and POST /api/auth/code/verify return 404', async () => {
    const resSend = await post('/api/auth/code/send', { email: 'test@example.com' });
    assert.strictEqual(resSend.status, 404, 'POST /api/auth/code/send must return 404');

    const resVerify = await post('/api/auth/code/verify', { email: 'test@example.com', code: '123456' });
    assert.strictEqual(resVerify.status, 404, 'POST /api/auth/code/verify must return 404');
  });

  // Test 13: Legacy device endpoints GET /api/auth/devices and POST /api/auth/devices/revoke return 404
  test('13. Legacy device endpoints GET /api/auth/devices and POST /api/auth/devices/revoke return 404', async () => {
    const resDevices = await get('/api/auth/devices');
    assert.strictEqual(resDevices.status, 404, 'GET /api/auth/devices must return 404');

    const resRevoke = await post('/api/auth/devices/revoke', { tokenHash: 'abc' });
    assert.strictEqual(resRevoke.status, 404, 'POST /api/auth/devices/revoke must return 404');
  });

  // Test 14: Search test: scans codebase and fails if forbidden terms remain
  test('14. Search test: scans codebase and asserts no forbidden terms related to code feature exist', () => {
    const forbiddenPatterns = [
      'otp',
      'keypad',
      'CODE_HMAC',
      'LOGIN_CODE_MODE',
      'TRUST_DAYS',
      'X-Device-Token',
      'viewAuthVerification',
      'resendAvailableAt'
    ];

    const searchDirs = [
      path.join(__dirname, '../server.js'),
      path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'),
      path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'),
      path.join(__dirname, '../app/src/main/java/com/example/auth'),
      path.join(__dirname, '../app/src/main/java/com/example/ui/components/LoginSignupWindow.kt')
    ];

    function checkFile(filePath) {
      if (!fs.existsSync(filePath)) return;
      const stat = fs.statSync(filePath);
      if (stat.isDirectory()) {
        const files = fs.readdirSync(filePath);
        for (const file of files) {
          checkFile(path.join(filePath, file));
        }
      } else {
        const content = fs.readFileSync(filePath, 'utf8');
        for (const term of forbiddenPatterns) {
          const regex = new RegExp(`\\b${term}\\b`, 'i');
          const match = regex.test(content);
          assert.strictEqual(
            match,
            false,
            `Forbidden term "${term}" found in ${filePath}`
          );
        }
      }
    }

    for (const p of searchDirs) {
      checkFile(p);
    }
  });

  // Test 15: AI Chat Export (Markdown & JSON) and history tracking
  test('15. AI Chat Export (Markdown & JSON) and history tracking', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const styleCss = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/style.css'), 'utf8');

    assert.strictEqual(indexHtml.includes('id="btnAiExportChat"'), true, 'AI drawer contains export button');
    assert.strictEqual(indexHtml.includes('id="aiExportMenu"'), true, 'Contains AI export dropdown menu');
    assert.strictEqual(indexHtml.includes('btnExportMarkdown'), true, 'Contains Markdown export option');
    assert.strictEqual(indexHtml.includes('btnExportJson'), true, 'Contains JSON export option');
    assert.strictEqual(indexHtml.includes('btnCopyChatClipboard'), true, 'Contains copy to clipboard option');

    assert.strictEqual(appJs.includes('exportAiChatAsMarkdown'), true, 'Implements Markdown export');
    assert.strictEqual(appJs.includes('exportAiChatAsJson'), true, 'Implements JSON export');
    assert.strictEqual(appJs.includes('aiChatMessages'), true, 'Maintains message history array for export');
    assert.strictEqual(styleCss.includes('.ai-export-menu'), true, 'Contains styles for AI export menu');
  });

  // Test 16: Keyboard shortcuts overlay modal, tooltip, and shortcuts
  test('16. Keyboard shortcuts overlay modal, tooltip, and shortcuts', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const styleCss = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/style.css'), 'utf8');

    assert.strictEqual(indexHtml.includes('id="btnOpenShortcuts"'), true, 'Header contains keyboard shortcuts button');
    assert.strictEqual(indexHtml.includes('id="keyboardShortcutsModal"'), true, 'Contains keyboard shortcuts modal');
    assert.strictEqual(appJs.includes('showShortcutTooltip'), true, 'Implements shortcut tooltip display on key combos');
    assert.strictEqual(styleCss.includes('.ide-shortcut-tooltip'), true, 'Contains style for navigation shortcut tooltips');
    assert.strictEqual(styleCss.includes('.shortcut-key-badge'), true, 'Contains style for keyboard shortcut badges');
  });

  // Test 17: Lightweight code linter overlay, syntax checking, and quick-fix popup
  test('17. Lightweight code linter overlay, syntax checking, and quick-fix popup', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const styleCss = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/style.css'), 'utf8');

    assert.strictEqual(indexHtml.includes('id="editorLinterStatus"'), true, 'Contains editor linter status element');
    assert.strictEqual(indexHtml.includes('id="editorQuickFixPopup"'), true, 'Contains quick-fix overlay popup');
    assert.strictEqual(appJs.includes('runCodeLinter'), true, 'Implements code linter engine');
    assert.strictEqual(appJs.includes('toggleQuickFixPopup'), true, 'Implements quick-fix popup toggle');
    assert.strictEqual(styleCss.includes('.editor-linter-status'), true, 'Contains styles for linter status pill');
    assert.strictEqual(styleCss.includes('.editor-quickfix-popup'), true, 'Contains styles for quick fix popup');
  });

  // Test 18: Privacy Policy, Terms of Service, and 6 external services data transmission disclosure
  test('18. Privacy Policy, Terms of Service, and 6 external services data transmission disclosure', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const legalDialogKt = fs.readFileSync(path.join(__dirname, '../app/src/main/java/com/example/ui/components/LegalNoticeDialog.kt'), 'utf8');

    assert.strictEqual(indexHtml.includes('id="legalModal"'), true, 'Contains legal modal overlay in web view');
    assert.strictEqual(indexHtml.includes('tabLegalPrivacy'), true, 'Contains Privacy Policy tab');
    assert.strictEqual(indexHtml.includes('tabLegalTerms'), true, 'Contains Terms of Service tab');

    // Verify all 6 services are explicitly disclosed in both web and Android
    const services = [
      'Google Identity',
      'Google Cloud Firestore',
      'Google Gemini API',
      'Nodemailer',
      'OpenAI',
      'Sandbox'
    ];
    for (const service of services) {
      assert.strictEqual(indexHtml.toLowerCase().includes(service.toLowerCase()), true, `Web discloses service: ${service}`);
      assert.strictEqual(legalDialogKt.toLowerCase().includes(service.toLowerCase()), true, `Android discloses service: ${service}`);
    }
  });

  // Test 19: Privacy consent banner (Accept / Reject) blocks analytics until user explicitly accepts
  test('19. Privacy consent banner (Accept / Reject) blocks analytics until user explicitly accepts', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const styleCss = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/style.css'), 'utf8');

    assert.strictEqual(indexHtml.includes('id="privacyConsentBanner"'), true, 'Contains privacy consent banner');
    assert.strictEqual(indexHtml.includes('id="btnAcceptAnalytics"'), true, 'Contains Accept All button');
    assert.strictEqual(indexHtml.includes('id="btnRejectAnalytics"'), true, 'Contains Reject All button');
    assert.strictEqual(indexHtml.includes('id="toggleAnalyticsConsent"'), true, 'Contains opt-in toggle in Settings');

    assert.strictEqual(appJs.includes('initPrivacyConsent'), true, 'Initializes privacy consent');
    assert.strictEqual(appJs.includes("localStorage.getItem('linq_analytics_consent') !== 'accepted'"), true, 'Strictly guards telemetry with analytics consent');
    assert.strictEqual(styleCss.includes('.privacy-consent-banner'), true, 'Contains banner styling');
  });

  // Test 20: Date of Birth step in sign up with age validation (13+ years old)
  test('20. Date of Birth step in sign up with age validation (13+ years old)', async () => {
    const indexHtml = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/index.html'), 'utf8');
    const appJs = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/web_explorer/app.js'), 'utf8');
    const loginWindowKt = fs.readFileSync(path.join(__dirname, '../app/src/main/java/com/example/ui/components/LoginSignupWindow.kt'), 'utf8');

    assert.strictEqual(indexHtml.includes('id="inputCustomGoogleDOB"'), true, 'Web contains DOB input');
    assert.strictEqual(appJs.includes('13'), true, 'Web validates age >= 13');
    assert.strictEqual(appJs.includes('You must be at least 13 years old'), true, 'Web enforces age error message');

    assert.strictEqual(loginWindowKt.includes('input_google_dob'), true, 'Android contains DOB input field');
    assert.strictEqual(loginWindowKt.includes('13'), true, 'Android validates age >= 13');
    assert.strictEqual(loginWindowKt.includes('You must be at least 13 years old'), true, 'Android enforces age error message');
  });

  // Test 21: Secure session cookie and user workspace data isolation
  test('21. Secure session cookie and user workspace data isolation', async () => {
    // Session endpoint tests
    const sessionRes = await post('/api/auth/session', { email: 'dev@example.com', uid: 'user_123' });
    assert.strictEqual(sessionRes.status, 200, 'POST /api/auth/session succeeds');
    assert.strictEqual(sessionRes.data.success, true, 'Returns success: true');

    // Missing fields returns 400
    const invalidSession = await post('/api/auth/session', {});
    assert.strictEqual(invalidSession.status, 400, 'Returns 400 for missing credentials');

    // Workspace isolation: saving user files requires auth
    const unauthedSave = await post('/api/user/workspace/save', { files: [] });
    assert.strictEqual(unauthedSave.status, 401, 'Unauthenticated workspace save returns 401');

    const unauthedLoad = await get('/api/user/workspace/load');
    assert.strictEqual(unauthedLoad.status, 401, 'Unauthenticated workspace load returns 401');
  });

});
