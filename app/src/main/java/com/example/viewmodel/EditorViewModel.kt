package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autocomplete.AutocompleteEngine
import com.example.autocomplete.AutocompleteSuggestion
import com.example.format.PrettierFormatter
import com.example.git.GitManager
import com.example.linter.CodeLinter
import com.example.linter.LintIssue
import com.example.model.*
import com.example.plugin.PluginItem
import com.example.plugin.PluginManager
import com.example.server.NanoHttpDevServer
import com.example.server.ServerStatus
import com.example.storage.FileRepository
import com.example.storage.FileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class EditorUiState(
    val activeNavTab: Int = 0,
    val openFiles: List<CodeFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val currentDirectory: File,
    val directoryFiles: List<FileItem> = emptyList(),
    val serverStatus: ServerStatus = ServerStatus(),
    val previewUrl: String? = null,
    val previewHtml: String? = null,
    val userAccount: UserAccount = UserAccount(),
    val plugins: List<PluginItem> = emptyList(),
    val showPluginSheet: Boolean = false,
    val executionResult: com.example.runner.ExecutionResult? = null,
    val showOutputConsole: Boolean = false,
    val isFullScreen: Boolean = false,
    val isZenMode: Boolean = false,
    val isMinimapVisible: Boolean = true,
    val isExplorerSidebarOpen: Boolean = false,
    val isTemplatesPanelOpen: Boolean = false,
    val isSearchReplaceOpen: Boolean = false,
    val lintIssues: List<LintIssue> = emptyList(),
    val gitStatus: GitStatus = GitStatus(),
    val autocompleteSuggestions: List<AutocompleteSuggestion> = emptyList(),
    val prettierFormatNotice: String? = null,
    val isTerminalDrawerExpanded: Boolean = false,
    val showTerminalDrawer: Boolean = false,
    val capturedSandboxLogs: List<CapturedLog> = listOf(
        CapturedLog(level = "INFO", message = "Debugger ready. Console logs and errors captured from userRenderedContent sandbox will appear here.", source = "userRenderedContent")
    ),
    val terminalLines: List<TerminalLine> = listOf(
        TerminalLine("Welcome to Linq Studio Terminal! Type 'help' for available commands.")
    ),
    val currentTheme: EditorTheme = EditorTheme.LinqDark
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    val fileRepository: FileRepository = FileRepositoryImpl(application)
    val pluginManager: PluginManager = PluginManager(application)
    val gitManager: GitManager = GitManager(application, fileRepository.internalWorkspaceDir)
    private val devServer = NanoHttpDevServer()
    private val prefs = application.getSharedPreferences("linq_user_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        EditorUiState(
            currentDirectory = fileRepository.internalWorkspaceDir,
            directoryFiles = fileRepository.listFiles(fileRepository.internalWorkspaceDir)
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadInitialStarterFiles()
        refreshGitStatus()

        viewModelScope.launch {
            pluginManager.plugins.collect { list ->
                _uiState.value = _uiState.value.copy(plugins = list)
            }
        }
    }

    private fun loadUserProfile() {
        val name = prefs.getString("user_name", "Developer") ?: "Developer"
        val title = prefs.getString("user_title", "Mobile Developer") ?: "Mobile Developer"
        val avatarId = prefs.getString("user_avatar_id", "avatar_terminal") ?: "avatar_terminal"
        val photoUri = prefs.getString("user_photo_uri", null)

        _uiState.value = _uiState.value.copy(
            userAccount = UserAccount(
                name = name,
                customTitle = title,
                avatarId = avatarId,
                avatarCustomUri = photoUri
            )
        )
    }

    fun updateUserProfile(username: String, customTitle: String, avatarId: String, photoUri: String?) {
        prefs.edit()
            .putString("user_name", username)
            .putString("user_title", customTitle)
            .putString("user_avatar_id", avatarId)
            .putString("user_photo_uri", photoUri)
            .apply()

        _uiState.value = _uiState.value.copy(
            userAccount = UserAccount(
                name = username,
                customTitle = customTitle,
                avatarId = avatarId,
                avatarCustomUri = photoUri
            )
        )
    }

    private fun loadInitialStarterFiles() {
        val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
        val indexHtml = File(webDir, "index.html")
        if (indexHtml.exists()) {
            openFile(indexHtml)
        }
    }

    fun setNavTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeNavTab = index)
    }

    fun selectTheme(theme: EditorTheme) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
    }

    fun togglePluginSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPluginSheet = show)
    }

    fun toggleFullScreen() {
        _uiState.value = _uiState.value.copy(isFullScreen = !_uiState.value.isFullScreen)
    }

    fun setFullScreen(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isFullScreen = enabled)
    }

    fun toggleZenMode() {
        val next = !_uiState.value.isZenMode
        _uiState.value = _uiState.value.copy(
            isZenMode = next,
            isFullScreen = if (next) true else _uiState.value.isFullScreen
        )
    }

    fun setZenMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isZenMode = enabled,
            isFullScreen = if (enabled) true else _uiState.value.isFullScreen
        )
    }

    fun toggleMinimap() {
        _uiState.value = _uiState.value.copy(isMinimapVisible = !_uiState.value.isMinimapVisible)
    }

    fun setMinimapVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isMinimapVisible = visible)
    }

    // --- File Explorer & Templates Sidebar Actions ---

    fun toggleExplorerSidebar() {
        val next = !_uiState.value.isExplorerSidebarOpen
        _uiState.value = _uiState.value.copy(
            isExplorerSidebarOpen = next,
            isTemplatesPanelOpen = if (next) false else _uiState.value.isTemplatesPanelOpen
        )
    }

    fun setExplorerSidebarOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(
            isExplorerSidebarOpen = isOpen,
            isTemplatesPanelOpen = if (isOpen) false else _uiState.value.isTemplatesPanelOpen
        )
    }

    fun toggleTemplatesPanel() {
        val next = !_uiState.value.isTemplatesPanelOpen
        _uiState.value = _uiState.value.copy(
            isTemplatesPanelOpen = next,
            isExplorerSidebarOpen = if (next) false else _uiState.value.isExplorerSidebarOpen
        )
    }

    fun setTemplatesPanelOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(
            isTemplatesPanelOpen = isOpen,
            isExplorerSidebarOpen = if (isOpen) false else _uiState.value.isExplorerSidebarOpen
        )
    }

    fun toggleSearchReplace() {
        _uiState.value = _uiState.value.copy(isSearchReplaceOpen = !_uiState.value.isSearchReplaceOpen)
    }

    fun setSearchReplaceOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchReplaceOpen = isOpen)
    }

    fun insertTemplateSnippet(snippet: String) {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex)
        if (active != null) {
            updateActiveFileContent(snippet)
        } else {
            openNewTab("index.html", snippet)
        }
        setTemplatesPanelOpen(false)
    }

    fun createFileFromTemplate(fileName: String, content: String) {
        val target = File(_uiState.value.currentDirectory, fileName)
        fileRepository.writeFile(target, content)
        loadDirectory(_uiState.value.currentDirectory)
        openFile(target)
        setTemplatesPanelOpen(false)
    }

    // --- Expandable Terminal Drawer & Sandbox Logger Actions ---

    fun toggleTerminalDrawer() {
        val currentShow = _uiState.value.showTerminalDrawer
        _uiState.value = _uiState.value.copy(
            showTerminalDrawer = !currentShow,
            isTerminalDrawerExpanded = if (!currentShow) true else _uiState.value.isTerminalDrawerExpanded
        )
    }

    fun setTerminalDrawerExpanded(expanded: Boolean) {
        _uiState.value = _uiState.value.copy(isTerminalDrawerExpanded = expanded)
    }

    fun setShowTerminalDrawer(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTerminalDrawer = show)
    }

    fun addSandboxConsoleLog(level: String, message: String, source: String? = null, line: Int? = null) {
        val entry = CapturedLog(
            level = level.uppercase(),
            message = message,
            source = source,
            lineNumber = line
        )
        val updated = _uiState.value.capturedSandboxLogs + entry
        val terminalLines = _uiState.value.terminalLines.toMutableList()
        val prefix = if (source != null) "[$level] $source:" else "[$level]"
        terminalLines.add(TerminalLine("$prefix $message", isError = level.equals("ERROR", ignoreCase = true)))

        _uiState.value = _uiState.value.copy(
            capturedSandboxLogs = updated,
            terminalLines = terminalLines,
            showTerminalDrawer = if (level.equals("ERROR", ignoreCase = true)) true else _uiState.value.showTerminalDrawer
        )
    }

    fun clearSandboxLogs() {
        _uiState.value = _uiState.value.copy(capturedSandboxLogs = emptyList())
    }

    // --- Tab & File Management System ---

    fun openFile(file: File) {
        val existingIndex = _uiState.value.openFiles.indexOfFirst { it.file.absolutePath == file.absolutePath }
        if (existingIndex != -1) {
            _uiState.value = _uiState.value.copy(activeFileIndex = existingIndex)
            recomputeLinterForIndex(existingIndex)
            return
        }

        val content = fileRepository.readFile(file)
        val newCodeFile = CodeFile(file = file, content = content)
        val updatedList = _uiState.value.openFiles + newCodeFile
        val newIndex = updatedList.lastIndex
        _uiState.value = _uiState.value.copy(
            openFiles = updatedList,
            activeFileIndex = newIndex
        )
        recomputeLinterForIndex(newIndex)
    }

    fun selectTab(index: Int) {
        if (index in _uiState.value.openFiles.indices) {
            _uiState.value = _uiState.value.copy(activeFileIndex = index)
            recomputeLinterForIndex(index)
        }
    }

    fun closeTab(index: Int) {
        val list = _uiState.value.openFiles.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            val newIdx = (index - 1).coerceAtLeast(0)
            _uiState.value = _uiState.value.copy(
                openFiles = list,
                activeFileIndex = if (list.isEmpty()) 0 else newIdx
            )
            recomputeLinterForIndex(newIdx)
        }
    }

    fun closeAllTabs() {
        _uiState.value = _uiState.value.copy(
            openFiles = emptyList(),
            activeFileIndex = 0,
            lintIssues = emptyList()
        )
    }

    fun closeOtherTabs(index: Int) {
        val files = _uiState.value.openFiles
        if (index in files.indices) {
            val kept = listOf(files[index])
            _uiState.value = _uiState.value.copy(
                openFiles = kept,
                activeFileIndex = 0
            )
            recomputeLinterForIndex(0)
        }
    }

    fun saveAllTabs() {
        val updated = _uiState.value.openFiles.map { codeFile ->
            if (codeFile.isModified) {
                fileRepository.writeFile(codeFile.file, codeFile.content)
                codeFile.copy(isModified = false)
            } else {
                codeFile
            }
        }
        _uiState.value = _uiState.value.copy(openFiles = updated)
    }

    fun newTab() {
        openNewTab("Untitled_${System.currentTimeMillis()}.html", "<!DOCTYPE html>\n<html>\n<body>\n  <h1>New Linq File</h1>\n</body>\n</html>")
    }

    fun openNewTab(fileName: String, initialContent: String) {
        val newFile = File(fileRepository.internalWorkspaceDir, fileName)
        fileRepository.writeFile(newFile, initialContent)
        openFile(newFile)
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun openFileByName(name: String) {
        val target = File(fileRepository.internalWorkspaceDir, name)
        if (target.exists()) {
            openFile(target)
        } else {
            val inWeb = File(File(fileRepository.internalWorkspaceDir, "WebStarter"), name)
            if (inWeb.exists()) openFile(inWeb)
        }
    }

    fun updateActiveFileContent(newContent: String, cursorPosition: Int = newContent.length) {
        val activeIdx = _uiState.value.activeFileIndex
        val list = _uiState.value.openFiles.toMutableList()
        if (activeIdx in list.indices) {
            val oldFile = list[activeIdx]
            list[activeIdx] = oldFile.copy(content = newContent, isModified = true)
            val lang = com.example.runner.CodeExecutionEngine.detectLanguage(oldFile.name)
            val issues = CodeLinter.lint(newContent, lang)
            val suggestions = AutocompleteEngine.getSuggestions(newContent, cursorPosition, lang)
            _uiState.value = _uiState.value.copy(
                openFiles = list,
                lintIssues = issues,
                autocompleteSuggestions = suggestions
            )
            refreshGitStatus()
        }
    }

    fun acceptSuggestion(suggestion: AutocompleteSuggestion) {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val content = active.content
        val textToInsert = suggestion.insertText
        val updated = if (content.isEmpty()) {
            textToInsert
        } else {
            val lastWordRegex = Regex("[a-zA-Z0-9_$.<\\-]+$")
            val match = lastWordRegex.find(content)
            if (match != null && (suggestion.label.startsWith(match.value, ignoreCase = true) || suggestion.insertText.startsWith(match.value, ignoreCase = true))) {
                content.substring(0, match.range.first) + textToInsert
            } else {
                content + textToInsert
            }
        }
        updateActiveFileContent(updated)
        _uiState.value = _uiState.value.copy(autocompleteSuggestions = emptyList())
    }

    fun dismissAutocomplete() {
        _uiState.value = _uiState.value.copy(autocompleteSuggestions = emptyList())
    }

    private fun recomputeLinterForIndex(index: Int) {
        val file = _uiState.value.openFiles.getOrNull(index)
        if (file != null) {
            val lang = com.example.runner.CodeExecutionEngine.detectLanguage(file.name)
            val issues = CodeLinter.lint(file.content, lang)
            _uiState.value = _uiState.value.copy(lintIssues = issues)
        } else {
            _uiState.value = _uiState.value.copy(lintIssues = emptyList())
        }
    }

    fun saveActiveFile() {
        val activeIdx = _uiState.value.activeFileIndex
        val list = _uiState.value.openFiles.toMutableList()
        if (activeIdx in list.indices) {
            val file = list[activeIdx]
            fileRepository.writeFile(file.file, file.content)
            list[activeIdx] = file.copy(isModified = false)
            _uiState.value = _uiState.value.copy(openFiles = list)
            refreshGitStatus()
        }
    }

    // --- Git Source Control Operations ---

    fun refreshGitStatus() {
        val status = gitManager.computeStatus(_uiState.value.directoryFiles, _uiState.value.openFiles)
        _uiState.value = _uiState.value.copy(gitStatus = status)
    }

    fun stageGitFile(relPath: String) {
        gitManager.stageFile(relPath)
        refreshGitStatus()
    }

    fun stageAllGitFiles() {
        gitManager.stageAll(_uiState.value.gitStatus.unstagedFiles)
        refreshGitStatus()
    }

    fun unstageGitFile(relPath: String) {
        gitManager.unstageFile(relPath)
        refreshGitStatus()
    }

    fun unstageAllGitFiles() {
        gitManager.unstageAll()
        refreshGitStatus()
    }

    fun commitGitChanges(message: String) {
        val author = "${_uiState.value.userAccount.name} <dev@linqstudio.local>"
        val commit = gitManager.commit(message, author)
        refreshGitStatus()
        if (commit != null) {
            val lines = _uiState.value.terminalLines.toMutableList()
            lines.add(TerminalLine("[git commit ${commit.id}] ${commit.message} (${commit.fileCount} files)"))
            _uiState.value = _uiState.value.copy(terminalLines = lines)
        }
    }

    fun pushGitChanges() {
        val result = gitManager.push()
        refreshGitStatus()
        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("[git push] $result"))
        _uiState.value = _uiState.value.copy(terminalLines = lines)
    }

    fun createGitBranch(name: String) {
        gitManager.createBranch(name)
        refreshGitStatus()
    }

    fun switchGitBranch(name: String) {
        gitManager.switchBranch(name)
        refreshGitStatus()
    }

    // --- Directory Management ---

    fun loadDirectory(dir: File) {
        _uiState.value = _uiState.value.copy(
            currentDirectory = dir,
            directoryFiles = fileRepository.listFiles(dir)
        )
    }

    fun navigateUp() {
        val parent = _uiState.value.currentDirectory.parentFile
        if (parent != null && parent.exists()) {
            loadDirectory(parent)
        }
    }

    fun createNewFileInExplorer(name: String) {
        val created = fileRepository.createFile(_uiState.value.currentDirectory, name)
        if (created != null) {
            loadDirectory(_uiState.value.currentDirectory)
            openFile(created)
        }
    }

    fun createNewFolderInExplorer(name: String) {
        fileRepository.createFolder(_uiState.value.currentDirectory, name)
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun deleteFileInExplorer(file: File) {
        fileRepository.delete(file)
        loadDirectory(_uiState.value.currentDirectory)
    }

    // --- Dev Server & Web Preview ---

    fun startDevServer() {
        val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
        if (!webDir.exists()) webDir.mkdirs()
        devServer.start(webDir, 8080)
        _uiState.value = _uiState.value.copy(serverStatus = devServer.status)
    }

    fun stopDevServer() {
        devServer.stop()
        _uiState.value = _uiState.value.copy(serverStatus = devServer.status)
    }

    fun openWebPreviewForActiveFile() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex)
        val htmlContent = if (active != null && (active.name.endsWith(".html", ignoreCase = true) || active.name.endsWith(".htm", ignoreCase = true))) {
            active.content
        } else {
            val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
            val indexFile = File(webDir, "index.html")
            if (indexFile.exists()) indexFile.readText() else "<!DOCTYPE html><html><body><h1>Linq Studio Preview</h1></body></html>"
        }

        val injectedHtml = injectDevConsoleLogger(htmlContent)

        _uiState.value = _uiState.value.copy(
            previewHtml = injectedHtml,
            previewUrl = "file://${fileRepository.internalWorkspaceDir.absolutePath}/WebStarter/index.html"
        )
    }

    private fun injectDevConsoleLogger(originalHtml: String): String {
        val script = """
        <script>
        (function() {
            var origLog = console.log;
            var origWarn = console.warn;
            var origError = console.error;
            var origInfo = console.info;

            function safeString(arg) {
                try {
                    if (typeof arg === 'object') return JSON.stringify(arg);
                    return String(arg);
                } catch(e) { return String(arg); }
            }

            console.log = function() {
                origLog.apply(console, arguments);
                var msg = Array.from(arguments).map(safeString).join(' ');
                if (window.AndroidLogger) { window.AndroidLogger.postMessage(JSON.stringify({level: 'LOG', msg: msg, src: 'userRenderedContent'})); }
            };
            console.warn = function() {
                origWarn.apply(console, arguments);
                var msg = Array.from(arguments).map(safeString).join(' ');
                if (window.AndroidLogger) { window.AndroidLogger.postMessage(JSON.stringify({level: 'WARN', msg: msg, src: 'userRenderedContent'})); }
            };
            console.error = function() {
                origError.apply(console, arguments);
                var msg = Array.from(arguments).map(safeString).join(' ');
                if (window.AndroidLogger) { window.AndroidLogger.postMessage(JSON.stringify({level: 'ERROR', msg: msg, src: 'userRenderedContent'})); }
            };
            console.info = function() {
                origInfo.apply(console, arguments);
                var msg = Array.from(arguments).map(safeString).join(' ');
                if (window.AndroidLogger) { window.AndroidLogger.postMessage(JSON.stringify({level: 'INFO', msg: msg, src: 'userRenderedContent'})); }
            };

            window.addEventListener('error', function(e) {
                if (window.AndroidLogger) {
                    window.AndroidLogger.postMessage(JSON.stringify({
                        level: 'ERROR',
                        msg: e.message + ' at ' + (e.filename ? e.filename.split('/').pop() : '') + ':' + e.lineno,
                        line: e.lineno,
                        src: 'userRenderedContent'
                    }));
                }
            });
        })();
        </script>
        """.trimIndent()

        return if (originalHtml.contains("<head>", ignoreCase = true)) {
            originalHtml.replace("<head>", "<head>\n$script", ignoreCase = true)
        } else if (originalHtml.contains("<html>", ignoreCase = true)) {
            originalHtml.replace("<html>", "<html>\n<head>$script</head>", ignoreCase = true)
        } else {
            "$script\n$originalHtml"
        }
    }

    fun dismissWebPreview() {
        _uiState.value = _uiState.value.copy(previewHtml = null, previewUrl = null)
    }

    // --- Multi-Language Code Execution ---

    fun runCode() {
        runActiveFile()
    }

    fun runActiveFile() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val lang = com.example.runner.CodeExecutionEngine.detectLanguage(active.name)

        if (lang == "HTML / Web") {
            openWebPreviewForActiveFile()
            return
        }

        val dir = active.file.parentFile ?: fileRepository.internalWorkspaceDir
        val result = com.example.runner.CodeExecutionEngine.execute(active.content, active.name, dir)

        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("--- [Run] ${active.name} ($lang) ---", isCommand = true))
        if (result.stdout.isNotEmpty()) {
            result.stdout.lines().forEach { if (it.isNotEmpty()) lines.add(TerminalLine(it)) }
        }
        if (result.stderr.isNotEmpty()) {
            result.stderr.lines().forEach { if (it.isNotEmpty()) lines.add(TerminalLine(it, isError = true)) }
        }
        lines.add(TerminalLine("[Finished with exit code ${result.exitCode} in ${result.executionTimeMs}ms]"))

        _uiState.value = _uiState.value.copy(
            executionResult = result,
            showOutputConsole = true,
            terminalLines = lines
        )
    }

    fun closeOutputConsole() {
        _uiState.value = _uiState.value.copy(showOutputConsole = false)
    }

    // --- Terminal Actions ---

    fun sendTerminalCommand(cmd: String) {
        val trimmedCmd = cmd.trim()
        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("$ $trimmedCmd", isCommand = true))

        val parts = trimmedCmd.split(" ")
        val firstWord = parts.firstOrNull()?.lowercase() ?: ""
        val targetArg = parts.drop(1).joinToString(" ").trim()

        when (firstWord) {
            "help" -> {
                lines.add(TerminalLine("=== Linq Studio Shell & Runner Commands ==="))
                lines.add(TerminalLine("  run [file]    - Execute file (Python, JS, Shell, C, Java, etc.)"))
                lines.add(TerminalLine("  python <file> - Run Python script (or 'py <file>')"))
                lines.add(TerminalLine("  node <file>   - Run JavaScript program (or 'js <file>')"))
                lines.add(TerminalLine("  bash <file>   - Run Bash / Shell script (or 'sh <file>')"))
                lines.add(TerminalLine("  languages     - List supported programming languages"))
                lines.add(TerminalLine("  templates     - Open Boilerplate templates panel"))
                lines.add(TerminalLine("  lint          - Check current file for syntax issues"))
                lines.add(TerminalLine("  ls            - List files in current directory"))
                lines.add(TerminalLine("  cat <file>    - Print file contents to terminal"))
                lines.add(TerminalLine("  pwd           - Print current working directory"))
                lines.add(TerminalLine("  echo <msg>    - Print message"))
                lines.add(TerminalLine("  server        - Check HTTP server status"))
                lines.add(TerminalLine("  plugins       - List installed extensions"))
                lines.add(TerminalLine("  clear         - Clear terminal screen"))
            }
            "templates" -> {
                toggleTemplatesPanel()
                lines.add(TerminalLine("Opened Boilerplate Templates panel"))
            }
            "lint" -> {
                val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex)
                if (active != null) {
                    val lang = com.example.runner.CodeExecutionEngine.detectLanguage(active.name)
                    val issues = CodeLinter.lint(active.content, lang)
                    if (issues.isEmpty()) {
                        lines.add(TerminalLine("✓ No syntax issues found in ${active.name} ($lang)"))
                    } else {
                        lines.add(TerminalLine("Found ${issues.size} syntax issue(s) in ${active.name}:", isError = true))
                        issues.forEach { issue ->
                            lines.add(TerminalLine("  Line ${issue.line}: ${issue.message} [${issue.rule}]", isError = issue.severity == com.example.linter.LintSeverity.ERROR))
                        }
                    }
                } else {
                    lines.add(TerminalLine("No file currently open in editor", isError = true))
                }
            }
            "languages" -> {
                lines.add(TerminalLine("Supported Programming Languages & Environments:"))
                lines.add(TerminalLine("• Python (.py, .pyw) - Embedded Python 3.11 engine"))
                lines.add(TerminalLine("• JavaScript (.js) & TypeScript (.ts) - V8/Console runtime"))
                lines.add(TerminalLine("• Shell / Bash (.sh, .bash) - Unix automation runner"))
                lines.add(TerminalLine("• Kotlin (.kt) & Java (.java) - JVM runner"))
                lines.add(TerminalLine("• C (.c) & C++ (.cpp) - GCC simulation engine"))
                lines.add(TerminalLine("• Rust (.rs) & Go (.go) - Native compiler runner"))
                lines.add(TerminalLine("• SQL (.sql) - SQLite/ANSI query engine"))
                lines.add(TerminalLine("• PHP (.php) - PHP 8.3 CLI runner"))
                lines.add(TerminalLine("• HTML / CSS / Web (.html, .css) - Live Dev Server & DOM inspection"))
                lines.add(TerminalLine("• JSON (.json) & Markdown (.md) - Structural validator & renderer"))
            }
            "run", "python", "py", "python3", "node", "js", "bash", "sh" -> {
                val fileNameToRun = if (targetArg.isNotEmpty()) {
                    targetArg
                } else {
                    _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex)?.name ?: ""
                }

                if (fileNameToRun.isEmpty()) {
                    lines.add(TerminalLine("Usage: $firstWord <filename> (or open a file in Editor)", isError = true))
                } else {
                    val file = File(_uiState.value.currentDirectory, fileNameToRun).let {
                        if (it.exists()) it else File(fileRepository.internalWorkspaceDir, fileNameToRun)
                    }
                    if (file.exists()) {
                        val content = fileRepository.readFile(file)
                        val res = com.example.runner.CodeExecutionEngine.execute(content, file.name, file.parentFile)
                        lines.add(TerminalLine("--- Executing ${file.name} (${res.language}) ---"))
                        if (res.stdout.isNotEmpty()) {
                            res.stdout.lines().forEach { if (it.isNotEmpty()) lines.add(TerminalLine(it)) }
                        }
                        if (res.stderr.isNotEmpty()) {
                            res.stderr.lines().forEach { if (it.isNotEmpty()) lines.add(TerminalLine(it, isError = true)) }
                        }
                        lines.add(TerminalLine("[Process exited with code ${res.exitCode} in ${res.executionTimeMs}ms]"))
                    } else {
                        lines.add(TerminalLine("$firstWord: cannot find '$fileNameToRun'", isError = true))
                    }
                }
            }
            "cat" -> {
                if (targetArg.isEmpty()) {
                    lines.add(TerminalLine("Usage: cat <filename>", isError = true))
                } else {
                    val file = File(_uiState.value.currentDirectory, targetArg).let {
                        if (it.exists()) it else File(fileRepository.internalWorkspaceDir, targetArg)
                    }
                    if (file.exists()) {
                        lines.add(TerminalLine(fileRepository.readFile(file)))
                    } else {
                        lines.add(TerminalLine("cat: $targetArg: No such file or directory", isError = true))
                    }
                }
            }
            "pwd" -> {
                lines.add(TerminalLine(_uiState.value.currentDirectory.absolutePath))
            }
            "ls" -> {
                val files = fileRepository.listFiles(_uiState.value.currentDirectory)
                for (f in files) {
                    lines.add(TerminalLine(if (f.isDirectory) "${f.name}/" else f.name))
                }
            }
            "server" -> {
                lines.add(TerminalLine("Server Running: ${devServer.status.isRunning} (Port: ${devServer.status.portOrNull ?: 8080})"))
            }
            "plugins" -> {
                for (p in _uiState.value.plugins.filter { it.isInstalled }) {
                    lines.add(TerminalLine("• ${p.name} (v${p.version}) - ${if (p.isEnabled) "ACTIVE" else "DISABLED"}"))
                }
            }
            "clear" -> {
                lines.clear()
            }
            "echo" -> {
                lines.add(TerminalLine(trimmedCmd.removePrefix("echo").trim().removeSurrounding("\"")))
            }
            else -> {
                lines.add(TerminalLine("bash: $firstWord: command not found (type 'help' or 'languages' for options)", isError = true))
            }
        }

        _uiState.value = _uiState.value.copy(terminalLines = lines)
    }

    fun clearTerminal() {
        _uiState.value = _uiState.value.copy(terminalLines = emptyList())
    }

    fun openWebDemo() {
        val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
        val indexHtml = File(webDir, "index.html")
        if (indexHtml.exists()) {
            openFile(indexHtml)
        }
        openWebPreviewForActiveFile()
    }

    fun restartDevServer() {
        stopDevServer()
        startDevServer()
    }

    fun closePreview() {
        dismissWebPreview()
    }

    fun formatActiveFileWithPrettier() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val lang = com.example.runner.CodeExecutionEngine.detectLanguage(active.name)
        val formatted = PrettierFormatter.format(active.content, lang)
        updateActiveFileContent(formatted)
        val notice = "✨ Formatted ${active.name} with Prettier style!"
        _uiState.value = _uiState.value.copy(prettierFormatNotice = notice)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_uiState.value.prettierFormatNotice == notice) {
                _uiState.value = _uiState.value.copy(prettierFormatNotice = null)
            }
        }
    }

    fun lintActiveFile() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val lang = com.example.runner.CodeExecutionEngine.detectLanguage(active.name)
        val issues = CodeLinter.lint(active.content, lang)
        _uiState.value = _uiState.value.copy(lintIssues = issues)
    }

    fun showActiveStats() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val linesCount = active.content.lines().size
        val charsCount = active.content.length
        val wordsCount = active.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val terminalLines = _uiState.value.terminalLines.toMutableList()
        terminalLines.add(TerminalLine("--- Stats for ${active.name} ---"))
        terminalLines.add(TerminalLine("Lines: $linesCount | Words: $wordsCount | Characters: $charsCount"))
        _uiState.value = _uiState.value.copy(terminalLines = terminalLines, showTerminalDrawer = true)
    }

    fun showActiveTodos() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val todos = active.content.lines().mapIndexedNotNull { idx, line ->
            if (line.contains("TODO", ignoreCase = true) || line.contains("FIXME", ignoreCase = true)) {
                "Line ${idx + 1}: ${line.trim()}"
            } else null
        }
        val terminalLines = _uiState.value.terminalLines.toMutableList()
        terminalLines.add(TerminalLine("--- TODOs in ${active.name} ---"))
        if (todos.isEmpty()) {
            terminalLines.add(TerminalLine("No TODO items found."))
        } else {
            todos.forEach { terminalLines.add(TerminalLine(it)) }
        }
        _uiState.value = _uiState.value.copy(terminalLines = terminalLines, showTerminalDrawer = true)
    }

    override fun onCleared() {
        super.onCleared()
        devServer.stop()
    }
}
