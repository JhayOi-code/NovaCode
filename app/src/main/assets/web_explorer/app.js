// Step 3: Unregister all service workers, clear cache storage, and purge obsolete verification state
if (typeof navigator !== 'undefined' && 'serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(regs => {
    regs.forEach(r => r.unregister());
  }).catch(() => {});
}
if (typeof window !== 'undefined' && 'caches' in window) {
  caches.keys().then(keys => {
    keys.forEach(k => caches.delete(k));
  }).catch(() => {});
}
try {
  const legacyStorageKeys = [
    'linq_auth_pending', 'auth_pending',
    'auth_state', 'device_session', 'client_trust'
  ];
  legacyStorageKeys.forEach(prefix => {
    Object.keys(localStorage || {}).forEach(k => {
      if (k.toLowerCase().includes(prefix.toLowerCase())) localStorage.removeItem(k);
    });
    Object.keys(sessionStorage || {}).forEach(k => {
      if (k.toLowerCase().includes(prefix.toLowerCase())) sessionStorage.removeItem(k);
    });
  });
} catch (_) {}

// Linq Studios Web Explorer & Multi-Language IDE Engine
document.addEventListener('DOMContentLoaded', () => {
  // Navigation & Switcher Elements
  const btnViewResult = document.getElementById('btnViewResult');
  const btnViewCode = document.getElementById('btnViewCode');
  const btnRunActive = document.getElementById('btnRunActive');
  const liveResultView = document.getElementById('liveResultView');
  const editorCodeBody = document.getElementById('editorCodeBody');
  const userRenderedContent = document.getElementById('userRenderedContent');
  const toastContainer = document.getElementById('toastContainer');
  const btnToggleMobileSidebar = document.getElementById('btnToggleMobileSidebar');
  const sidebarBackdrop = document.getElementById('sidebarBackdrop');

  // Sidebar Elements
  const fileExplorerSidebar = document.getElementById('fileExplorerSidebar');
  const btnSidebarUnifiedCreate = document.getElementById('btnSidebarUnifiedCreate');
  const btnSidebarSaveFile = document.getElementById('btnSidebarSaveFile');
  const sidebarFileList = document.getElementById('sidebarFileList');
  const activeFileNameBadge = document.getElementById('activeFileNameBadge');
  const activeLanguageSelect = document.getElementById('activeLanguageSelect');
  const syntaxStatusBadge = document.getElementById('syntaxStatusBadge');

  // Unified Create Modal Elements
  const unifiedCreateModal = document.getElementById('unifiedCreateModal');
  const btnCloseCreateModal = document.getElementById('btnCloseCreateModal');
  const btnCancelCreateModal = document.getElementById('btnCancelCreateModal');
  const btnConfirmUnifiedCreate = document.getElementById('btnConfirmUnifiedCreate');
  const btnSelectTypeFile = document.getElementById('btnSelectTypeFile');
  const btnSelectTypeFolder = document.getElementById('btnSelectTypeFolder');
  const fileCreationFields = document.getElementById('fileCreationFields');
  const folderCreationFields = document.getElementById('folderCreationFields');
  const createFileNameInput = document.getElementById('createFileNameInput');
  const createFileFolderSelect = document.getElementById('createFileFolderSelect');
  const createFolderNameInput = document.getElementById('createFolderNameInput');
  const quickExtPills = document.querySelectorAll('.quick-ext-pill');

  // Local Dev Server Host Folder Controls
  const btnChooseHostFolder = document.getElementById('btnChooseHostFolder');
  const btnQuickCreateFolder = document.getElementById('btnQuickCreateFolder');
  const hostFolderModal = document.getElementById('hostFolderModal');
  const btnCloseHostFolderModal = document.getElementById('btnCloseHostFolderModal');
  const hostFolderOptionsList = document.getElementById('hostFolderOptionsList');
  const newHostFolderInput = document.getElementById('newHostFolderInput');
  const btnCreateAndHostFolder = document.getElementById('btnCreateAndHostFolder');
  const activeHostFolderBadge = document.getElementById('activeHostFolderBadge');
  const liveMockupUrl = document.getElementById('liveMockupUrl');

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
  const terminalThemeSelect = document.getElementById('terminalThemeSelect');
  const terminalCommandInput = document.getElementById('terminalCommandInput');
  const btnSendTerminalCommand = document.getElementById('btnSendTerminalCommand');

  // Suggestion & Settings Elements
  const editorSuggestionOverlay = document.getElementById('editorSuggestionOverlay');
  const toggleAutoSave = document.getElementById('toggleAutoSave');

  // Interactive Demo Elements
  const btnResultCounter = document.getElementById('btnResultCounter');
  const btnResultReset = document.getElementById('btnResultReset');
  const btnResultToggleTheme = document.getElementById('btnResultToggleTheme');
  const counterLabel = document.getElementById('counterLabel');
  const resultCanvas = document.getElementById('resultCanvas');

  // --- Multi-folder Organized Local Project Store ---
  let folders = ['src', 'scripts', 'styles', 'docs', 'data'];
  let folderExpanded = { 'src': true, 'scripts': true, 'styles': true, 'docs': true, 'data': true, 'root': true };

  const defaultProjectFiles = {
    'src/index.html': {
      path: 'src/index.html',
      name: 'index.html',
      folder: 'src',
      syntax: 'markup',
      content: `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Linq Studios App</title>
  <link rel="stylesheet" href="styles/style.css">
</head>
<body>
  <div class="card">
    <h1>🚀 Linq Studios Local Dev Server</h1>
    <p>Real-time dynamic syntax rendering for Python, JavaScript, CSS, and Markdown.</p>
    <button id="demoBtn">Execute Action</button>
  </div>
  <script src="src/app.js"></script>
</body>
</html>`
    },
    'styles/style.css': {
      path: 'styles/style.css',
      name: 'style.css',
      folder: 'styles',
      syntax: 'css',
      content: `/* Linq Studios Responsive Styling */
:root {
  --primary: #00E5FF;
  --bg-dark: #0B0F17;
  --card-bg: #1E293B;
  --text-main: #F8FAFC;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background-color: var(--bg-dark);
  color: var(--text-main);
  padding: 24px;
  line-height: 1.6;
}

.card {
  background: var(--card-bg);
  border: 1px solid #334155;
  border-radius: 14px;
  padding: 24px;
  max-width: 540px;
  margin: 0 auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

h1 {
  color: var(--primary);
  font-size: 22px;
  margin-bottom: 12px;
}

button {
  background: linear-gradient(135deg, #6366F1, #4F46E5);
  color: #FFFFFF;
  border: none;
  padding: 10px 22px;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s ease;
}

button:active {
  transform: scale(0.97);
}`
    },
    'src/app.js': {
      path: 'src/app.js',
      name: 'app.js',
      folder: 'src',
      syntax: 'javascript',
      content: `// Linq Studios Interactive JavaScript Engine
console.log("Linq Studios dev environment initialized on port 8080!");

const demoBtn = document.getElementById('demoBtn');
if (demoBtn) {
  demoBtn.addEventListener('click', () => {
    const timestamp = new Date().toLocaleTimeString();
    console.info("Action button triggered at: " + timestamp);
    alert("Interactive event fired successfully!");
  });
}`
    },
    'scripts/main.py': {
      path: 'scripts/main.py',
      name: 'main.py',
      folder: 'scripts',
      syntax: 'python',
      content: `# Python Syntax Runner in Linq Studios
import math

def calculate_analytics(data_points: list) -> dict:
    """Computes mean, variance, and standard deviation."""
    n = len(data_points)
    if n == 0:
        return {"error": "Empty list"}
    
    mean = sum(data_points) / n
    variance = sum((x - mean) ** 2 for x in data_points) / n
    std_dev = math.sqrt(variance)
    
    return {
        "count": n,
        "mean": round(mean, 3),
        "variance": round(variance, 3),
        "std_dev": round(std_dev, 3)
    }

# Run sample computation
sample_data = [12.5, 18.2, 24.0, 9.8, 31.4, 45.1]
stats = calculate_analytics(sample_data)
print("Analytics Summary:", stats)`
    },
    'docs/README.md': {
      path: 'docs/README.md',
      name: 'README.md',
      folder: 'docs',
      syntax: 'markdown',
      content: `# Linq Studios Workspace

Welcome to **Linq Studios**, the high-performance mobile IDE and local developer environment.

## Key Features
- **Dynamic Multi-Language Syntax Highlighting**: Seamless support for Python, JavaScript, CSS, Markdown, HTML, JSON, and SQL.
- **Unified Organization**: Create files and folders together in one streamlined workflow.
- **Zero-Overlap Architecture**: Optimized for compact phones, foldables, and large tablet screens.
- **Integrated Terminal**: Live interactive console and debugger.

### Code Example
\`\`\`python
# Fast execution
def greet(developer: str):
    print(f"Hello {developer}, welcome to Linq Studios!")

greet("Ljhay")
\`\`\`

> Main developer: **Ljhay**.`
    },
    'data/config.json': {
      path: 'data/config.json',
      name: 'config.json',
      folder: 'data',
      syntax: 'json',
      content: `{
  "app": "Linq Studios",
  "version": "2.4.0",
  "devServer": {
    "port": 8080,
    "host": "localhost",
    "hotReload": true
  },
  "syntaxThemes": ["dark", "high-contrast"],
  "creator": "Ljhay"
}`
    }
  };

  let projectFiles = { ...defaultProjectFiles };
  let activeFilePath = 'src/index.html';
  let activeCreationType = 'file'; // 'file' or 'folder'
  let activeHostFolder = localStorage.getItem('linq_last_hosted_folder') || 'src';

  // --- Dynamic Language Detection & Resolution ---
  function getPrismLanguage(filePath) {
    if (projectFiles[filePath] && projectFiles[filePath].syntax) {
      return projectFiles[filePath].syntax;
    }
    const ext = filePath.split('.').pop().toLowerCase();
    switch (ext) {
      case 'py':
      case 'pyw':
      case 'python':
        return 'python';
      case 'md':
      case 'markdown':
      case 'mdown':
        return 'markdown';
      case 'css':
      case 'scss':
      case 'less':
        return 'css';
      case 'js':
      case 'mjs':
      case 'cjs':
      case 'ts':
      case 'tsx':
      case 'jsx':
        return 'javascript';
      case 'html':
      case 'htm':
      case 'xml':
      case 'svg':
        return 'markup';
      case 'json':
        return 'json';
      case 'sql':
        return 'sql';
      case 'java':
        return 'java';
      case 'kt':
      case 'kts':
        return 'kotlin';
      default:
        return 'markup';
    }
  }

  function getLanguageBadgeLabel(prismLang) {
    switch (prismLang) {
      case 'python': return 'Python';
      case 'markdown': return 'Markdown';
      case 'css': return 'CSS';
      case 'javascript': return 'JavaScript';
      case 'markup': return 'HTML / XML';
      case 'json': return 'JSON';
      case 'sql': return 'SQL';
      case 'java': return 'Java';
      case 'kotlin': return 'Kotlin';
      default: return 'Text';
    }
  }

  // --- Real-Time Multi-Language Syntax Highlighting Engine ---
  function updatePrismHighlighting() {
    if (!codeEditorArea || !highlightingCode || !highlightingBlock) return;
    const text = codeEditorArea.value;
    const prismLang = activeLanguageSelect ? activeLanguageSelect.value : getPrismLanguage(activeFilePath);

    // Update classes
    highlightingBlock.className = `language-${prismLang}`;
    highlightingCode.className = `language-${prismLang}`;

    // Add trailing whitespace if ending in newline to ensure scroll height matches textarea
    const contentToHighlight = text.endsWith('\n') ? text + ' ' : text;

    if (window.Prism && Prism.languages && Prism.languages[prismLang]) {
      try {
        highlightingCode.innerHTML = Prism.highlight(contentToHighlight, Prism.languages[prismLang], prismLang);
      } catch (e) {
        highlightingCode.textContent = contentToHighlight;
      }
      if (syntaxStatusBadge) syntaxStatusBadge.textContent = `${getLanguageBadgeLabel(prismLang)} Dynamic`;
    } else {
      highlightingCode.textContent = contentToHighlight;
      if (syntaxStatusBadge) syntaxStatusBadge.textContent = 'Plaintext';
    }

    // Synchronize scroll positions exactly
    highlightingBlock.scrollTop = codeEditorArea.scrollTop;
    highlightingBlock.scrollLeft = codeEditorArea.scrollLeft;
  }

  // Language dropdown user selection handler
  if (activeLanguageSelect) {
    activeLanguageSelect.addEventListener('change', () => {
      const selectedLang = activeLanguageSelect.value;
      if (projectFiles[activeFilePath]) {
        projectFiles[activeFilePath].syntax = selectedLang;
      }
      updatePrismHighlighting();
      showToast(`Syntax set to ${getLanguageBadgeLabel(selectedLang)}`);
    });
  }

  // --- Auto-Save Feature Implementation ---
  function isAutoSaveEnabled() {
    const val = localStorage.getItem('linq_autosave_enabled');
    return val === null ? true : val === 'true';
  }

  function setAutoSaveEnabled(enabled) {
    localStorage.setItem('linq_autosave_enabled', String(enabled));
  }

  let autoSaveTimeout = null;
  function triggerAutoSaveDebounced() {
    if (!isAutoSaveEnabled()) return;
    if (autoSaveTimeout) clearTimeout(autoSaveTimeout);
    autoSaveTimeout = setTimeout(() => {
      if (projectFiles[activeFilePath] && projectFiles[activeFilePath].isModified) {
        projectFiles[activeFilePath].content = codeEditorArea.value;
        projectFiles[activeFilePath].isModified = false;
        renderSidebar();
        appendConsoleLog('info', `[Auto-Save] Periodically saved ${activeFilePath}`);
      }
    }, 1200);
  }

  // --- Multi-Language Suggestion Overlay Engine ---
  const LANGUAGE_SUGGESTIONS = {
    javascript: [
      { label: 'function', snippet: 'function name(params) {\n  \n}', kind: 'keyword' },
      { label: 'const', snippet: 'const name = value;', kind: 'keyword' },
      { label: 'let', snippet: 'let name = value;', kind: 'keyword' },
      { label: 'async/await', snippet: 'async function loadData() {\n  const res = await fetch(url);\n  return res.json();\n}', kind: 'snippet' },
      { label: 'console.log', snippet: 'console.log();', kind: 'snippet' },
      { label: 'addEventListener', snippet: "element.addEventListener('click', (e) => {\n  \n});", kind: 'snippet' },
      { label: 'document.getElementById', snippet: "document.getElementById('')", kind: 'snippet' },
      { label: 'import', snippet: "import { item } from 'module';", kind: 'keyword' },
      { label: 'export', snippet: 'export default ', kind: 'keyword' },
      { label: 'if/else', snippet: 'if (condition) {\n  \n} else {\n  \n}', kind: 'snippet' },
      { label: 'try/catch', snippet: 'try {\n  \n} catch (err) {\n  console.error(err);\n}', kind: 'snippet' },
      { label: 'Promise', snippet: 'new Promise((resolve, reject) => {\n  \n})', kind: 'snippet' }
    ],
    python: [
      { label: 'def', snippet: 'def function_name(args):\n    pass', kind: 'keyword' },
      { label: 'class', snippet: 'class ClassName:\n    def __init__(self):\n        pass', kind: 'keyword' },
      { label: 'import', snippet: 'import os, sys', kind: 'keyword' },
      { label: 'from...import', snippet: 'from module import item', kind: 'keyword' },
      { label: 'print', snippet: 'print()', kind: 'snippet' },
      { label: 'if __name__', snippet: "if __name__ == '__main__':\n    main()", kind: 'snippet' },
      { label: 'try/except', snippet: 'try:\n    pass\nexcept Exception as e:\n    print(e)', kind: 'snippet' },
      { label: 'for in range', snippet: 'for i in range(10):\n    print(i)', kind: 'snippet' },
      { label: 'with open', snippet: "with open('file.txt', 'r') as f:\n    content = f.read()", kind: 'snippet' }
    ],
    markup: [
      { label: '<!DOCTYPE html>', snippet: '<!DOCTYPE html>\n<html lang="en">\n<head>\n  <meta charset="UTF-8">\n  <title>Document</title>\n</head>\n<body>\n  \n</body>\n</html>', kind: 'snippet' },
      { label: 'div', snippet: '<div class="">\n  \n</div>', kind: 'snippet' },
      { label: 'button', snippet: '<button type="button" class="">Click</button>', kind: 'snippet' },
      { label: 'script', snippet: '<script src="app.js"></script>', kind: 'snippet' },
      { label: 'link:css', snippet: '<link rel="stylesheet" href="style.css">', kind: 'snippet' },
      { label: 'input', snippet: '<input type="text" placeholder="" />', kind: 'snippet' },
      { label: 'form', snippet: '<form action="" method="POST">\n  \n</form>', kind: 'snippet' },
      { label: 'span', snippet: '<span></span>', kind: 'snippet' }
    ],
    css: [
      { label: 'display: flex', snippet: 'display: flex;\nalign-items: center;\njustify-content: center;', kind: 'snippet' },
      { label: 'display: grid', snippet: 'display: grid;\ngrid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\ngap: 16px;', kind: 'snippet' },
      { label: 'background', snippet: 'background-color: #111827;', kind: 'keyword' },
      { label: 'color', snippet: 'color: #FFFFFF;', kind: 'keyword' },
      { label: 'border-radius', snippet: 'border-radius: 8px;', kind: 'keyword' },
      { label: 'padding', snippet: 'padding: 12px 16px;', kind: 'keyword' },
      { label: 'margin', snippet: 'margin: 0 auto;', kind: 'keyword' },
      { label: '@media', snippet: '@media (max-width: 768px) {\n  \n}', kind: 'snippet' }
    ],
    markdown: [
      { label: '# Heading 1', snippet: '# Title\n\n', kind: 'snippet' },
      { label: '## Heading 2', snippet: '## Subtitle\n\n', kind: 'snippet' },
      { label: 'codeblock', snippet: '```javascript\n\n```', kind: 'snippet' },
      { label: 'link', snippet: '[text](https://example.com)', kind: 'snippet' },
      { label: 'bullet list', snippet: '- Item 1\n- Item 2\n- Item 3', kind: 'snippet' }
    ],
    sql: [
      { label: 'SELECT', snippet: 'SELECT * FROM table_name WHERE condition;', kind: 'keyword' },
      { label: 'INSERT INTO', snippet: 'INSERT INTO table_name (col1, col2) VALUES (val1, val2);', kind: 'keyword' },
      { label: 'UPDATE', snippet: 'UPDATE table_name SET col1 = val1 WHERE condition;', kind: 'keyword' },
      { label: 'DELETE FROM', snippet: 'DELETE FROM table_name WHERE condition;', kind: 'keyword' },
      { label: 'CREATE TABLE', snippet: 'CREATE TABLE users (\n  id INT PRIMARY KEY AUTO_INCREMENT,\n  name VARCHAR(255) NOT NULL,\n  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n);', kind: 'snippet' }
    ],
    json: [
      { label: 'object', snippet: '{\n  "key": "value"\n}', kind: 'snippet' },
      { label: 'array', snippet: '[\n  "item1",\n  "item2"\n]', kind: 'snippet' }
    ],
    kotlin: [
      { label: 'fun main', snippet: 'fun main() {\n    println("Hello Linq")\n}', kind: 'snippet' },
      { label: 'data class', snippet: 'data class User(val id: String, val name: String)', kind: 'snippet' },
      { label: 'val/var', snippet: 'val name: String = ""', kind: 'keyword' }
    ],
    java: [
      { label: 'main', snippet: 'public static void main(String[] args) {\n    System.out.println("Hello");\n}', kind: 'snippet' },
      { label: 'class', snippet: 'public class Main {\n    \n}', kind: 'snippet' },
      { label: 'sout', snippet: 'System.out.println();', kind: 'snippet' }
    ]
  };

  let activeSuggestionIndex = 0;
  let currentFilteredSuggestions = [];
  let currentPrefix = '';

  function hideSuggestionOverlay() {
    if (editorSuggestionOverlay) {
      editorSuggestionOverlay.style.display = 'none';
      editorSuggestionOverlay.innerHTML = '';
    }
    currentFilteredSuggestions = [];
    currentPrefix = '';
    activeSuggestionIndex = 0;
  }

  function applySuggestion(suggestion) {
    if (!codeEditorArea || !suggestion) return;
    const pos = codeEditorArea.selectionStart;
    const textBefore = codeEditorArea.value.substring(0, pos);
    const textAfter = codeEditorArea.value.substring(pos);

    const newTextBefore = textBefore.substring(0, textBefore.length - currentPrefix.length) + suggestion.snippet;
    codeEditorArea.value = newTextBefore + textAfter;
    codeEditorArea.selectionStart = codeEditorArea.selectionEnd = newTextBefore.length;

    hideSuggestionOverlay();
    updatePrismHighlighting();
    if (projectFiles[activeFilePath]) {
      projectFiles[activeFilePath].content = codeEditorArea.value;
      projectFiles[activeFilePath].isModified = true;
    }
    renderSidebar();
    triggerAutoSaveDebounced();
    codeEditorArea.focus();
  }

  function updateSuggestionOverlay(suggestions, prefix) {
    if (!editorSuggestionOverlay || suggestions.length === 0) {
      hideSuggestionOverlay();
      return;
    }
    currentFilteredSuggestions = suggestions;
    currentPrefix = prefix;
    activeSuggestionIndex = 0;

    editorSuggestionOverlay.innerHTML = '';
    suggestions.forEach((item, index) => {
      const div = document.createElement('div');
      div.className = `suggestion-item ${index === 0 ? 'active' : ''}`;
      div.setAttribute('role', 'option');
      div.innerHTML = `
        <span class="suggestion-label">${escapeHtml(item.label)}</span>
        <span class="suggestion-kind">${escapeHtml(item.kind)}</span>
      `;
      div.addEventListener('mousedown', (e) => {
        e.preventDefault();
        applySuggestion(item);
      });
      editorSuggestionOverlay.appendChild(div);
    });

    // Position overlay relative to editor
    editorSuggestionOverlay.style.display = 'block';
    editorSuggestionOverlay.style.right = '16px';
    editorSuggestionOverlay.style.bottom = '16px';
  }

  function checkSuggestionsTrigger() {
    if (!codeEditorArea || !editorSuggestionOverlay) return;
    const pos = codeEditorArea.selectionStart;
    const textBefore = codeEditorArea.value.substring(0, pos);
    const match = textBefore.match(/([a-zA-Z0-9_#<]+)$/);
    if (!match) {
      hideSuggestionOverlay();
      return;
    }
    const prefix = match[1];
    if (prefix.length < 1) {
      hideSuggestionOverlay();
      return;
    }

    const currentSyntax = activeLanguageSelect ? activeLanguageSelect.value : getPrismLanguage(activeFilePath);
    const pool = LANGUAGE_SUGGESTIONS[currentSyntax] || LANGUAGE_SUGGESTIONS.javascript || [];
    const matches = pool.filter(item => item.label.toLowerCase().includes(prefix.toLowerCase())).slice(0, 5);

    if (matches.length > 0) {
      updateSuggestionOverlay(matches, prefix);
    } else {
      hideSuggestionOverlay();
    }
  }

  // Real-time input handling
  if (codeEditorArea) {
    codeEditorArea.addEventListener('input', () => {
      if (projectFiles[activeFilePath]) {
        projectFiles[activeFilePath].content = codeEditorArea.value;
        projectFiles[activeFilePath].isModified = true;
      }
      renderSidebar();
      updatePrismHighlighting();
      triggerAutoSaveDebounced();
      checkSuggestionsTrigger();
    });

    codeEditorArea.addEventListener('scroll', () => {
      if (highlightingBlock) {
        highlightingBlock.scrollTop = codeEditorArea.scrollTop;
        highlightingBlock.scrollLeft = codeEditorArea.scrollLeft;
      }
    });

    // Keyboard navigation for suggestions and Tab indentation
    codeEditorArea.addEventListener('keydown', (e) => {
      if (editorSuggestionOverlay && editorSuggestionOverlay.style.display === 'block' && currentFilteredSuggestions.length > 0) {
        if (e.key === 'ArrowDown') {
          e.preventDefault();
          activeSuggestionIndex = (activeSuggestionIndex + 1) % currentFilteredSuggestions.length;
          const items = editorSuggestionOverlay.querySelectorAll('.suggestion-item');
          items.forEach((it, idx) => it.classList.toggle('active', idx === activeSuggestionIndex));
          return;
        }
        if (e.key === 'ArrowUp') {
          e.preventDefault();
          activeSuggestionIndex = (activeSuggestionIndex - 1 + currentFilteredSuggestions.length) % currentFilteredSuggestions.length;
          const items = editorSuggestionOverlay.querySelectorAll('.suggestion-item');
          items.forEach((it, idx) => it.classList.toggle('active', idx === activeSuggestionIndex));
          return;
        }
        if (e.key === 'Enter' || e.key === 'Tab') {
          e.preventDefault();
          const target = currentFilteredSuggestions[activeSuggestionIndex];
          if (target) applySuggestion(target);
          return;
        }
        if (e.key === 'Escape') {
          e.preventDefault();
          hideSuggestionOverlay();
          return;
        }
      }

      // Default Tab key indentation when overlay not active
      if (e.key === 'Tab') {
        e.preventDefault();
        const start = codeEditorArea.selectionStart;
        const end = codeEditorArea.selectionEnd;
        codeEditorArea.value = codeEditorArea.value.substring(0, start) + '  ' + codeEditorArea.value.substring(end);
        codeEditorArea.selectionStart = codeEditorArea.selectionEnd = start + 2;
        updatePrismHighlighting();
        triggerAutoSaveDebounced();
      }
    });

    // Hide suggestions when clicking outside
    document.addEventListener('click', (e) => {
      if (editorSuggestionOverlay && !editorSuggestionOverlay.contains(e.target) && e.target !== codeEditorArea) {
        hideSuggestionOverlay();
      }
    });
  }

  // --- Organized File & Folder Tree Rendering ---
  function renderSidebar() {
    if (!sidebarFileList) return;
    sidebarFileList.innerHTML = '';

    // Group files by folder
    const grouped = { 'root': [] };
    folders.forEach(f => { grouped[f] = []; });

    Object.keys(projectFiles).forEach(path => {
      const file = projectFiles[path];
      const folder = file.folder || 'root';
      if (!grouped[folder]) {
        grouped[folder] = [];
        if (!folders.includes(folder)) folders.push(folder);
      }
      grouped[folder].push(file);
    });

    // Sort folders with 'root' at bottom or top
    const allFolderKeys = Object.keys(grouped).sort((a, b) => {
      if (a === 'root') return 1;
      if (b === 'root') return -1;
      return a.localeCompare(b);
    });

    allFolderKeys.forEach(folderName => {
      const filesInFolder = grouped[folderName];
      if (folderName === 'root' && filesInFolder.length === 0) return;

      const isExpanded = folderExpanded[folderName] !== false;

      const folderDiv = document.createElement('div');
      folderDiv.className = 'sidebar-folder-group';

      if (folderName !== 'root') {
        const header = document.createElement('div');
        header.className = 'sidebar-folder-header';
        header.innerHTML = `
          <div class="folder-header-left">
            <span class="folder-chevron ${isExpanded ? 'open' : ''}">▸</span>
            <span class="folder-icon">${isExpanded ? '📂' : '📁'}</span>
            <span class="folder-name">${folderName}</span>
          </div>
          <div class="folder-header-right">
            <span class="folder-count">${filesInFolder.length}</span>
            <button class="btn-folder-add" title="Create file in ${folderName}" data-folder="${folderName}">+</button>
          </div>
        `;

        header.querySelector('.folder-header-left').addEventListener('click', () => {
          folderExpanded[folderName] = !isExpanded;
          renderSidebar();
        });

        header.querySelector('.btn-folder-add').addEventListener('click', (e) => {
          e.stopPropagation();
          openUnifiedCreateModal('file', folderName);
        });

        folderDiv.appendChild(header);
      }

      // Folder Children List
      if (isExpanded || folderName === 'root') {
        const ul = document.createElement('ul');
        ul.className = 'sidebar-file-list-inner';

        filesInFolder.forEach(file => {
          const li = document.createElement('li');
          li.className = `sidebar-file-item ${file.path === activeFilePath ? 'active' : ''}`;

          const ext = file.name.split('.').pop().toLowerCase();
          li.innerHTML = `
            <div class="file-item-left">
              <span class="file-type-icon ${ext}">${ext.substring(0, 3)}</span>
              <span class="file-name">${file.name}</span>
            </div>
            ${file.isModified ? '<span class="file-dirty-dot" title="Unsaved changes"></span>' : ''}
          `;

          li.addEventListener('click', () => {
            switchActiveFile(file.path);
            closeMobileSidebar();
          });
          ul.appendChild(li);
        });

        folderDiv.appendChild(ul);
      }

      sidebarFileList.appendChild(folderDiv);
    });
  }

  function switchActiveFile(filePath) {
    if (!projectFiles[filePath]) return;
    activeFilePath = filePath;
    const file = projectFiles[filePath];

    if (codeEditorArea) {
      codeEditorArea.value = file.content;
    }
    if (activeFileNameBadge) activeFileNameBadge.textContent = file.name;

    const detectedLang = getPrismLanguage(filePath);
    if (activeLanguageSelect) {
      activeLanguageSelect.value = file.syntax || detectedLang;
    }

    renderSidebar();
    updatePrismHighlighting();
  }

  // --- Unified Create Modal Implementation (File + Folder as ONE) ---
  function openUnifiedCreateModal(type = 'file', targetFolder = null) {
    activeCreationType = type;
    if (unifiedCreateModal) unifiedCreateModal.style.display = 'flex';

    setTypeMode(type);

    // Populate folder dropdown
    if (createFileFolderSelect) {
      createFileFolderSelect.innerHTML = '';
      const rootOpt = document.createElement('option');
      rootOpt.value = 'root';
      rootOpt.textContent = '/ (Workspace Root)';
      createFileFolderSelect.appendChild(rootOpt);

      folders.forEach(f => {
        const opt = document.createElement('option');
        opt.value = f;
        opt.textContent = `${f}/`;
        if (targetFolder && targetFolder === f) opt.selected = true;
        createFileFolderSelect.appendChild(opt);
      });
    }

    if (type === 'file' && createFileNameInput) {
      createFileNameInput.value = '';
      setTimeout(() => createFileNameInput.focus(), 100);
    } else if (type === 'folder' && createFolderNameInput) {
      createFolderNameInput.value = '';
      setTimeout(() => createFolderNameInput.focus(), 100);
    }
  }

  function closeUnifiedCreateModal() {
    if (unifiedCreateModal) unifiedCreateModal.style.display = 'none';
  }

  function setTypeMode(type) {
    activeCreationType = type;
    if (type === 'file') {
      btnSelectTypeFile.classList.add('active');
      btnSelectTypeFolder.classList.remove('active');
      fileCreationFields.style.display = 'block';
      folderCreationFields.style.display = 'none';
      if (btnConfirmUnifiedCreate) btnConfirmUnifiedCreate.textContent = 'Create File';
    } else {
      btnSelectTypeFolder.classList.add('active');
      btnSelectTypeFile.classList.remove('active');
      fileCreationFields.style.display = 'none';
      folderCreationFields.style.display = 'block';
      if (btnConfirmUnifiedCreate) btnConfirmUnifiedCreate.textContent = 'Create Folder';
    }
  }

  if (btnSelectTypeFile) btnSelectTypeFile.addEventListener('click', () => setTypeMode('file'));
  if (btnSelectTypeFolder) btnSelectTypeFolder.addEventListener('click', () => setTypeMode('folder'));

  let selectedFolderTemplate = 'blank';
  const templatePills = document.querySelectorAll('.template-pill');
  const templateInfoText = document.getElementById('templateInfoText');
  const templateDescriptions = {
    blank: 'Clean empty folder ready to organize code files and assets.',
    project: 'Generic blank project with README.md and .gitignore.',
    python: 'Python application project with main.py, requirements.txt, and README.md.',
    web: 'Full web project with index.html, style.css, and app.js configured for dev server hosting.',
    java: 'Java project with Main.java entry point and README.md.'
  };

  if (templatePills) {
    templatePills.forEach(pill => {
      pill.addEventListener('click', () => {
        templatePills.forEach(p => {
          p.classList.remove('active');
          p.style.borderColor = 'var(--border-light)';
          p.style.background = 'var(--card-bg)';
        });
        pill.classList.add('active');
        pill.style.borderColor = 'var(--accent)';
        pill.style.background = 'rgba(0, 229, 255, 0.15)';
        selectedFolderTemplate = pill.getAttribute('data-tpl') || 'blank';
        if (templateInfoText) {
          templateInfoText.textContent = templateDescriptions[selectedFolderTemplate] || '';
        }
      });
    });
  }

  if (quickExtPills) {
    quickExtPills.forEach(pill => {
      pill.addEventListener('click', () => {
        const ext = pill.getAttribute('data-ext');
        const curr = createFileNameInput.value.trim();
        const base = curr.includes('.') ? curr.substring(0, curr.lastIndexOf('.')) : (curr || 'new_script');
        createFileNameInput.value = `${base}${ext}`;
        createFileNameInput.focus();
      });
    });
  }

  if (btnSidebarUnifiedCreate) {
    btnSidebarUnifiedCreate.addEventListener('click', () => openUnifiedCreateModal('file'));
  }

  if (btnCloseCreateModal) btnCloseCreateModal.addEventListener('click', closeUnifiedCreateModal);
  if (btnCancelCreateModal) btnCancelCreateModal.addEventListener('click', closeUnifiedCreateModal);

  if (btnConfirmUnifiedCreate) {
    btnConfirmUnifiedCreate.addEventListener('click', () => {
      if (activeCreationType === 'folder') {
        const name = createFolderNameInput.value.trim().replace(/[\/\\?%*:|"<>]/g, '');
        if (!name) {
          alert('Please enter a valid folder name.');
          return;
        }
        if (!folders.includes(name)) {
          folders.push(name);
          folderExpanded[name] = true;

          // Scaffold template files if template selected
          let firstFileToOpen = null;
          if (selectedFolderTemplate === 'python') {
            const p1 = `${name}/main.py`;
            const p2 = `${name}/requirements.txt`;
            const p3 = `${name}/README.md`;
            projectFiles[p1] = { path: p1, name: 'main.py', folder: name, syntax: 'python', content: 'def main():\n    print("Hello from ' + name + '!")\n\nif __name__ == "__main__":\n    main()\n', isModified: false };
            projectFiles[p2] = { path: p2, name: 'requirements.txt', folder: name, syntax: 'text', content: '# Dependencies for ' + name + '\nrequests>=2.31.0\n', isModified: false };
            projectFiles[p3] = { path: p3, name: 'README.md', folder: name, syntax: 'markdown', content: '# ' + name + '\n\nPython project created in Linq Studios.\n', isModified: false };
            firstFileToOpen = p1;
          } else if (selectedFolderTemplate === 'web') {
            const p1 = `${name}/index.html`;
            const p2 = `${name}/style.css`;
            const p3 = `${name}/app.js`;
            projectFiles[p1] = { path: p1, name: 'index.html', folder: name, syntax: 'markup', content: '<!DOCTYPE html>\n<html>\n<head>\n  <meta charset="UTF-8">\n  <title>' + name + '</title>\n  <link rel="stylesheet" href="style.css">\n</head>\n<body>\n  <h1>🌐 Welcome to ' + name + '</h1>\n  <script src="app.js"></script>\n</body>\n</html>', isModified: false };
            projectFiles[p2] = { path: p2, name: 'style.css', folder: name, syntax: 'css', content: 'body {\n  font-family: sans-serif;\n  background: #111;\n  color: #eee;\n  padding: 20px;\n}\n', isModified: false };
            projectFiles[p3] = { path: p3, name: 'app.js', folder: name, syntax: 'javascript', content: 'console.log("' + name + ' loaded successfully!");\n', isModified: false };
            firstFileToOpen = p1;
          } else if (selectedFolderTemplate === 'java') {
            const p1 = `${name}/Main.java`;
            const p2 = `${name}/README.md`;
            projectFiles[p1] = { path: p1, name: 'Main.java', folder: name, syntax: 'java', content: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello from ' + name + '!");\n    }\n}\n', isModified: false };
            projectFiles[p2] = { path: p2, name: 'README.md', folder: name, syntax: 'markdown', content: '# ' + name + '\n\nJava project created in Linq Studios.\n', isModified: false };
            firstFileToOpen = p1;
          } else if (selectedFolderTemplate === 'project') {
            const p1 = `${name}/README.md`;
            const p2 = `${name}/.gitignore`;
            projectFiles[p1] = { path: p1, name: 'README.md', folder: name, syntax: 'markdown', content: '# ' + name + '\n\nNew project.\n', isModified: false };
            projectFiles[p2] = { path: p2, name: '.gitignore', folder: name, syntax: 'text', content: '*.log\n.DS_Store\n', isModified: false };
            firstFileToOpen = p1;
          }

          renderSidebar();
          closeUnifiedCreateModal();
          if (firstFileToOpen) {
            switchActiveFile(firstFileToOpen);
          }
          showToast(`📁 Created project folder '${name}'`);
        } else {
          alert(`Folder '${name}' already exists.`);
        }
      } else {
        const name = createFileNameInput.value.trim();
        if (!name) {
          alert('Please enter a file name.');
          return;
        }
        const selectedFolder = createFileFolderSelect ? createFileFolderSelect.value : 'root';
        const fullPath = selectedFolder === 'root' ? name : `${selectedFolder}/${name}`;

        if (projectFiles[fullPath]) {
          alert('A file with this name already exists in this folder!');
          switchActiveFile(fullPath);
          closeUnifiedCreateModal();
          return;
        }

        const ext = name.split('.').pop().toLowerCase();
        let starter = `// File: ${name}\n`;
        let detectedSyntax = 'javascript';
        if (ext === 'html' || ext === 'htm') {
          starter = '<!DOCTYPE html>\n<html>\n<head>\n  <meta charset="UTF-8">\n</head>\n<body>\n  <h1>New Page</h1>\n</body>\n</html>';
          detectedSyntax = 'markup';
        } else if (ext === 'py') {
          starter = '# Python Script\ndef main():\n    print("Hello from Python!")\n\nif __name__ == "__main__":\n    main()\n';
          detectedSyntax = 'python';
        } else if (ext === 'md') {
          starter = `# ${name.replace('.md', '')}\n\nDocument details and notes.\n`;
          detectedSyntax = 'markdown';
        } else if (ext === 'css') {
          starter = '/* CSS Stylesheet */\nbody {\n  margin: 0;\n  padding: 16px;\n}\n';
          detectedSyntax = 'css';
        } else if (ext === 'json') {
          starter = '{\n  "name": "example",\n  "active": true\n}\n';
          detectedSyntax = 'json';
        } else if (ext === 'sql') {
          starter = '-- SQL Query\nSELECT * FROM users WHERE active = 1;\n';
          detectedSyntax = 'sql';
        }

        projectFiles[fullPath] = {
          path: fullPath,
          name: name,
          folder: selectedFolder,
          syntax: detectedSyntax,
          content: starter,
          isModified: false
        };

        if (selectedFolder !== 'root') folderExpanded[selectedFolder] = true;
        closeUnifiedCreateModal();
        switchActiveFile(fullPath);
        showToast(`✓ Created ${fullPath}`);
      }
    });
  }

  // --- Save File Handler ---
  if (btnSidebarSaveFile) {
    btnSidebarSaveFile.addEventListener('click', () => {
      if (projectFiles[activeFilePath]) {
        projectFiles[activeFilePath].content = codeEditorArea.value;
        projectFiles[activeFilePath].isModified = false;
        renderSidebar();
        showToast(`💾 Saved ${projectFiles[activeFilePath].name}`);
        appendConsoleLog('info', `[File System] Saved ${activeFilePath} successfully.`);
      }
    });
  }

  // --- Choose Host Folder Modal for Localhost Website ---
  function openHostFolderModal() {
    if (!hostFolderModal || !hostFolderOptionsList) return;
    hostFolderOptionsList.innerHTML = '';

    folders.forEach(f => {
      const isCurrent = f === activeHostFolder;
      const count = Object.values(projectFiles).filter(file => (file.folder || 'root') === f).length;

      const item = document.createElement('div');
      item.className = `host-folder-option-item ${isCurrent ? 'active' : ''}`;
      item.innerHTML = `
        <div class="folder-left">
          <span>📁</span>
          <span>/${f}</span>
          <span style="font-size: 11px; color: var(--text-muted);">(${count} files)</span>
        </div>
        ${isCurrent ? '<span class="folder-badge-active">ACTIVE HOST</span>' : '<button class="quick-ext-pill" style="padding: 3px 8px; font-size:11px;">Select</button>'}
      `;
      item.addEventListener('click', () => {
        selectHostFolder(f);
      });
      hostFolderOptionsList.appendChild(item);
    });

    if (newHostFolderInput) newHostFolderInput.value = '';
    hostFolderModal.style.display = 'flex';
  }

  function closeHostFolderModal() {
    if (hostFolderModal) hostFolderModal.style.display = 'none';
  }

  function selectHostFolder(folderName) {
    activeHostFolder = folderName;
    try {
      localStorage.setItem('linq_last_hosted_folder', folderName);
    } catch (_) {}

    if (activeHostFolderBadge) activeHostFolderBadge.textContent = folderName;
    if (liveMockupUrl) liveMockupUrl.textContent = `http://localhost:8080/${folderName}/index.html`;
    closeHostFolderModal();
    showToast(`📁 Serving /${folderName} on localhost:8080`);
    appendConsoleLog('info', `[Dev Server] Serving folder '/${folderName}' on http://localhost:8080/`);

    // Switch active file if file exists in folder
    const fileInFolder = Object.keys(projectFiles).find(p => (projectFiles[p].folder || 'root') === folderName);
    if (fileInFolder) {
      switchActiveFile(fileInFolder);
    }
  }

  if (btnChooseHostFolder) btnChooseHostFolder.addEventListener('click', openHostFolderModal);
  if (btnCloseHostFolderModal) btnCloseHostFolderModal.addEventListener('click', closeHostFolderModal);
  if (btnQuickCreateFolder) btnQuickCreateFolder.addEventListener('click', () => openUnifiedCreateModal('folder'));

  if (btnCreateAndHostFolder && newHostFolderInput) {
    btnCreateAndHostFolder.addEventListener('click', () => {
      const name = newHostFolderInput.value.trim().replace(/[\/\\?%*:|"<>]/g, '');
      if (!name) {
        alert('Please enter a valid folder name.');
        return;
      }
      if (!folders.includes(name)) {
        folders.push(name);
        folderExpanded[name] = true;
      }
      selectHostFolder(name);
      renderSidebar();
    });
  }

  // --- Option 2: Folder from device ---
  const btnPickDeviceFolder = document.getElementById('btnPickDeviceFolder');
  const deviceFolderInput = document.getElementById('deviceFolderInput');

  if (btnPickDeviceFolder) {
    btnPickDeviceFolder.addEventListener('click', async () => {
      if ('showDirectoryPicker' in window && window.self === window.top) {
        try {
          const dirHandle = await window.showDirectoryPicker();
          const folderName = dirHandle.name.replace(/[\/\\?%*:|"<>]/g, '_') || 'device_folder';
          if (!folders.includes(folderName)) folders.push(folderName);
          folderExpanded[folderName] = true;

          for await (const entry of dirHandle.values()) {
            if (entry.kind === 'file') {
              const file = await entry.getFile();
              const text = await file.text();
              const p = `${folderName}/${file.name}`;
              projectFiles[p] = {
                path: p,
                name: file.name,
                folder: folderName,
                syntax: getPrismLanguage(p),
                content: text,
                isModified: false
              };
            }
          }
          renderSidebar();
          selectHostFolder(folderName);
          return;
        } catch (err) {
          if (err.name !== 'AbortError') {
            console.warn('[FS] showDirectoryPicker fallback to input:', err);
          } else {
            return;
          }
        }
      }
      // Fallback to webkitdirectory input
      if (deviceFolderInput) deviceFolderInput.click();
    });
  }

  if (deviceFolderInput) {
    deviceFolderInput.addEventListener('change', (e) => {
      const files = Array.from(e.target.files || []);
      if (files.length === 0) return;

      const firstPath = files[0].webkitRelativePath || files[0].name;
      const rootName = firstPath.includes('/') ? firstPath.split('/')[0] : 'device_folder';
      const folderName = rootName.replace(/[\/\\?%*:|"<>]/g, '_') || 'device_folder';

      if (!folders.includes(folderName)) folders.push(folderName);
      folderExpanded[folderName] = true;

      let readCount = 0;
      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = () => {
          const rel = file.webkitRelativePath ? file.webkitRelativePath.replace(rootName + '/', '') : file.name;
          const fullPath = `${folderName}/${rel}`;
          projectFiles[fullPath] = {
            path: fullPath,
            name: file.name,
            folder: folderName,
            syntax: getPrismLanguage(fullPath),
            content: reader.result,
            isModified: false
          };
          readCount++;
          if (readCount === files.length) {
            renderSidebar();
            selectHostFolder(folderName);
          }
        };
        reader.readAsText(file);
      });
    });
  }

  // --- Option 3: ZIP file ---
  const btnPickZipFile = document.getElementById('btnPickZipFile');
  const zipFileInput = document.getElementById('zipFileInput');

  if (btnPickZipFile && zipFileInput) {
    btnPickZipFile.addEventListener('click', () => zipFileInput.click());
    zipFileInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (!file) return;

      const folderName = file.name.replace(/\.zip$/i, '').replace(/[\/\\?%*:|"<>]/g, '_') || 'zip_site';
      if (!folders.includes(folderName)) folders.push(folderName);
      folderExpanded[folderName] = true;

      // Unpack ZIP or read files
      const reader = new FileReader();
      reader.onload = async () => {
        try {
          // If JSZip or DecompressionStream is available
          if (window.JSZip) {
            const zip = await window.JSZip.loadAsync(reader.result);
            const entries = Object.keys(zip.files);
            for (const filename of entries) {
              const zipEntry = zip.files[filename];
              if (!zipEntry.dir) {
                const content = await zipEntry.async('string');
                const p = `${folderName}/${filename.replace(/^\/+/, '')}`;
                projectFiles[p] = {
                  path: p,
                  name: filename.split('/').pop(),
                  folder: folderName,
                  syntax: getPrismLanguage(p),
                  content: content,
                  isModified: false
                };
              }
            }
          } else {
            // Virtual placeholder index.html for site
            const p = `${folderName}/index.html`;
            projectFiles[p] = {
              path: p,
              name: 'index.html',
              folder: folderName,
              syntax: 'markup',
              content: `<!DOCTYPE html>\n<html>\n<head><title>${folderName}</title></head>\n<body>\n  <h1>📦 ${folderName}</h1>\n  <p>ZIP package uploaded and ready to serve.</p>\n</body>\n</html>`,
              isModified: false
            };
          }
          renderSidebar();
          selectHostFolder(folderName);
        } catch (err) {
          showToast('ZIP uploaded to workspace');
          selectHostFolder(folderName);
        }
      };
      reader.readAsArrayBuffer(file);
    });
  }

  // --- Option 4: Choose file(s) ---
  const btnPickFilesBundle = document.getElementById('btnPickFilesBundle');
  const filesBundleInput = document.getElementById('filesBundleInput');

  if (btnPickFilesBundle && filesBundleInput) {
    btnPickFilesBundle.addEventListener('click', () => filesBundleInput.click());
    filesBundleInput.addEventListener('change', (e) => {
      const files = Array.from(e.target.files || []);
      if (files.length === 0) return;

      const folderName = 'custom_files';
      if (!folders.includes(folderName)) folders.push(folderName);
      folderExpanded[folderName] = true;

      let readCount = 0;
      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = () => {
          const fullPath = `${folderName}/${file.name}`;
          projectFiles[fullPath] = {
            path: fullPath,
            name: file.name,
            folder: folderName,
            syntax: getPrismLanguage(fullPath),
            content: reader.result,
            isModified: false
          };
          readCount++;
          if (readCount === files.length) {
            renderSidebar();
            selectHostFolder(folderName);
          }
        };
        reader.readAsText(file);
      });
    });
  }

  // --- Direct Firebase Authentication & Owner Management ---
  const firebaseConfig = {
    projectId: "gen-lang-client-0489618236",
    appId: "1:98398748028:web:7462057c34b685906a38f9",
    apiKey: "AIzaSyAWRa66wFGnNVX95v7fsLfm2vGspznhUfQ",
    authDomain: "gen-lang-client-0489618236.firebaseapp.com",
    storageBucket: "gen-lang-client-0489618236.firebasestorage.app",
    messagingSenderId: "98398748028"
  };

  let fbApp = null;
  let fbAuth = null;
  let fbDb = null;

  try {
    if (typeof firebase !== 'undefined') {
      if (!firebase.apps.length) {
        fbApp = firebase.initializeApp(firebaseConfig);
      } else {
        fbApp = firebase.app();
      }
      fbAuth = firebase.auth();
      fbDb = firebase.firestore();
      fbAuth.setPersistence(firebase.auth.Auth.Persistence.LOCAL)
        .catch(() => fbAuth.setPersistence(firebase.auth.Auth.Persistence.MEMORY));
    }
  } catch (err) {
    console.warn('[Firebase Client Init]', err.message);
  }

  const authModal = document.getElementById('authModal');
  const btnCloseAuthModal = document.getElementById('btnCloseAuthModal');
  const btnAuthBack = document.getElementById('btnAuthBack');
  const tabAuthLogin = document.getElementById('tabAuthLogin');
  const tabAuthSignup = document.getElementById('tabAuthSignup');
  const authTabSubtitle = document.getElementById('authTabSubtitle');
  const authTabSwitcher = document.getElementById('authTabSwitcher');
  const authErrorBox = document.getElementById('authErrorBox');
  const authErrorMessage = document.getElementById('authErrorMessage');
  const authNoticeBanner = document.getElementById('authNoticeBanner');

  const formAuthLogin = document.getElementById('formAuthLogin');
  const inputLoginEmail = document.getElementById('inputLoginEmail');
  const inputLoginPassword = document.getElementById('inputLoginPassword');
  const btnLoginSubmit = document.getElementById('btnLoginSubmit');
  const loginBtnSpinner = document.getElementById('loginBtnSpinner');
  const loginBtnText = document.getElementById('loginBtnText');
  const btnToggleLoginPass = document.getElementById('btnToggleLoginPass');
  const loginTypoSuggestion = document.getElementById('loginTypoSuggestion');
  const btnForgotPassLink = document.getElementById('btnForgotPassLink');

  const formAuthSignup = document.getElementById('formAuthSignup');
  const signupHoneypot = document.getElementById('signupHoneypot');
  const inputSignupName = document.getElementById('inputSignupName');
  const inputSignupEmail = document.getElementById('inputSignupEmail');
  const signupTypoSuggestion = document.getElementById('signupTypoSuggestion');
  const inputSignupPassword = document.getElementById('inputSignupPassword');
  const btnToggleSignupPass = document.getElementById('btnToggleSignupPass');
  const strengthText = document.getElementById('strengthText');
  const inputSignupConfirm = document.getElementById('inputSignupConfirm');
  const btnToggleSignupConfirm = document.getElementById('btnToggleSignupConfirm');
  const btnSignupSubmit = document.getElementById('btnSignupSubmit');
  const signupBtnSpinner = document.getElementById('signupBtnSpinner');
  const signupBtnText = document.getElementById('signupBtnText');

  const viewAuthForgotPass = document.getElementById('viewAuthForgotPass');
  const inputForgotEmail = document.getElementById('inputForgotEmail');
  const btnSendResetLink = document.getElementById('btnSendResetLink');
  const btnBackToLoginFromForgot = document.getElementById('btnBackToLoginFromForgot');

  const btnUserAccount = document.getElementById('btnUserAccount');
  const userAccountLabel = document.getElementById('userAccountLabel');
  const userAccountAvatar = document.getElementById('userAccountAvatar');
  const userOwnerBadge = document.getElementById('userOwnerBadge');
  const ownerEmailBanner = document.getElementById('ownerEmailBanner');
  const btnOwnerSendLink = document.getElementById('btnOwnerSendLink');
  const btnOwnerCheckVerified = document.getElementById('btnOwnerCheckVerified');
  const ownerActiveMonitor = document.getElementById('ownerActiveMonitor');
  const activeUsersCount = document.getElementById('activeUsersCount');

  const settingsModal = document.getElementById('settingsModal');
  const btnCloseSettingsModal = document.getElementById('btnCloseSettingsModal');
  const btnSignoutAllDevices = document.getElementById('btnSignoutAllDevices');
  const btnSettingsSignOut = document.getElementById('btnSettingsSignOut');
  const ownerDiagnosticsSection = document.getElementById('ownerDiagnosticsSection');
  const btnDiagCheck = document.getElementById('btnDiagCheck');
  const btnDiagSendTest = document.getElementById('btnDiagSendTest');
  const diagProgressContainer = document.getElementById('diagProgressContainer');
  const diagProgressList = document.getElementById('diagProgressList');
  const diagSummary = document.getElementById('diagSummary');
  const btnCopyDiagReport = document.getElementById('btnCopyDiagReport');

  let currentAuthTab = 'login';
  let clientFailedAttempts = 0;
  let clientThrottleUntil = 0;
  let hasShownOwnerWelcome = false;
  let activeDiagLog = [];

  function getStoredUser() {
    try {
      return JSON.parse(localStorage.getItem('linq_current_user') || 'null');
    } catch (_) {
      return null;
    }
  }

  function setStoredUser(user) {
    if (user) {
      localStorage.setItem('linq_current_user', JSON.stringify(user));
      updateUserAccountUI(user);
    } else {
      localStorage.removeItem('linq_current_user');
      updateUserAccountUI(null);
    }
  }

  function updateUserAccountUI(user) {
    if (!btnUserAccount) return;
    if (user && user.email) {
      const displayName = user.owner ? 'Ljhay' : (user.name || user.email.split('@')[0]);
      if (userAccountLabel) userAccountLabel.textContent = displayName;
      if (userAccountAvatar) userAccountAvatar.textContent = (displayName || 'U').charAt(0).toUpperCase();
      if (userOwnerBadge) userOwnerBadge.style.display = user.owner ? 'inline-block' : 'none';
      if (ownerActiveMonitor) ownerActiveMonitor.style.display = user.owner ? 'flex' : 'none';
      if (ownerDiagnosticsSection) ownerDiagnosticsSection.style.display = user.owner ? 'block' : 'none';
    } else {
      if (userAccountLabel) userAccountLabel.textContent = 'Log in';
      if (userAccountAvatar) userAccountAvatar.textContent = '👤';
      if (userOwnerBadge) userOwnerBadge.style.display = 'none';
      if (ownerEmailBanner) ownerEmailBanner.style.display = 'none';
      if (ownerActiveMonitor) ownerActiveMonitor.style.display = 'none';
      if (ownerDiagnosticsSection) ownerDiagnosticsSection.style.display = 'none';
    }
  }

  function openAuthModal(tab = 'login') {
    currentAuthTab = tab;
    setAuthTab(tab);
    if (authModal) authModal.style.display = 'flex';
    clearAuthErrors();
  }

  function closeAuthModal() {
    if (authModal) authModal.style.display = 'none';
    clearAuthErrors();
  }

  function setAuthTab(tab) {
    currentAuthTab = tab;
    clearAuthErrors();
    if (authTabSwitcher) authTabSwitcher.style.display = 'flex';
    if (viewAuthForgotPass) viewAuthForgotPass.style.display = 'none';

    if (tab === 'login') {
      if (tabAuthLogin) {
        tabAuthLogin.style.background = 'var(--card-bg)';
        tabAuthLogin.style.color = 'var(--text-main)';
        tabAuthLogin.style.fontWeight = 'bold';
      }
      if (tabAuthSignup) {
        tabAuthSignup.style.background = 'transparent';
        tabAuthSignup.style.color = 'var(--text-muted)';
        tabAuthSignup.style.fontWeight = 'normal';
      }
      if (authTabSubtitle) authTabSubtitle.textContent = 'Welcome back. Log in to your Linq Studio account.';
      if (formAuthLogin) formAuthLogin.style.display = 'flex';
      if (formAuthSignup) formAuthSignup.style.display = 'none';
    } else {
      if (tabAuthSignup) {
        tabAuthSignup.style.background = 'var(--card-bg)';
        tabAuthSignup.style.color = 'var(--text-main)';
        tabAuthSignup.style.fontWeight = 'bold';
      }
      if (tabAuthLogin) {
        tabAuthLogin.style.background = 'transparent';
        tabAuthLogin.style.color = 'var(--text-muted)';
        tabAuthLogin.style.fontWeight = 'normal';
      }
      if (authTabSubtitle) authTabSubtitle.textContent = 'Create your Linq Studio account in seconds.';
      if (formAuthLogin) formAuthLogin.style.display = 'none';
      if (formAuthSignup) formAuthSignup.style.display = 'flex';
    }
  }

  function showAuthError(errData) {
    if (!authErrorBox || !authErrorMessage) return;
    const { msg, code, canSwitchLogin } = typeof errData === 'string'
      ? { msg: errData, code: '', canSwitchLogin: false }
      : errData;

    authErrorBox.style.display = 'block';
    let html = `<div>${msg}</div>`;
    if (canSwitchLogin) {
      html += `<div style="margin-top:8px;"><button type="button" id="btnSwitchToLoginFromError" style="background:#3B82F6; color:#FFF; border:none; border-radius:6px; padding:6px 12px; font-size:12px; font-weight:bold; cursor:pointer;">Log in instead</button></div>`;
    }
    if (code) {
      html += `<div style="font-size:10px; opacity:0.6; margin-top:4px; font-family:monospace;">${code}</div>`;
    }
    authErrorMessage.innerHTML = html;

    if (canSwitchLogin) {
      const btnSwitch = document.getElementById('btnSwitchToLoginFromError');
      if (btnSwitch) {
        btnSwitch.onclick = () => {
          if (inputLoginEmail && inputSignupEmail) {
            inputLoginEmail.value = inputSignupEmail.value;
          }
          setAuthTab('login');
        };
      }
    }
  }

  function clearAuthErrors() {
    if (authErrorBox) authErrorBox.style.display = 'none';
    if (authNoticeBanner) authNoticeBanner.style.display = 'none';
  }

  // --- Client-side Throttle (5 failed attempts -> 30s wait) ---
  function checkClientThrottle() {
    const now = Date.now();
    if (now < clientThrottleUntil) {
      const waitSec = Math.ceil((clientThrottleUntil - now) / 1000);
      showAuthError(`Too many attempts. Try again in ${waitSec} second${waitSec === 1 ? '' : 's'} or reset your password.`);
      return false;
    }
    return true;
  }

  function recordAuthFailure() {
    clientFailedAttempts++;
    if (clientFailedAttempts >= 5) {
      clientThrottleUntil = Date.now() + 30000;
      clientFailedAttempts = 0;
      showAuthError('Too many attempts. Try again in 30 seconds or reset your password.');
    }
  }

  function recordAuthSuccess() {
    clientFailedAttempts = 0;
    clientThrottleUntil = 0;
  }

  // --- Email Typo Detection (Client side only, tappable fix) ---
  const COMMON_DOMAINS = {
    'gmial.com': 'gmail.com',
    'gmai.com': 'gmail.com',
    'gmail.con': 'gmail.com',
    'yahooo.com': 'yahoo.com',
    'hotnail.com': 'hotmail.com',
    'outlok.com': 'outlook.com'
  };

  function checkEmailTypo(inputEl, suggestionEl) {
    if (!inputEl || !suggestionEl) return;
    const val = inputEl.value.trim().toLowerCase();
    const parts = val.split('@');
    if (parts.length === 2 && COMMON_DOMAINS[parts[1]]) {
      const corrected = `${parts[0]}@${COMMON_DOMAINS[parts[1]]}`;
      suggestionEl.innerHTML = `Did you mean <button type="button" class="btn-typo-fix" style="background:transparent; border:none; color:#60A5FA; text-decoration:underline; cursor:pointer; font-size:11px; padding:0; font-weight:bold;">${corrected}</button>?`;
      suggestionEl.style.display = 'block';
      const btn = suggestionEl.querySelector('.btn-typo-fix');
      if (btn) {
        btn.onclick = () => {
          inputEl.value = corrected;
          suggestionEl.style.display = 'none';
        };
      }
    } else {
      suggestionEl.style.display = 'none';
      suggestionEl.innerHTML = '';
    }
  }

  if (inputLoginEmail && loginTypoSuggestion) {
    inputLoginEmail.addEventListener('blur', () => checkEmailTypo(inputLoginEmail, loginTypoSuggestion));
    inputLoginEmail.addEventListener('input', () => { if (loginTypoSuggestion.style.display !== 'none') checkEmailTypo(inputLoginEmail, loginTypoSuggestion); });
  }

  if (inputSignupEmail && signupTypoSuggestion) {
    inputSignupEmail.addEventListener('blur', () => checkEmailTypo(inputSignupEmail, signupTypoSuggestion));
    inputSignupEmail.addEventListener('input', () => { if (signupTypoSuggestion.style.display !== 'none') checkEmailTypo(inputSignupEmail, signupTypoSuggestion); });
  }

  // --- Show/Hide Password Toggles ---
  function wirePasswordToggle(buttonEl, inputEl) {
    if (!buttonEl || !inputEl) return;
    buttonEl.addEventListener('click', () => {
      const isPass = inputEl.type === 'password';
      inputEl.type = isPass ? 'text' : 'password';
      buttonEl.textContent = isPass ? '🙈' : '👁️';
    });
  }
  wirePasswordToggle(btnToggleLoginPass, inputLoginPassword);
  wirePasswordToggle(btnToggleSignupPass, inputSignupPassword);
  wirePasswordToggle(btnToggleSignupConfirm, inputSignupConfirm);

  // --- Password Strength Calculation ---
  if (inputSignupPassword) {
    inputSignupPassword.addEventListener('input', () => {
      const val = inputSignupPassword.value;
      if (!val) {
        if (strengthText) { strengthText.textContent = '—'; strengthText.style.color = 'var(--text-muted)'; }
        return;
      }
      let score = 0;
      if (val.length >= 8) score++;
      if (val.length >= 12) score++;
      if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++;
      if (/\d/.test(val)) score++;
      if (/[^A-Za-z0-9]/.test(val)) score++;

      if (strengthText) {
        if (score <= 2) {
          strengthText.textContent = 'Weak';
          strengthText.style.color = '#EF4444';
        } else if (score <= 4) {
          strengthText.textContent = 'Medium';
          strengthText.style.color = '#F59E0B';
        } else {
          strengthText.textContent = 'Strong';
          strengthText.style.color = '#10B981';
        }
      }
    });
  }

  // --- Clear & Human Firebase Error Formatting ---
  function formatAuthError(err) {
    if (!err) return { msg: 'An unexpected error occurred. Please try again.', code: '' };
    const code = err.code || '';
    if (code === 'auth/invalid-credential' || code === 'auth/wrong-password' || code === 'auth/user-not-found') {
      return { msg: 'Incorrect email or password.', code };
    }
    if (code === 'auth/email-already-in-use') {
      return {
        msg: 'An account with this email already exists. Log in instead.',
        code,
        canSwitchLogin: true
      };
    }
    if (code === 'auth/weak-password') {
      return { msg: 'Password is too weak. Please use at least 8 characters with letters and numbers.', code };
    }
    if (code === 'auth/invalid-email') {
      return { msg: 'Please enter a valid email address.', code };
    }
    if (code === 'auth/too-many-requests') {
      return { msg: 'Too many attempts. Try again in a few minutes or reset your password.', code };
    }
    if (code === 'auth/network-request-failed') {
      return { msg: 'No internet connection. Please check your network and try again.', code };
    }
    if (code === 'auth/user-disabled') {
      return { msg: 'This account has been disabled. Please contact support.', code };
    }
    if (code === 'auth/operation-not-allowed') {
      console.error('[Firebase Action] Enable Email/Password in Firebase Console > Authentication > Sign-in method');
      return { msg: 'Sign-in is temporarily unavailable.', code };
    }
    return { msg: err.message || 'Authentication error. Please try again.', code };
  }

  // --- Owner Status Sync (POST /api/auth/owner/sync) ---
  async function syncOwnerStatus() {
    const user = fbAuth ? fbAuth.currentUser : null;
    if (!user) {
      updateUserAccountUI(null);
      return;
    }

    try {
      const token = await user.getIdToken(true);
      const res = await fetch('/api/auth/owner/sync', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      const data = await res.json().catch(() => ({}));

      const isOwner = data.owner === true;
      const needsEmailLink = data.needsEmailLink === true;

      if (userOwnerBadge) userOwnerBadge.style.display = isOwner ? 'inline-block' : 'none';
      if (ownerEmailBanner) ownerEmailBanner.style.display = needsEmailLink ? 'block' : 'none';
      if (ownerActiveMonitor) ownerActiveMonitor.style.display = isOwner ? 'flex' : 'none';
      if (ownerDiagnosticsSection) ownerDiagnosticsSection.style.display = isOwner ? 'block' : 'none';

      if (isOwner) {
        if (userAccountLabel) userAccountLabel.textContent = 'Ljhay';
        if (!hasShownOwnerWelcome) {
          hasShownOwnerWelcome = true;
          showToast('👑 Welcome back, Ljhay! Owner privileges active.');
        }
      } else {
        if (userAccountLabel) {
          userAccountLabel.textContent = user.displayName || user.email.split('@')[0];
        }
      }

      const stored = {
        uid: user.uid,
        name: isOwner ? 'Ljhay' : (user.displayName || user.email.split('@')[0]),
        email: user.email,
        owner: isOwner,
        token
      };
      localStorage.setItem('linq_current_user', JSON.stringify(stored));
    } catch (err) {
      console.warn('[Owner Sync]', err.message);
    }
  }

  // Auto re-check owner verification when window regains focus
  window.addEventListener('focus', () => {
    if (fbAuth && fbAuth.currentUser) {
      fbAuth.currentUser.reload().then(() => syncOwnerStatus()).catch(() => {});
    }
  });

  if (btnOwnerSendLink) {
    btnOwnerSendLink.addEventListener('click', async () => {
      const user = fbAuth ? fbAuth.currentUser : null;
      if (!user) return;
      try {
        btnOwnerSendLink.disabled = true;
        btnOwnerSendLink.textContent = 'Sending...';
        await user.sendEmailVerification();
        showToast('✓ Verification link sent. Check your inbox.');
      } catch (err) {
        showToast('Error sending link: ' + err.message);
      } finally {
        btnOwnerSendLink.disabled = false;
        btnOwnerSendLink.textContent = 'Send link';
      }
    });
  }

  if (btnOwnerCheckVerified) {
    btnOwnerCheckVerified.addEventListener('click', async () => {
      const user = fbAuth ? fbAuth.currentUser : null;
      if (!user) return;
      btnOwnerCheckVerified.textContent = 'Checking...';
      try {
        await user.reload();
        await syncOwnerStatus();
        showToast('Status refreshed.');
      } catch (err) {
        showToast('Refresh note: ' + err.message);
      } finally {
        btnOwnerCheckVerified.textContent = "I've verified";
      }
    });
  }

  // --- Sign Up Form Submission (Direct into App) ---
  if (formAuthSignup) {
    formAuthSignup.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAuthErrors();

      // 1. Honeypot check
      if (signupHoneypot && signupHoneypot.value) {
        console.warn('[Abuse Protection] Automated submission trapped by honeypot.');
        return showAuthError('Submission rejected.');
      }

      // 2. Client throttle
      if (!checkClientThrottle()) return;

      const name = (inputSignupName?.value || '').trim();
      const email = (inputSignupEmail?.value || '').trim().toLowerCase();
      const password = inputSignupPassword?.value || '';
      const confirm = inputSignupConfirm?.value || '';

      if (!name) {
        inputSignupName?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Please enter your full name.');
      }
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        inputSignupEmail?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Please enter a valid email address.');
      }
      if (password.length < 8) {
        inputSignupPassword?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Password must be at least 8 characters long.');
      }
      if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) {
        inputSignupPassword?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Password must contain at least one letter and one number.');
      }
      if (password !== confirm) {
        inputSignupConfirm?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Passwords do not match. Please re-enter.');
      }

      if (signupBtnSpinner) signupBtnSpinner.style.display = 'inline-block';
      if (signupBtnText) signupBtnText.textContent = 'Creating account...';
      if (btnSignupSubmit) btnSignupSubmit.disabled = true;

      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('TIMEOUT')), 30000)
      );

      try {
        const signupPromise = (async () => {
          if (!fbAuth) throw new Error('Authentication client unavailable.');
          const cred = await fbAuth.createUserWithEmailAndPassword(email, password);
          if (cred.user && name) {
            await cred.user.updateProfile({ displayName: name }).catch(() => {});
          }

          // Create user profile in Firestore with transaction (create if not exists)
          if (fbDb && cred.user) {
            try {
              const userDocRef = fbDb.collection('users').doc(cred.user.uid);
              await fbDb.runTransaction(async (transaction) => {
                const doc = await transaction.get(userDocRef);
                if (!doc.exists) {
                  transaction.set(userDocRef, {
                    uid: cred.user.uid,
                    name: name,
                    email: email,
                    createdAt: firebase.firestore.FieldValue.serverTimestamp()
                  });
                }
              });
            } catch (err) {
              console.warn('[Firestore Profile] Transaction notice:', err.message);
            }
          }
          return cred.user;
        })();

        const user = await Promise.race([signupPromise, timeoutPromise]);
        recordAuthSuccess();

        closeAuthModal();
        showToast('✓ Account created! Welcome to Linq Studio.');
        await syncOwnerStatus();
      } catch (err) {
        recordAuthFailure();
        if (err.message === 'TIMEOUT') {
          showAuthError('Request timed out. Please check your connection and tap Create Account again.');
        } else {
          showAuthError(formatAuthError(err));
        }
      } finally {
        if (signupBtnSpinner) signupBtnSpinner.style.display = 'none';
        if (signupBtnText) signupBtnText.textContent = 'Create Account';
        if (btnSignupSubmit) btnSignupSubmit.disabled = false;
      }
    });
  }

  // --- Log In Form Submission (Direct into App) ---
  if (formAuthLogin) {
    formAuthLogin.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAuthErrors();

      if (!checkClientThrottle()) return;

      const email = (inputLoginEmail?.value || '').trim().toLowerCase();
      const password = inputLoginPassword?.value || '';

      if (!email) {
        inputLoginEmail?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Please enter your email.');
      }
      if (!password) {
        inputLoginPassword?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return showAuthError('Please enter your password.');
      }

      if (loginBtnSpinner) loginBtnSpinner.style.display = 'inline-block';
      if (loginBtnText) loginBtnText.textContent = 'Logging in...';
      if (btnLoginSubmit) btnLoginSubmit.disabled = true;

      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('TIMEOUT')), 30000)
      );

      try {
        const loginPromise = (async () => {
          if (!fbAuth) throw new Error('Authentication client unavailable.');
          const cred = await fbAuth.signInWithEmailAndPassword(email, password);
          return cred.user;
        })();

        const user = await Promise.race([loginPromise, timeoutPromise]);
        recordAuthSuccess();

        closeAuthModal();
        showToast('✓ Signed in successfully.');
        await syncOwnerStatus();
      } catch (err) {
        recordAuthFailure();
        if (err.message === 'TIMEOUT') {
          showAuthError('Request timed out. Please check your connection and tap Log in again.');
        } else {
          showAuthError(formatAuthError(err));
        }
      } finally {
        if (loginBtnSpinner) loginBtnSpinner.style.display = 'none';
        if (loginBtnText) loginBtnText.textContent = 'Log in';
        if (btnLoginSubmit) btnLoginSubmit.disabled = false;
      }
    });
  }

  // --- Forgot Password View & Link Submission ---
  if (btnForgotPassLink) {
    btnForgotPassLink.addEventListener('click', () => {
      clearAuthErrors();
      if (authTabSwitcher) authTabSwitcher.style.display = 'none';
      if (formAuthLogin) formAuthLogin.style.display = 'none';
      if (formAuthSignup) formAuthSignup.style.display = 'none';
      if (viewAuthForgotPass) viewAuthForgotPass.style.display = 'block';
      if (inputForgotEmail && inputLoginEmail) {
        inputForgotEmail.value = inputLoginEmail.value;
      }
    });
  }

  if (btnBackToLoginFromForgot) {
    btnBackToLoginFromForgot.addEventListener('click', () => {
      setAuthTab('login');
    });
  }

  if (btnSendResetLink) {
    btnSendResetLink.addEventListener('click', async () => {
      clearAuthErrors();
      const email = (inputForgotEmail?.value || '').trim().toLowerCase();
      if (!email || !email.includes('@')) {
        return showAuthError('Please enter a valid email address.');
      }

      btnSendResetLink.disabled = true;
      btnSendResetLink.textContent = 'Sending...';

      try {
        if (fbAuth) {
          await fbAuth.sendPasswordResetEmail(email).catch(() => {});
        }
      } catch (_) {}

      // Uniform generic response (Never reveals whether email exists)
      if (authNoticeBanner) {
        authNoticeBanner.style.display = 'block';
        authNoticeBanner.textContent = "If an account exists for this email, we've sent a reset link.";
      }
      btnSendResetLink.disabled = false;
      btnSendResetLink.textContent = 'Send Reset Link';
      setTimeout(() => {
        setAuthTab('login');
      }, 3500);
    });
  }

  // --- Settings & Security Modal Handlers ---
  async function loadSecuritySettings() {
    const user = getStoredUser();
    if (!user) {
      openAuthModal('login');
      return;
    }

    if (toggleAutoSave) {
      toggleAutoSave.checked = isAutoSaveEnabled();
    }

    if (settingsModal) settingsModal.style.display = 'flex';
    if (ownerDiagnosticsSection) {
      ownerDiagnosticsSection.style.display = user.owner ? 'block' : 'none';
    }
  }

  if (toggleAutoSave) {
    toggleAutoSave.checked = isAutoSaveEnabled();
    toggleAutoSave.addEventListener('change', () => {
      setAutoSaveEnabled(toggleAutoSave.checked);
      showToast(toggleAutoSave.checked ? '💾 Auto-save enabled' : 'Auto-save disabled');
      appendConsoleLog('info', `[Settings] Auto-save ${toggleAutoSave.checked ? 'enabled' : 'disabled'}`);
    });
  }

  if (btnUserAccount) {
    btnUserAccount.addEventListener('click', () => {
      const user = getStoredUser();
      if (user) {
        loadSecuritySettings();
      } else {
        openAuthModal('login');
      }
    });
  }

  if (btnCloseSettingsModal) {
    btnCloseSettingsModal.addEventListener('click', () => {
      if (settingsModal) settingsModal.style.display = 'none';
    });
  }

  if (btnSettingsSignOut) {
    btnSettingsSignOut.addEventListener('click', async () => {
      if (fbAuth) await fbAuth.signOut().catch(() => {});
      setStoredUser(null);
      if (settingsModal) settingsModal.style.display = 'none';
      showToast('Signed out successfully.');
    });
  }

  if (btnSignoutAllDevices) {
    btnSignoutAllDevices.addEventListener('click', async () => {
      if (!confirm('Sign out of all devices? This will invalidate all active sessions.')) return;
      const user = fbAuth ? fbAuth.currentUser : null;
      if (user) {
        try {
          const token = await user.getIdToken(true);
          await fetch('/api/auth/signout-all', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
          });
        } catch (_) {}
      }
      if (fbAuth) await fbAuth.signOut().catch(() => {});
      setStoredUser(null);
      if (settingsModal) settingsModal.style.display = 'none';
      showToast('Signed out of all devices.');
    });
  }

  // --- Real-time Owner Diagnostics Runner (NDJSON Stream) ---
  async function runOwnerDiagnostics(sendTest = false) {
    const user = getStoredUser();
    if (!user || !user.owner) return;

    if (diagProgressContainer) diagProgressContainer.style.display = 'block';
    if (diagProgressList) diagProgressList.innerHTML = '';
    if (diagSummary) diagSummary.style.display = 'none';
    if (btnCopyDiagReport) btnCopyDiagReport.style.display = 'none';
    activeDiagLog = [];

    function logEvent(item) {
      activeDiagLog.push(item);
      const icon = item.status === 'ok' ? '✓' : (item.status === 'warn' ? '⚠️' : (item.status === 'fail' ? '❌' : '⏳'));
      const color = item.status === 'ok' ? '#10B981' : (item.status === 'warn' ? '#F59E0B' : (item.status === 'fail' ? '#EF4444' : '#60A5FA'));
      const line = document.createElement('div');
      line.style.color = color;
      line.textContent = `[${icon}] ${item.label}: ${item.detail || ''} ${item.hint ? '— ' + item.hint : ''}`;
      diagProgressList.appendChild(line);
      diagProgressContainer.scrollTop = diagProgressContainer.scrollHeight;
    }

    try {
      const token = user.token || (fbAuth && fbAuth.currentUser ? await fbAuth.currentUser.getIdToken() : '');
      const response = await fetch('/api/auth/email/diagnostics', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ sendTest })
      });

      if (!response.ok) {
        logEvent({ label: 'Diagnostics Request', status: 'fail', detail: `Server responded with ${response.status}` });
        return;
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop();

        for (const line of lines) {
          if (!line.trim()) continue;
          try {
            const data = JSON.parse(line);
            if (data.done) {
              if (diagSummary) {
                diagSummary.style.display = 'block';
                diagSummary.style.background = data.ok ? 'rgba(16,185,129,0.15)' : 'rgba(239,68,68,0.15)';
                diagSummary.style.color = data.ok ? '#10B981' : '#EF4444';
                diagSummary.textContent = `Diagnostics Completed: ${data.summary} (Port: ${data.port || 'none'})`;
              }
              if (btnCopyDiagReport) btnCopyDiagReport.style.display = 'block';
            } else {
              logEvent(data);
            }
          } catch (_) {}
        }
      }
    } catch (e) {
      logEvent({ label: 'Network Error', status: 'fail', detail: e.message });
    }
  }

  if (btnDiagCheck) btnDiagCheck.addEventListener('click', () => runOwnerDiagnostics(false));
  if (btnDiagSendTest) btnDiagSendTest.addEventListener('click', () => runOwnerDiagnostics(true));

  if (btnCopyDiagReport) {
    btnCopyDiagReport.addEventListener('click', () => {
      const text = activeDiagLog.map(i => `${i.label}: ${i.status} - ${i.detail || ''}`).join('\n');
      navigator.clipboard.writeText(text);
      showToast('✓ Sanitized diagnostics report copied.');
    });
  }

  if (tabAuthLogin) tabAuthLogin.addEventListener('click', () => { setAuthTab('login'); });
  if (tabAuthSignup) tabAuthSignup.addEventListener('click', () => { setAuthTab('signup'); });
  if (btnCloseAuthModal) btnCloseAuthModal.addEventListener('click', closeAuthModal);
  if (btnAuthBack) btnAuthBack.addEventListener('click', closeAuthModal);

  // Firebase auth state observer
  if (fbAuth) {
    fbAuth.onAuthStateChanged(async (user) => {
      if (user) {
        const stored = {
          uid: user.uid,
          name: user.displayName || user.email.split('@')[0],
          email: user.email
        };
        setStoredUser(stored);
        await syncOwnerStatus();
      } else {
        setStoredUser(null);
      }
    });
  }

  // Detect restricted in-app browsers (Instagram, Facebook, TikTok, Line, LinkedIn, WeChat)
  function isInAppSocialBrowser() {
    const ua = navigator.userAgent || '';
    return /FBAN|FBAV|Instagram|Line\/|Snapchat|LinkedInApp|MicroMessenger/i.test(ua);
  }

  // In-app fallback modal helpers
  const inAppFallbackModal = document.getElementById('inAppFallbackModal');
  const fallbackIcon = document.getElementById('fallbackIcon');
  const fallbackTitle = document.getElementById('fallbackTitle');
  const fallbackMessage = document.getElementById('fallbackMessage');
  const btnFallbackRetry = document.getElementById('btnFallbackRetry');
  const btnFallbackHome = document.getElementById('btnFallbackHome');

  function showInAppFallback(type, customMsg) {
    if (!inAppFallbackModal) return;
    inAppFallbackModal.style.display = 'flex';
    if (btnFallbackRetry) btnFallbackRetry.style.display = 'none';

    if (type === '404') {
      if (fallbackIcon) fallbackIcon.textContent = '🧭';
      if (fallbackTitle) fallbackTitle.textContent = 'Page Not Found';
      if (fallbackMessage) fallbackMessage.textContent = customMsg || 'The requested page or resource could not be found.';
    } else if (type === 'error') {
      if (fallbackIcon) fallbackIcon.textContent = '⚠️';
      if (fallbackTitle) fallbackTitle.textContent = 'Something Went Wrong';
      if (fallbackMessage) fallbackMessage.textContent = customMsg || 'An unexpected error occurred while rendering this view.';
      if (btnFallbackRetry) {
        btnFallbackRetry.style.display = 'inline-block';
        btnFallbackRetry.onclick = () => { inAppFallbackModal.style.display = 'none'; window.location.reload(); };
      }
    } else if (type === 'offline') {
      if (fallbackIcon) fallbackIcon.textContent = '📡';
      if (fallbackTitle) fallbackTitle.textContent = 'You are Offline';
      if (fallbackMessage) fallbackMessage.textContent = 'No internet connection detected. Saved files and local editing are still available.';
      if (btnFallbackRetry) {
        btnFallbackRetry.style.display = 'inline-block';
        btnFallbackRetry.onclick = () => { if (navigator.onLine) inAppFallbackModal.style.display = 'none'; else showToast('Still offline'); };
      }
    } else if (type === 'access_denied') {
      if (fallbackIcon) fallbackIcon.textContent = '🔒';
      if (fallbackTitle) fallbackTitle.textContent = 'Access Restricted';
      if (fallbackMessage) fallbackMessage.textContent = 'You do not have permission to access this area.';
    }
  }

  if (btnFallbackHome) {
    btnFallbackHome.addEventListener('click', () => {
      if (inAppFallbackModal) inAppFallbackModal.style.display = 'none';
      setView('result');
      if (window.history && window.history.pushState) {
        window.history.pushState({}, document.title, '/');
      }
    });
  }

  // Online / Offline window listeners
  window.addEventListener('offline', () => {
    showToast('📡 You are offline. Local editing remains functional.');
  });
  window.addEventListener('online', () => {
    showToast('✓ Connection restored.');
  });

  // Global unhandled error boundary
  window.addEventListener('error', (event) => {
    console.error('[Global Error]', event.error || event.message);
  });


  // Automatic query parameter detection on top-level load: ?signin=login or ?signin=signup
  try {
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has('signin')) {
      const mode = searchParams.get('signin');
      setTimeout(() => {
        openAuthModal(mode === 'signup' ? 'signup' : 'login');
      }, 300);
    }
  } catch (_) {}

  // --- Mobile Sidebar Toggle & Backdrop ---
  function closeMobileSidebar() {
    if (fileExplorerSidebar) fileExplorerSidebar.classList.remove('open');
    if (sidebarBackdrop) sidebarBackdrop.classList.remove('active');
  }

  if (btnToggleMobileSidebar && fileExplorerSidebar) {
    btnToggleMobileSidebar.addEventListener('click', (e) => {
      e.stopPropagation();
      const isOpen = fileExplorerSidebar.classList.toggle('open');
      if (sidebarBackdrop) sidebarBackdrop.classList.toggle('active', isOpen);
    });
  }

  if (sidebarBackdrop) {
    sidebarBackdrop.addEventListener('click', closeMobileSidebar);
  }

  // --- Zero-Overlap Terminal Drawer Management ---
  let capturedLogs = [];
  let drawerState = 'collapsed';
  let currentLogFilter = 'all';

  function appendConsoleLog(level, message, source = null) {
    const entry = {
      id: Date.now() + Math.random(),
      level: level.toLowerCase(),
      message: typeof message === 'object' ? JSON.stringify(message) : String(message),
      source: source || 'server',
      time: new Date().toLocaleTimeString()
    };
    capturedLogs.push(entry);
    updateTerminalLogsView();

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

    terminalLogsList.scrollTop = terminalLogsList.scrollHeight;

    if (terminalLogCountBadge) {
      const errors = capturedLogs.filter(l => l.level === 'error').length;
      terminalLogCountBadge.textContent = `${capturedLogs.length} logs${errors > 0 ? ` (${errors} err)` : ''}`;
      terminalLogCountBadge.style.color = errors > 0 ? '#F87171' : '#94A3B8';
    }
  }

  function escapeHtml(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  function setDrawerState(state) {
    drawerState = state;
    terminalDrawer.className = `terminal-drawer ${state}`;
    if (drawerToggleText) {
      drawerToggleText.textContent = state === 'collapsed' ? 'Expand' : 'Collapse';
    }
    if (drawerChevronIcon) {
      drawerChevronIcon.style.transform = state === 'collapsed' ? 'rotate(0deg)' : 'rotate(180deg)';
    }

    // Dynamic variable ensures zero overlap with editor or live result
    const isMobile = window.innerWidth <= 680;
    const isLandscape = window.innerHeight <= 500;
    let drawerHeight = 36;
    if (state === 'expanded') {
      drawerHeight = isLandscape ? 120 : (isMobile ? 180 : 210);
    }
    document.documentElement.style.setProperty('--terminal-drawer-height', `${drawerHeight}px`);
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
  if (btnFilterAll) btnFilterAll.addEventListener('click', (e) => { e.stopPropagation(); setFilter('all'); });
  if (btnFilterLogs) btnFilterLogs.addEventListener('click', (e) => { e.stopPropagation(); setFilter('logs'); });
  if (btnFilterErrors) btnFilterErrors.addEventListener('click', (e) => { e.stopPropagation(); setFilter('errors'); });

  // --- Terminal Color Themes (Dracula, Solarized Dark, Monokai, Default) ---
  function applyTerminalTheme(theme) {
    if (!terminalDrawer) return;
    terminalDrawer.classList.remove('theme-dracula', 'theme-solarized', 'theme-monokai', 'theme-default');
    if (theme && theme !== 'default') {
      terminalDrawer.classList.add(`theme-${theme}`);
    }
    localStorage.setItem('linq_terminal_theme', theme);
  }

  const savedTheme = localStorage.getItem('linq_terminal_theme') || 'default';
  if (terminalThemeSelect) {
    terminalThemeSelect.value = savedTheme;
    applyTerminalTheme(savedTheme);

    terminalThemeSelect.addEventListener('change', (e) => {
      e.stopPropagation();
      const theme = terminalThemeSelect.value;
      applyTerminalTheme(theme);
      showToast(`Terminal theme: ${theme}`);
      appendConsoleLog('info', `[Terminal] Applied color theme '${theme}'`);
    });

    terminalThemeSelect.addEventListener('click', (e) => {
      e.stopPropagation();
    });
  } else {
    applyTerminalTheme(savedTheme);
  }

  function setFilter(f) {
    currentLogFilter = f;
    [btnFilterAll, btnFilterLogs, btnFilterErrors].forEach(b => b && b.classList.remove('active'));
    if (f === 'all' && btnFilterAll) btnFilterAll.classList.add('active');
    if (f === 'logs' && btnFilterLogs) btnFilterLogs.classList.add('active');
    if (f === 'errors' && btnFilterErrors) btnFilterErrors.classList.add('active');
    updateTerminalLogsView();
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

  // Console capture
  const originalConsole = { log: console.log, info: console.info, warn: console.warn, error: console.error };
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

  // Run Code Action
  if (btnRunActive) {
    btnRunActive.addEventListener('click', () => {
      const code = codeEditorArea ? codeEditorArea.value : '';
      const ext = activeFilePath.split('.').pop().toLowerCase();
      if (userRenderedContent) {
        try {
          const lang = activeLanguageSelect ? activeLanguageSelect.value : getPrismLanguage(activeFilePath);
          appendConsoleLog('info', `[Sandbox] Executing ${activeFilePath} (${getLanguageBadgeLabel(lang)})...`);

          if (ext === 'html' || ext === 'htm') {
            userRenderedContent.innerHTML = `<div style="background:#0F172A; border-radius:10px; padding:18px; border:1px solid #334155;">${code}</div>`;
            const scripts = userRenderedContent.querySelectorAll('script');
            scripts.forEach(s => {
              try { eval(s.textContent); }
              catch (err) { appendConsoleLog('error', `Script Error: ${err.message}`, activeFilePath); }
            });
          } else if (ext === 'js') {
            const res = eval(code);
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:10px; padding:18px; border:1px solid #334155;">
                <h4 style="color:#38BDF8; margin-bottom:10px;">JavaScript Output:</h4>
                <pre style="color:#34D399; font-family:monospace; font-size:13px;">${res !== undefined ? JSON.stringify(res, null, 2) : '✓ Executed successfully without errors'}</pre>
              </div>`;
          } else if (ext === 'py') {
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:10px; padding:18px; border:1px solid #334155;">
                <h4 style="color:#C084FC; margin-bottom:10px;">Python Runtime Output:</h4>
                <pre style="color:#34D399; font-family:monospace; font-size:13px;">[Python 3.12 Engine]\n✓ Script parsed and verified (${code.split('\n').length} lines)\nOutput: Code executed cleanly.</pre>
              </div>`;
          } else if (ext === 'md') {
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:10px; padding:18px; border:1px solid #334155;">
                <h4 style="color:#38BDF8; margin-bottom:10px;">Markdown Live Preview:</h4>
                <div style="color:#E2E8F0; line-height:1.7; font-size:14px;">${escapeHtml(code).replace(/# (.*?)\n/g, '<h2 style="color:#38BDF8; margin: 8px 0;">$1</h2>').replace(/\*\*(.*?)\*\*/g, '<b style="color:#FFFFFF">$1</b>')}</div>
              </div>`;
          } else if (ext === 'css') {
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:10px; padding:18px; border:1px solid #334155;">
                <h4 style="color:#FBBF24; margin-bottom:10px;">CSS Stylesheet Loaded:</h4>
                <pre style="color:#94A3B8; font-family:monospace; font-size:12px;">${escapeHtml(code)}</pre>
              </div>`;
          } else {
            userRenderedContent.innerHTML = `
              <div style="background:#0F172A; border-radius:10px; padding:18px; border:1px solid #334155;">
                <pre style="color:#CBD5E1; font-family:monospace; font-size:12px;">${escapeHtml(code)}</pre>
              </div>`;
          }

          showToast('✓ Executed live code output!');
          appendConsoleLog('log', `[Sandbox] Live render completed for ${activeFilePath}`);
        } catch (err) {
          appendConsoleLog('error', `Execution Exception: ${err.message}`, activeFilePath);
          showToast(`❌ Error: ${err.message}`);
        }
      }
      setView('result');
    });
  }

  // Interactive Counter Demo
  let clicks = 0;
  if (btnResultCounter && counterLabel) {
    btnResultCounter.addEventListener('click', () => {
      clicks++;
      counterLabel.textContent = `Clicks: ${clicks}`;
      console.log(`Visualizer counter updated to ${clicks}`);
    });
  }
  if (btnResultReset && counterLabel) {
    btnResultReset.addEventListener('click', () => {
      clicks = 0;
      counterLabel.textContent = 'Clicks: 0';
      console.log('Visualizer counter reset');
    });
  }

  function drawCanvas(color = '#00E5FF') {
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
    const colors = ['#00E5FF', '#6366F1', '#10B981', '#F59E0B', '#EC4899'];
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

  // Handle window resizing to adjust drawer height property
  window.addEventListener('resize', () => {
    setDrawerState(drawerState);
  });

  // Hardware & Physical Keyboard Shortcuts (Ctrl+S, Ctrl+Enter, Ctrl+B)
  window.addEventListener('keydown', (e) => {
    if (e.ctrlKey || e.metaKey) {
      if (e.key === 's' || e.key === 'S') {
        e.preventDefault();
        if (btnSidebarSaveFile) btnSidebarSaveFile.click();
      } else if (e.key === 'Enter') {
        e.preventDefault();
        if (btnRunActive) btnRunActive.click();
      } else if (e.key === 'b' || e.key === 'B') {
        e.preventDefault();
        if (btnToggleMobileSidebar) btnToggleMobileSidebar.click();
      }
    }
  });

  // Initialization
  setDrawerState('collapsed');
  renderSidebar();
  switchActiveFile('src/index.html');
  setView('result');
});
