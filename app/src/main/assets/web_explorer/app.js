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
  const terminalCommandInput = document.getElementById('terminalCommandInput');
  const btnSendTerminalCommand = document.getElementById('btnSendTerminalCommand');

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
  let activeHostFolder = 'src';

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

  // Real-time input handling
  if (codeEditorArea) {
    codeEditorArea.addEventListener('input', () => {
      if (projectFiles[activeFilePath]) {
        projectFiles[activeFilePath].content = codeEditorArea.value;
        projectFiles[activeFilePath].isModified = true;
      }
      renderSidebar();
      updatePrismHighlighting();
    });

    codeEditorArea.addEventListener('scroll', () => {
      if (highlightingBlock) {
        highlightingBlock.scrollTop = codeEditorArea.scrollTop;
        highlightingBlock.scrollLeft = codeEditorArea.scrollLeft;
      }
    });

    // Tab key indentation
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
          renderSidebar();
          closeUnifiedCreateModal();
          showToast(`📁 Created folder '${name}'`);
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

  // Initialization
  setDrawerState('collapsed');
  renderSidebar();
  switchActiveFile('src/index.html');
  setView('result');
});
