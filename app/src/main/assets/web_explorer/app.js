// Linq Studio Web Explorer & Prism.js IDE Engine
document.addEventListener('DOMContentLoaded', () => {
  // Navigation & Switcher Elements
  const btnViewResult = document.getElementById('btnViewResult');
  const btnViewCode = document.getElementById('btnViewCode');
  const btnRunActive = document.getElementById('btnRunActive');
  const liveResultView = document.getElementById('liveResultView');
  const editorCodeBody = document.getElementById('editorCodeBody');
  const userRenderedContent = document.getElementById('userRenderedContent');
  const toastContainer = document.getElementById('toastContainer');

  // Sidebar Elements
  const btnSidebarNewFile = document.getElementById('btnSidebarNewFile');
  const btnSidebarSaveFile = document.getElementById('btnSidebarSaveFile');
  const sidebarFileList = document.getElementById('sidebarFileList');
  const activeFileNameBadge = document.getElementById('activeFileNameBadge');
  const activeLanguageBadge = document.getElementById('activeLanguageBadge');

  // Prism Real-time Editor Elements
  const codeEditorArea = document.getElementById('codeEditorArea');
  const highlightingBlock = document.getElementById('highlightingBlock');
  const highlightingCode = document.getElementById('highlightingCode');

  // Terminal Drawer Elements
  const terminalDrawer = document.getElementById('terminalDrawer');
  const btnToggleTerminalDrawer = document.getElementById('btnToggleTerminalDrawer');
  const terminalDrawerHeader = document.getElementById('terminalDrawerHeader');
  const btnClearTerminalDrawer = document.getElementById('btnClearTerminalDrawer');
  const terminalLogsList = document.getElementById('terminalLogsList');
  const terminalLogCountBadge = document.getElementById('terminalLogCountBadge');
  const drawerToggleText = document.getElementById('drawerToggleText');
  const drawerChevronIcon = document.getElementById('drawerChevronIcon');
  const btnFilterAll = document.getElementById('btnFilterAll');
  const btnFilterLogs = document.getElementById('btnFilterLogs');
  const btnFilterErrors = document.getElementById('btnFilterErrors');
  const terminalCommandInput = document.getElementById('terminalCommandInput');
  const btnSendTerminalCommand = document.getElementById('btnSendTerminalCommand');

  // Interactive Demo Elements
  const btnResultCounter = document.getElementById('btnResultCounter');
  const btnResultReset = document.getElementById('btnResultReset');
  const btnResultToggleTheme = document.getElementById('btnResultToggleTheme');
  const counterLabel = document.getElementById('counterLabel');
  const resultCanvas = document.getElementById('resultCanvas');

  // --- Multi-file Local Project Store (Mapped to Editor Session) ---
  const defaultProjectFiles = {
    'index.html': {
      name: 'index.html',
      language: 'html',
      prismLang: 'markup',
      content: `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Linq Studio Project</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="card">
    <h1>Hello from Linq Studio!</h1>
    <p>Real-time Prism.js syntax highlighting enabled.</p>
    <button id="demoBtn">Click Me</button>
  </div>
  <script src="app.js"></script>
</body>
</html>`
    },
    'style.css': {
      name: 'style.css',
      language: 'css',
      prismLang: 'css',
      content: `/* Custom App Styles */
body {
  font-family: system-ui, -apple-system, sans-serif;
  background-color: #0F172A;
  color: #F8FAFC;
  padding: 20px;
}

.card {
  background: #1E293B;
  border: 1px solid #334155;
  border-radius: 12px;
  padding: 20px;
}

h1 {
  color: #38BDF8;
  font-size: 20px;
}

button {
  background: #6366F1;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}`
    },
    'app.js': {
      name: 'app.js',
      language: 'javascript',
      prismLang: 'javascript',
      content: `// Interactive JavaScript logic
console.log("Linq Studio application initialized!");

const demoBtn = document.getElementById('demoBtn');
if (demoBtn) {
  demoBtn.addEventListener('click', () => {
    console.info("User tapped demo button at: " + new Date().toLocaleTimeString());
    alert("Button tapped inside sandbox!");
  });
}`
    },
    'main.py': {
      name: 'main.py',
      language: 'python',
      prismLang: 'javascript',
      content: `# Python Script Demo
def calculate_metrics(count):
    print(f"Processing {count} items...")
    return [i ** 2 for i in range(count)]

results = calculate_metrics(5)
print("Computed values:", results)`
    }
  };

  let projectFiles = { ...defaultProjectFiles };
  let activeFileName = 'index.html';

  function getPrismLanguage(fileName) {
    if (fileName.endsWith('.html') || fileName.endsWith('.htm')) return 'markup';
    if (fileName.endsWith('.css')) return 'css';
    if (fileName.endsWith('.js') || fileName.endsWith('.ts')) return 'javascript';
    return 'javascript';
  }

  function getLanguageBadgeText(fileName) {
    if (fileName.endsWith('.html')) return 'HTML';
    if (fileName.endsWith('.css')) return 'CSS';
    if (fileName.endsWith('.js')) return 'JavaScript';
    if (fileName.endsWith('.py')) return 'Python';
    return 'Text';
  }

  // --- Real-Time Prism.js Syntax Highlighting ---
  function updatePrismHighlighting() {
    if (!codeEditorArea || !highlightingCode || !highlightingBlock) return;
    const text = codeEditorArea.value;
    const prismLang = getPrismLanguage(activeFileName);

    // Update Prism language class on pre & code
    highlightingBlock.className = `language-${prismLang}`;
    highlightingCode.className = `language-${prismLang}`;

    if (window.Prism && Prism.languages && Prism.languages[prismLang]) {
      highlightingCode.innerHTML = Prism.highlight(text, Prism.languages[prismLang], prismLang) + '\n';
    } else {
      highlightingCode.textContent = text + '\n';
    }

    // Synchronize scroll positions
    highlightingBlock.scrollTop = codeEditorArea.scrollTop;
    highlightingBlock.scrollLeft = codeEditorArea.scrollLeft;
  }

  // Listen to input and scroll for instant real-time highlighting
  if (codeEditorArea) {
    codeEditorArea.addEventListener('input', () => {
      if (projectFiles[activeFileName]) {
        projectFiles[activeFileName].content = codeEditorArea.value;
        projectFiles[activeFileName].isModified = true;
      }
      renderSidebarFiles();
      updatePrismHighlighting();
    });

    codeEditorArea.addEventListener('scroll', () => {
      if (highlightingBlock) {
        highlightingBlock.scrollTop = codeEditorArea.scrollTop;
        highlightingBlock.scrollLeft = codeEditorArea.scrollLeft;
      }
    });

    // Support Tab key indentation
    codeEditorArea.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        e.preventDefault();
        const start = codeEditorArea.selectionStart;
        const end = codeEditorArea.selectionEnd;
        codeEditorArea.value = codeEditorArea.value.substring(0, start) + '  ' + codeEditorArea.value.substring(end);
        codeEditorArea.selectionStart = codeEditorArea.selectionEnd = start + 2;
        updatePrismHighlighting();
      }
    });
  }

  // --- Render File Explorer Sidebar ---
  function renderSidebarFiles() {
    if (!sidebarFileList) return;
    sidebarFileList.innerHTML = '';

    Object.keys(projectFiles).forEach(fileName => {
      const file = projectFiles[fileName];
      const li = document.createElement('li');
      li.className = `sidebar-file-item ${fileName === activeFileName ? 'active' : ''}`;

      const ext = fileName.split('.').pop().toLowerCase();
      li.innerHTML = `
        <div class="file-item-left">
          <span class="file-type-icon ${ext}">${ext}</span>
          <span class="file-name">${fileName}</span>
        </div>
        ${file.isModified ? '<span class="file-dirty-dot" title="Unsaved changes"></span>' : ''}
      `;

      li.addEventListener('click', () => switchActiveFile(fileName));
      sidebarFileList.appendChild(li);
    });
  }

  function switchActiveFile(fileName) {
    if (!projectFiles[fileName]) return;
    activeFileName = fileName;
    const file = projectFiles[fileName];

    if (codeEditorArea) {
      codeEditorArea.value = file.content;
    }
    if (activeFileNameBadge) activeFileNameBadge.textContent = fileName;
    if (activeLanguageBadge) activeLanguageBadge.textContent = getLanguageBadgeText(fileName);

    renderSidebarFiles();
    updatePrismHighlighting();
  }

  // 'New File' button handler
  if (btnSidebarNewFile) {
    btnSidebarNewFile.addEventListener('click', () => {
      const newName = prompt('Enter new file name (e.g., script.js, about.html, utils.css):', 'newFile.js');
      if (newName && newName.trim()) {
        const cleanName = newName.trim();
        if (projectFiles[cleanName]) {
          alert('A file with this name already exists!');
          switchActiveFile(cleanName);
          return;
        }
        projectFiles[cleanName] = {
          name: cleanName,
          language: cleanName.split('.').pop(),
          prismLang: getPrismLanguage(cleanName),
          content: cleanName.endsWith('.html') ? '<!DOCTYPE html>\n<html>\n<body>\n</body>\n</html>' : '// New file: ' + cleanName + '\n',
          isModified: false
        };
        switchActiveFile(cleanName);
        showToast(`✓ Created file ${cleanName}`);
      }
    });
  }

  // 'Save' button handler
  if (btnSidebarSaveFile) {
    btnSidebarSaveFile.addEventListener('click', () => {
      if (projectFiles[activeFileName]) {
        projectFiles[activeFileName].content = codeEditorArea.value;
        projectFiles[activeFileName].isModified = false;
        renderSidebarFiles();
        showToast(`💾 Saved ${activeFileName} to local session`);
        appendConsoleLog('info', `[File System] Saved ${activeFileName} successfully.`);
      }
    });
  }

  // --- Expandable Terminal Drawer & Sandbox Logger ---
  let capturedLogs = [];
  let drawerState = 'collapsed'; // 'collapsed', 'expanded', 'fullscreen'
  let currentLogFilter = 'all';

  function appendConsoleLog(level, message, source = null) {
    const entry = {
      id: Date.now() + Math.random(),
      level: level.toLowerCase(), // 'log', 'info', 'warn', 'error'
      message: typeof message === 'object' ? JSON.stringify(message) : String(message),
      source: source || 'sandbox',
      time: new Date().toLocaleTimeString()
    };
    capturedLogs.push(entry);
    updateTerminalLogsView();

    // If it's an error, auto-expand drawer to alert user if collapsed
    if (level === 'error' && drawerState === 'collapsed') {
      setDrawerState('expanded');
    }
  }

  function updateTerminalLogsView() {
    if (!terminalLogsList) return;
    terminalLogsList.innerHTML = '';

    const filtered = capturedLogs.filter(log => {
      if (currentLogFilter === 'logs') return log.level === 'log' || log.level === 'info';
      if (currentLogFilter === 'errors') return log.level === 'error' || log.level === 'warn';
      return true;
    });

    filtered.forEach(log => {
      const div = document.createElement('div');
      div.className = `log-entry ${log.level}`;
      div.innerHTML = `
        <span class="log-time">[${log.time}]</span>
        <span class="log-level ${log.level}">${log.level.toUpperCase()}</span>
        <span class="log-msg">${escapeHtml(log.message)}</span>
        ${log.source ? `<span class="log-src">${escapeHtml(log.source)}</span>` : ''}
      `;
      terminalLogsList.appendChild(div);
    });

    // Auto-scroll to bottom
    terminalLogsList.scrollTop = terminalLogsList.scrollHeight;

    // Update count badge
    if (terminalLogCountBadge) {
      const errors = capturedLogs.filter(l => l.level === 'error').length;
      terminalLogCountBadge.textContent = `${capturedLogs.length} logs${errors > 0 ? ` (${errors} err)` : ''}`;
      terminalLogCountBadge.style.color = errors > 0 ? '#F87171' : '#94A3B8';
    }
  }

  function escapeHtml(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  function setDrawerState(state) {
    drawerState = state;
    terminalDrawer.className = `terminal-drawer ${state}`;
    if (drawerToggleText) {
      drawerToggleText.textContent = state === 'collapsed' ? 'Expand' : (state === 'expanded' ? 'Collapse' : 'Minimize');
    }
    if (drawerChevronIcon) {
      drawerChevronIcon.style.transform = state === 'collapsed' ? 'rotate(0deg)' : 'rotate(180deg)';
    }
  }

  if (btnToggleTerminalDrawer) {
    btnToggleTerminalDrawer.addEventListener('click', (e) => {
      e.stopPropagation();
      setDrawerState(drawerState === 'collapsed' ? 'expanded' : 'collapsed');
    });
  }

  if (terminalDrawerHeader) {
    terminalDrawerHeader.addEventListener('click', () => {
      setDrawerState(drawerState === 'collapsed' ? 'expanded' : 'collapsed');
    });
  }

  if (btnClearTerminalDrawer) {
    btnClearTerminalDrawer.addEventListener('click', (e) => {
      e.stopPropagation();
      capturedLogs = [];
      updateTerminalLogsView();
      showToast('Console cleared');
    });
  }

  // Filter chips
  if (btnFilterAll) {
    btnFilterAll.addEventListener('click', (e) => {
      e.stopPropagation();
      currentLogFilter = 'all';
      updateFilterButtons();
      updateTerminalLogsView();
    });
  }
  if (btnFilterLogs) {
    btnFilterLogs.addEventListener('click', (e) => {
      e.stopPropagation();
      currentLogFilter = 'logs';
      updateFilterButtons();
      updateTerminalLogsView();
    });
  }
  if (btnFilterErrors) {
    btnFilterErrors.addEventListener('click', (e) => {
      e.stopPropagation();
      currentLogFilter = 'errors';
      updateFilterButtons();
      updateTerminalLogsView();
    });
  }

  function updateFilterButtons() {
    [btnFilterAll, btnFilterLogs, btnFilterErrors].forEach(b => b && b.classList.remove('active'));
    if (currentLogFilter === 'all' && btnFilterAll) btnFilterAll.classList.add('active');
    if (currentLogFilter === 'logs' && btnFilterLogs) btnFilterLogs.classList.add('active');
    if (currentLogFilter === 'errors' && btnFilterErrors) btnFilterErrors.classList.add('active');
  }

  // Interactive eval input
  function evalTerminalInput() {
    if (!terminalCommandInput) return;
    const code = terminalCommandInput.value.trim();
    if (!code) return;
    appendConsoleLog('log', `> ${code}`, 'input');
    try {
      const res = eval(code);
      appendConsoleLog('info', `< ${res !== undefined ? String(res) : 'undefined'}`);
    } catch (err) {
      appendConsoleLog('error', err.message, 'eval');
    }
    terminalCommandInput.value = '';
  }

  if (btnSendTerminalCommand) btnSendTerminalCommand.addEventListener('click', evalTerminalInput);
  if (terminalCommandInput) {
    terminalCommandInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') evalTerminalInput();
    });
  }

  // --- Intercept userRenderedContent sandbox console & errors ---
  const originalConsole = {
    log: console.log,
    info: console.info,
    warn: console.warn,
    error: console.error
  };

  console.log = function(...args) {
    originalConsole.log.apply(console, args);
    appendConsoleLog('log', args.map(a => typeof a === 'object' ? JSON.stringify(a) : a).join(' '));
  };
  console.info = function(...args) {
    originalConsole.info.apply(console, args);
    appendConsoleLog('info', args.map(a => typeof a === 'object' ? JSON.stringify(a) : a).join(' '));
  };
  console.warn = function(...args) {
    originalConsole.warn.apply(console, args);
    appendConsoleLog('warn', args.map(a => typeof a === 'object' ? JSON.stringify(a) : a).join(' '));
  };
  console.error = function(...args) {
    originalConsole.error.apply(console, args);
    appendConsoleLog('error', args.map(a => typeof a === 'object' ? JSON.stringify(a) : a).join(' '));
  };

  window.onerror = function(message, source, lineno, colno, error) {
    const srcStr = source ? `${source.split('/').pop()}:${lineno}` : `line ${lineno}`;
    appendConsoleLog('error', message, srcStr);
    return false;
  };

  // --- View Switcher (Result vs Code) ---
  function setView(mode) {
    if (mode === 'result') {
      btnViewResult.classList.add('active');
      btnViewCode.classList.remove('active');
      liveResultView.style.display = 'flex';
      editorCodeBody.style.display = 'none';
      drawCanvas();
    } else {
      btnViewCode.classList.add('active');
      btnViewResult.classList.remove('active');
      liveResultView.style.display = 'none';
      editorCodeBody.style.display = 'flex';
      updatePrismHighlighting();
    }
  }

  if (btnViewResult) btnViewResult.addEventListener('click', () => setView('result'));
  if (btnViewCode) btnViewCode.addEventListener('click', () => setView('code'));

  // Run Code Action: updates userRenderedContent sandbox and captures any errors
  if (btnRunActive) {
    btnRunActive.addEventListener('click', () => {
      const code = codeEditorArea ? codeEditorArea.value : '';
      if (userRenderedContent) {
        try {
          appendConsoleLog('info', `[Sandbox] Executing ${activeFileName} in userRenderedContent...`);
          
          if (activeFileName.endsWith('.html')) {
            userRenderedContent.innerHTML = `<div style="background:#0F172A; border-radius:8px; padding:16px; border:1px solid #334155;">${code}</div>`;
            // Execute any scripts found in the rendered HTML
            const scripts = userRenderedContent.querySelectorAll('script');
            scripts.forEach(s => {
              try {
                eval(s.textContent);
              } catch (err) {
                appendConsoleLog('error', `Script Error: ${err.message}`, activeFileName);
              }
            });
          } else if (activeFileName.endsWith('.js')) {
            const res = eval(code);
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:8px; padding:16px; border:1px solid #334155;">
                <h4 style="color:#38BDF8; margin-bottom:8px;">JS Execution Output:</h4>
                <pre style="color:#34D399; font-family:monospace; font-size:12px;">${res !== undefined ? JSON.stringify(res, null, 2) : '✓ Executed successfully without errors'}</pre>
              </div>`;
          } else {
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:8px; padding:16px; border:1px solid #334155;">
                <pre style="color:#CBD5E1; font-family:monospace; font-size:12px;">${escapeHtml(code)}</pre>
              </div>`;
          }

          showToast('✓ Executed live code output!');
          appendConsoleLog('log', `[Sandbox] Live render completed for ${activeFileName}`);
        } catch (err) {
          appendConsoleLog('error', `Execution Exception: ${err.message}`, activeFileName);
          showToast(`❌ Error: ${err.message}`);
        }
      }
      setView('result');
    });
  }

  // Demo buttons
  let clicks = 0;
  if (btnResultCounter && counterLabel) {
    btnResultCounter.addEventListener('click', () => {
      clicks++;
      counterLabel.textContent = `Clicks: ${clicks}`;
      console.log(`Counter updated to ${clicks}`);
    });
  }
  if (btnResultReset && counterLabel) {
    btnResultReset.addEventListener('click', () => {
      clicks = 0;
      counterLabel.textContent = 'Clicks: 0';
      console.log('Counter reset');
    });
  }

  function drawCanvas(color = '#38BDF8') {
    if (!resultCanvas) return;
    const ctx = resultCanvas.getContext('2d');
    if (!ctx) return;
    ctx.clearRect(0, 0, resultCanvas.width, resultCanvas.height);
    ctx.strokeStyle = color;
    ctx.lineWidth = 3;
    ctx.beginPath();
    const w = resultCanvas.width;
    const h = resultCanvas.height;
    for (let x = 0; x < w; x++) {
      const y = h / 2 + Math.sin(x * 0.04) * 20 * Math.cos(x * 0.015);
      if (x === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
  }

  if (btnResultToggleTheme) {
    const colors = ['#6366F1', '#06B6D4', '#10B981', '#F59E0B', '#EC4899'];
    let idx = 0;
    btnResultToggleTheme.addEventListener('click', () => {
      idx = (idx + 1) % colors.length;
      drawCanvas(colors[idx]);
    });
  }
  drawCanvas();

  function showToast(msg) {
    if (!toastContainer) return;
    const t = document.createElement('div');
    t.className = 'toast';
    t.textContent = msg;
    toastContainer.appendChild(t);
    setTimeout(() => t.remove(), 2500);
  }

  // Initialize
  renderSidebarFiles();
  switchActiveFile('index.html');
  setView('result');
});
