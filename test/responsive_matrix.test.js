const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('fs');
const path = require('path');

const indexHtmlPath = path.join(__dirname, '../app/src/main/assets/web_explorer/index.html');
const styleCssPath = path.join(__dirname, '../app/src/main/assets/web_explorer/style.css');
const appJsPath = path.join(__dirname, '../app/src/main/assets/web_explorer/app.js');
const aiProvidersJsPath = path.join(__dirname, '../app/src/main/assets/web_explorer/ai_providers.js');
const languageGuidesPath = path.join(__dirname, '../app/src/main/assets/web_explorer/language_guides_data.js');

test('Responsive Matrix, Layout, and Feature Completeness', async (t) => {
  const indexHtml = fs.readFileSync(indexHtmlPath, 'utf8');
  const styleCss = fs.readFileSync(styleCssPath, 'utf8');
  const appJs = fs.readFileSync(appJsPath, 'utf8');
  const aiProvidersJs = fs.readFileSync(aiProvidersJsPath, 'utf8');
  const languageGuidesJs = fs.readFileSync(languageGuidesPath, 'utf8');

  await t.test('1. Developer Profile Card & "Test SMTP" Button Layout Assurance', () => {
    // Assert Developer Profile card exists and has proper responsive wrapping
    assert.match(indexHtml, /id="ownerDiagnosticsSection"/);
    assert.match(indexHtml, /class="developer-profile-card"/);

    // Assert "Test SMTP" button (btnDiagSendTest) exists and has min-height:48px, line-height, white-space, and overflow
    const testSmtpBtnRegex = /id="btnDiagSendTest"[^>]*style="[^"]*min-height:\s*48px[^"]*"/;
    assert.match(indexHtml, testSmtpBtnRegex, 'Test SMTP button must have min-height:48px for accessibility');
    assert.match(indexHtml, /id="btnDiagSendTest"[^>]*style="[^"]*white-space:\s*nowrap[^"]*"/, 'Test SMTP button must prevent text wrapping awkwardly');
    assert.match(indexHtml, /id="btnDiagSendTest"[^>]*style="[^"]*overflow:\s*visible[^"]*"/, 'Test SMTP button must have overflow:visible to avoid clipping');
    assert.match(indexHtml, />Test SMTP<\/button>/, 'Test SMTP button text must be exact');

    // Assert Check Connection button also has min-height:48px
    assert.match(indexHtml, /id="btnDiagCheck"[^>]*style="[^"]*min-height:\s*48px[^"]*"/);
  });

  await t.test('2. CSS Viewport Matrix & Media Queries (260px - 1024px)', () => {
    // Media queries for mobile & ultra-compact viewports
    assert.match(styleCss, /@media\s*\(\s*max-width:\s*480px\s*\)/, 'Must include max-width: 480px responsive rules');
    assert.match(styleCss, /@media\s*\(\s*max-width:\s*320px\s*\)/, 'Must include ultra-compact max-width: 320px responsive rules');
    assert.match(styleCss, /\.developer-profile-card\s*\{[^}]*box-sizing:\s*border-box/, 'Developer card must have box-sizing: border-box');
  });

  await t.test('3. Floating Debug Bug & Console Error Interceptor', () => {
    // Assert Floating Debug Bug exists in HTML
    assert.match(indexHtml, /id="btnDebugBug"/);
    assert.match(indexHtml, /id="debugLogModal"/);
    assert.match(indexHtml, /id="debugLogsList"/);
    assert.match(indexHtml, /class="floating-debug-bug"/);

    // Assert console interception exists in app.js
    assert.match(appJs, /window\.captureDebugLog\s*=/, 'Must provide global captureDebugLog function');
    assert.match(appJs, /window\.appDebugLogs\s*=/, 'Must provide debug logs array');
  });

  await t.test('4. AI Providers Engine: 15 Supported Providers Matrix', () => {
    const requiredProviders = [
      'openai', 'groq', 'openrouter', 'anthropic', 'gemini',
      'mistral', 'deepseek', 'xai', 'together', 'fireworks',
      'perplexity', 'cohere', 'ollama', 'lmstudio', 'custom'
    ];

    requiredProviders.forEach(pId => {
      assert.ok(aiProvidersJs.includes(`id: '${pId}'`) || aiProvidersJs.includes(`id: "${pId}"`), `Provider ${pId} must be defined`);
    });

    // Check sanitizeApiKey, maskApiKey, detectProviderFromKey, parseError, testProvider
    assert.match(aiProvidersJs, /function sanitizeApiKey/);
    assert.match(aiProvidersJs, /function maskApiKey/);
    assert.match(aiProvidersJs, /function detectProviderFromKey/);
    assert.match(aiProvidersJs, /function parseError/);
    assert.match(aiProvidersJs, /async function testProvider/);
  });

  await t.test('5. Language Guides & Cheat Sheets Completeness', () => {
    const expectedLanguages = [
      'python', 'javascript', 'typescript', 'java',
      'kotlin', 'bash', 'web', 'c', 'cpp', 'rust', 'go'
    ];

    expectedLanguages.forEach(lang => {
      assert.ok(languageGuidesJs.includes(`id: '${lang}'`) || languageGuidesJs.includes(`id: "${lang}"`), `Language guide ${lang} must exist`);
    });

    // Assert guide cards container and modal exist in HTML
    assert.match(indexHtml, /id="homeLanguageGuidesSection"/);
    assert.match(indexHtml, /id="languageGuideModal"/);
    assert.match(indexHtml, /id="guideTocChips"/);
    assert.match(indexHtml, /id="guideQuickRefTableContainer"/);
  });

  await t.test('6. Real-Time Telemetry & FPS Meter', () => {
    assert.match(indexHtml, /id="headerFpsBadge"/);
    assert.match(indexHtml, /id="measuredFpsLabel"/);
    assert.match(indexHtml, /id="ownerActiveMonitor"/);
    assert.match(indexHtml, /id="activeUsersCount"/);
    assert.match(indexHtml, /id="monitorLiveBadge"/);

    // Check FPS calculation in app.js
    assert.match(appJs, /function measureFpsLoop/);
    assert.match(appJs, /requestAnimationFrame\(measureFpsLoop\)/);
  });

  await t.test('7. Smooth Sidebar Expansion / Collapse Animations', () => {
    assert.match(styleCss, /\.sidebar-folder-children-wrapper/);
    assert.match(styleCss, /grid-template-rows:\s*0fr/);
    assert.match(styleCss, /grid-template-rows:\s*1fr/);
    assert.match(styleCss, /\.folder-chevron/);
    assert.match(styleCss, /transform:\s*rotate\(90deg\)/);
  });
});
