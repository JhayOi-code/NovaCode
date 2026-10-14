package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val terminalLines: List<TerminalLine> = listOf(
        TerminalLine("Welcome to Linq Studio Terminal! Type 'help' for available commands.")
    ),
    val currentTheme: EditorTheme = EditorTheme.LinqDark
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    val fileRepository: FileRepository = FileRepositoryImpl(application)
    val pluginManager: PluginManager = PluginManager(application)
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

    // --- File & Editor Management ---

    fun openFile(file: File) {
        val existingIndex = _uiState.value.openFiles.indexOfFirst { it.file.absolutePath == file.absolutePath }
        if (existingIndex != -1) {
            _uiState.value = _uiState.value.copy(activeFileIndex = existingIndex)
            return
        }

        val content = fileRepository.readFile(file)
        val newCodeFile = CodeFile(file = file, content = content)
        val updatedList = _uiState.value.openFiles + newCodeFile
        _uiState.value = _uiState.value.copy(
            openFiles = updatedList,
            activeFileIndex = updatedList.lastIndex
        )
    }

    fun selectTab(index: Int) {
        if (index in _uiState.value.openFiles.indices) {
            _uiState.value = _uiState.value.copy(activeFileIndex = index)
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
        }
    }

    fun newTab() {
        val newFile = File(fileRepository.internalWorkspaceDir, "Untitled_${System.currentTimeMillis()}.html")
        fileRepository.writeFile(newFile, "<!DOCTYPE html>\n<html>\n<body>\n  <h1>New Linq File</h1>\n</body>\n</html>")
        openFile(newFile)
        loadDirectory(_uiState.value.currentDirectory)
    }

    fun updateActiveFileContent(newContent: String) {
        val activeIdx = _uiState.value.activeFileIndex
        val list = _uiState.value.openFiles.toMutableList()
        if (activeIdx in list.indices) {
            list[activeIdx] = list[activeIdx].copy(content = newContent, isModified = true)
            _uiState.value = _uiState.value.copy(openFiles = list)
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
        }
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

    fun restartDevServer() {
        devServer.restart()
        _uiState.value = _uiState.value.copy(serverStatus = devServer.status)
    }

    fun openWebDemo() {
        if (!devServer.status.isRunning) {
            startDevServer()
        }
        val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
        val indexFile = File(webDir, "index.html")
        val content = if (indexFile.exists()) indexFile.readText() else null

        _uiState.value = _uiState.value.copy(
            previewUrl = "http://localhost:8080/index.html",
            previewHtml = content
        )
    }

    fun openWebPreviewForActiveFile() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex)
        if (active != null) {
            if (!devServer.status.isRunning) {
                val dir = active.file.parentFile ?: fileRepository.internalWorkspaceDir
                devServer.start(dir, 8080)
            }
            _uiState.value = _uiState.value.copy(
                previewUrl = "http://localhost:8080/${active.file.name}",
                previewHtml = active.content
            )
        } else {
            openWebDemo()
        }
    }

    fun closePreview() {
        _uiState.value = _uiState.value.copy(previewUrl = null, previewHtml = null)
    }

    // --- Plugin Actions ---

    fun formatActiveFileWithPrettier() {
        val activeIdx = _uiState.value.activeFileIndex
        val list = _uiState.value.openFiles.toMutableList()
        if (activeIdx in list.indices) {
            val file = list[activeIdx]
            val formatted = pluginManager.formatCode(file.content, file.file.extension)
            list[activeIdx] = file.copy(content = formatted, isModified = true)
            _uiState.value = _uiState.value.copy(openFiles = list)
        }
    }

    fun lintActiveFile() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val diagnostics = pluginManager.lintCode(active.content, active.file.extension)
        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("--- ESLint Diagnostics for ${active.name} ---", isCommand = true))
        if (diagnostics.isEmpty()) {
            lines.add(TerminalLine("✓ No lint errors found! Clean code."))
        } else {
            for (diag in diagnostics) {
                lines.add(TerminalLine("L${diag.line}: ${diag.message} (${diag.ruleId})", isError = diag.severity == com.example.plugin.DiagnosticSeverity.ERROR))
            }
        }
        _uiState.value = _uiState.value.copy(terminalLines = lines, activeNavTab = 3)
    }

    fun showActiveStats() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val stats = pluginManager.calculateStats(active.content, active.file.extension)
        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("--- Code Statistics: ${active.name} ---", isCommand = true))
        lines.add(TerminalLine("Total Lines: ${stats.lines} | Words: ${stats.words} | Characters: ${stats.characters}"))
        lines.add(TerminalLine("Blank Lines: ${stats.blankLines} | Comments: ${stats.commentLines}"))
        _uiState.value = _uiState.value.copy(terminalLines = lines, activeNavTab = 3)
    }

    fun showActiveTodos() {
        val active = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val todos = pluginManager.extractTodos(active.content)
        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("--- TODO / FIXME Tasks: ${active.name} ---", isCommand = true))
        if (todos.isEmpty()) {
            lines.add(TerminalLine("No pending TODO comments found."))
        } else {
            for (t in todos) lines.add(TerminalLine(t))
        }
        _uiState.value = _uiState.value.copy(terminalLines = lines, activeNavTab = 3)
    }

    // --- Terminal Actions ---

    fun sendTerminalCommand(cmd: String) {
        val lines = _uiState.value.terminalLines.toMutableList()
        lines.add(TerminalLine("$ $cmd", isCommand = true))

        when (val firstWord = cmd.trim().split(" ").firstOrNull()?.lowercase()) {
            "help" -> {
                lines.add(TerminalLine("Available commands:"))
                lines.add(TerminalLine("  ls        - List workspace files"))
                lines.add(TerminalLine("  cat <f>   - Print file contents"))
                lines.add(TerminalLine("  echo <m>  - Print message"))
                lines.add(TerminalLine("  server    - Check HTTP server status"))
                lines.add(TerminalLine("  plugins   - List installed extensions"))
                lines.add(TerminalLine("  clear     - Clear terminal screen"))
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
                lines.add(TerminalLine(cmd.removePrefix("echo").trim()))
            }
            else -> {
                lines.add(TerminalLine("bash: $firstWord: command not found (type 'help' for options)", isError = true))
            }
        }

        _uiState.value = _uiState.value.copy(terminalLines = lines)
    }

    fun clearTerminal() {
        _uiState.value = _uiState.value.copy(terminalLines = emptyList())
    }

    override fun onCleared() {
        super.onCleared()
        devServer.stop()
    }
}
