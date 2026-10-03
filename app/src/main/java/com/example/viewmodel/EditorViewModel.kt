package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.git.GitService
import com.example.git.GitServiceImpl
import com.example.git.model.GitResult
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.PermissionStatus
import com.example.model.SupportedLanguage
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import com.example.storage.FileRepository
import com.example.storage.FileRepositoryImpl
import com.example.storage.StoragePermissionHelper
import com.example.terminal.TerminalEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class EditorUiState(
    val openFiles: List<CodeFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val currentDirectory: File,
    val currentPathOrUri: String,
    val directoryFiles: List<FileItem> = emptyList(),
    val storageRoots: List<FileItem> = emptyList(),
    val currentTheme: EditorTheme = EditorTheme.NovaDark,
    val fontSizeSp: Int = 13,
    val isWordWrapEnabled: Boolean = false,
    val reduceTransparency: Boolean = false,
    val permissionStatus: PermissionStatus = PermissionStatus(),
    val showPermissionWizard: Boolean = false,
    val previewHtml: String? = null,
    val isTerminalOpen: Boolean = false,
    val terminalLines: List<TerminalLine> = emptyList(),
    val isTerminalRunning: Boolean = false,
    val activeNavTab: Int = 0 // 0: Home, 1: Editor, 2: Files, 3: Settings, 4: About
)

class EditorViewModel(
    application: Application,
    val fileRepository: FileRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        FileRepositoryImpl(application)
    )

    private val _uiState = MutableStateFlow(
        EditorUiState(
            currentDirectory = fileRepository.internalWorkspaceDir,
            currentPathOrUri = fileRepository.internalWorkspaceDir.absolutePath
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
        observeStorageRoots()
        loadDirectory(fileRepository.internalWorkspaceDir.absolutePath)
        openInitialFiles()
    }

    private fun observeStorageRoots() {
        viewModelScope.launch {
            fileRepository.getStorageRoots()
                .catch { e -> e.printStackTrace() }
                .collect { roots ->
                    _uiState.update { it.copy(storageRoots = roots) }
                }
        }
    }

    private fun openInitialFiles() {
        viewModelScope.launch {
            val readmePath = File(fileRepository.internalWorkspaceDir, "README.md").absolutePath
            fileRepository.readFile(readmePath)
                .catch { /* ignore missing */ }
                .collect { content ->
                    val file = CodeFile(
                        name = "README.md",
                        path = readmePath,
                        content = content,
                        language = SupportedLanguage.MARKDOWN
                    )
                    _uiState.update { it.copy(openFiles = listOf(file), activeFileIndex = 0) }
                }
        }
    }

    fun refreshPermissions() {
        val status = StoragePermissionHelper.checkStatus(getApplication())
        _uiState.update { it.copy(permissionStatus = status) }
        observeStorageRoots()
    }

    fun setNavTab(tabIndex: Int) {
        _uiState.update { it.copy(activeNavTab = tabIndex) }
    }

    fun togglePermissionWizard(show: Boolean) {
        _uiState.update { it.copy(showPermissionWizard = show) }
    }

    fun selectTheme(theme: EditorTheme) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun importJsonTheme(jsonStr: String): Boolean {
        return try {
            val theme = EditorTheme.fromJson(jsonStr)
            _uiState.update { it.copy(currentTheme = theme) }
            Toast.makeText(getApplication(), "Applied theme: ${theme.name}", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Failed to parse JSON theme: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun setFontSize(sizeSp: Int) {
        _uiState.update { it.copy(fontSizeSp = sizeSp) }
    }

    fun toggleWordWrap(enabled: Boolean) {
        _uiState.update { it.copy(isWordWrapEnabled = enabled) }
    }

    fun toggleReduceTransparency(enabled: Boolean) {
        _uiState.update { it.copy(reduceTransparency = enabled) }
    }

    fun loadDirectory(pathOrUri: String) {
        viewModelScope.launch {
            fileRepository.listFiles(pathOrUri)
                .catch { e ->
                    e.printStackTrace()
                    Toast.makeText(getApplication(), "Failed to read files: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .collect { files ->
                    val dirFile = if (pathOrUri.startsWith("content://")) {
                        fileRepository.internalWorkspaceDir
                    } else {
                        File(pathOrUri)
                    }
                    _uiState.update {
                        it.copy(
                            currentDirectory = dirFile,
                            currentPathOrUri = pathOrUri,
                            directoryFiles = files
                        )
                    }
                }
        }
    }

    fun loadDirectory(directory: File) {
        loadDirectory(directory.absolutePath)
    }

    fun navigateUp() {
        val current = _uiState.value.currentPathOrUri
        if (current.startsWith("content://")) {
            // For SAF tree root, return to internal workspace
            loadDirectory(fileRepository.internalWorkspaceDir.absolutePath)
        } else {
            val parent = File(current).parentFile
            if (parent != null && parent.canRead()) {
                loadDirectory(parent.absolutePath)
            }
        }
    }

    fun openFile(fileItem: FileItem) {
        viewModelScope.launch {
            val existingIndex = _uiState.value.openFiles.indexOfFirst { it.path == fileItem.path }
            if (existingIndex != -1) {
                _uiState.update { it.copy(activeFileIndex = existingIndex, activeNavTab = 1) }
                return@launch
            }

            fileRepository.readFile(fileItem.path)
                .catch { e ->
                    Toast.makeText(getApplication(), "Failed to open: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .collect { content ->
                    val newCodeFile = CodeFile(
                        name = fileItem.name,
                        path = fileItem.path,
                        content = content,
                        language = fileItem.language
                    )
                    val newFiles = _uiState.value.openFiles + newCodeFile
                    _uiState.update {
                        it.copy(
                            openFiles = newFiles,
                            activeFileIndex = newFiles.lastIndex,
                            activeNavTab = 1
                        )
                    }
                }
        }
    }

    fun openFromUri(uri: Uri) {
        viewModelScope.launch {
            val path = uri.toString()
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_file.txt"
            fileRepository.readFile(path)
                .catch { e ->
                    Toast.makeText(getApplication(), "Error loading URI: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .collect { content ->
                    val codeFile = CodeFile(
                        name = name,
                        path = path,
                        content = content,
                        language = SupportedLanguage.fromFilename(name)
                    )
                    val newFiles = _uiState.value.openFiles + codeFile
                    _uiState.update {
                        it.copy(
                            openFiles = newFiles,
                            activeFileIndex = newFiles.lastIndex,
                            activeNavTab = 1
                        )
                    }
                }
        }
    }

    fun selectTab(index: Int) {
        if (index in _uiState.value.openFiles.indices) {
            _uiState.update { it.copy(activeFileIndex = index) }
        }
    }

    fun closeTab(index: Int) {
        val files = _uiState.value.openFiles.toMutableList()
        if (index in files.indices) {
            files.removeAt(index)
            val newActiveIndex = if (files.isEmpty()) 0 else (index.coerceAtMost(files.lastIndex))
            _uiState.update { it.copy(openFiles = files, activeFileIndex = newActiveIndex) }
        }
    }

    fun newTab() {
        val count = _uiState.value.openFiles.size + 1
        val newFile = CodeFile(
            name = "Untitled_$count.txt",
            path = File(fileRepository.internalWorkspaceDir, "Untitled_$count.txt").absolutePath,
            content = "",
            language = SupportedLanguage.PLAINTEXT,
            isDirty = true
        )
        val files = _uiState.value.openFiles + newFile
        _uiState.update {
            it.copy(
                openFiles = files,
                activeFileIndex = files.lastIndex,
                activeNavTab = 1
            )
        }
    }

    fun updateActiveFileContent(newContent: String) {
        val currentIdx = _uiState.value.activeFileIndex
        val files = _uiState.value.openFiles.toMutableList()
        if (currentIdx in files.indices) {
            val current = files[currentIdx]
            files[currentIdx] = current.withUpdatedContent(newContent)
            _uiState.update { it.copy(openFiles = files) }
        }
    }

    fun saveActiveFile() {
        val currentIdx = _uiState.value.activeFileIndex
        val files = _uiState.value.openFiles.toMutableList()
        if (currentIdx in files.indices) {
            val current = files[currentIdx]
            viewModelScope.launch {
                val result = fileRepository.writeFile(current.path, current.content)
                result.onSuccess {
                    files[currentIdx] = current.markSaved()
                    _uiState.update { it.copy(openFiles = files) }
                    loadDirectory(_uiState.value.currentPathOrUri)
                    Toast.makeText(getApplication(), "Saved: ${current.name}", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(getApplication(), "Save failed: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun createNewFileInExplorer(name: String) {
        viewModelScope.launch {
            val result = fileRepository.createFile(_uiState.value.currentPathOrUri, name, isFolder = false)
            result.onSuccess { item ->
                loadDirectory(_uiState.value.currentPathOrUri)
                openFile(item)
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Could not create file: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createNewFolderInExplorer(name: String) {
        viewModelScope.launch {
            val result = fileRepository.createFile(_uiState.value.currentPathOrUri, name, isFolder = true)
            result.onSuccess {
                loadDirectory(_uiState.value.currentPathOrUri)
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Could not create folder: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteFileInExplorer(path: String) {
        viewModelScope.launch {
            val result = fileRepository.deleteFile(path)
            result.onSuccess {
                val openIdx = _uiState.value.openFiles.indexOfFirst { it.path == path }
                if (openIdx != -1) closeTab(openIdx)
                loadDirectory(_uiState.value.currentPathOrUri)
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Delete failed: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun renameFileInExplorer(oldPath: String, newName: String) {
        viewModelScope.launch {
            val result = fileRepository.renameFile(oldPath, newName)
            result.onSuccess {
                loadDirectory(_uiState.value.currentPathOrUri)
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Rename failed: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun exportZip(dir: File) {
        viewModelScope.launch {
            val outZip = File(dir.parentFile ?: dir, "${dir.name}_export.zip")
            val result = fileRepository.zipDirectory(dir.absolutePath, outZip)
            result.onSuccess {
                loadDirectory(_uiState.value.currentPathOrUri)
                Toast.makeText(getApplication(), "Exported to: ${outZip.name}", Toast.LENGTH_LONG).show()
            }.onFailure { err ->
                Toast.makeText(getApplication(), "Zip export failed: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openWebDemo() {
        viewModelScope.launch {
            val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
            val indexFile = File(webDir, "index.html")
            val cssFile = File(webDir, "style.css")
            val jsFile = File(webDir, "app.js")

            val html = if (indexFile.exists()) indexFile.readText() else "<h1>NovaCode</h1>"
            val css = if (cssFile.exists()) cssFile.readText() else ""
            val js = if (jsFile.exists()) jsFile.readText() else ""

            val combinedHtml = html
                .replace("<link rel=\"stylesheet\" href=\"style.css\">", "<style>$css</style>")
                .replace("<script src=\"app.js\"></script>", "<script>$js</script>")

            _uiState.update { it.copy(previewHtml = combinedHtml) }
        }
    }

    val gitService: GitService = GitServiceImpl()

    private val terminalEngine by lazy {
        TerminalEngine(fileRepository.internalWorkspaceDir, gitService)
    }

    fun gitClone(repoUrl: String, onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val targetName = repoUrl.substringAfterLast('/').removeSuffix(".git").ifEmpty { "repo" }
            val targetDir = File(fileRepository.internalWorkspaceDir, targetName)
            when (val res = gitService.clone(repoUrl, targetDir)) {
                is GitResult.Success -> {
                    loadDirectory(_uiState.value.currentPathOrUri)
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitCommit(message: String, onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val res = gitService.commit(_uiState.value.currentDirectory, message)) {
                is GitResult.Success -> {
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitPush(remote: String = "origin", branch: String = "main", onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            when (val res = gitService.push(_uiState.value.currentDirectory, remote, branch)) {
                is GitResult.Success -> {
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun openPythonDemo() {
        val pyFile = File(fileRepository.internalWorkspaceDir, "PythonStarter/main.py")
        if (pyFile.exists()) {
            openFile(FileItem(name = pyFile.name, path = pyFile.absolutePath, isDirectory = false))
        }
    }

    fun runActiveFile(file: CodeFile) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTerminalOpen = true,
                    isTerminalRunning = true,
                    activeNavTab = 1 // Switch to Editor screen
                )
            }

            // Slight realistic delay to reflect process spin-up
            kotlinx.coroutines.delay(200)

            val outputLines = terminalEngine.executeCode(file)
            _uiState.update {
                it.copy(
                    terminalLines = it.terminalLines + outputLines,
                    isTerminalRunning = false,
                    // If HTML, also optionally allow web preview toggle
                    previewHtml = if (file.language == SupportedLanguage.HTML) file.content else it.previewHtml
                )
            }
        }
    }

    fun rerunActiveFile() {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex)
        if (currentFile != null) {
            runActiveFile(currentFile)
        } else {
            sendTerminalCommand("echo 'No active file selected to run'")
        }
    }

    fun sendTerminalCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.equals("clear", ignoreCase = true)) {
            clearTerminal()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTerminalRunning = true) }
            val currentDir = _uiState.value.currentDirectory
            val outputLines = terminalEngine.executeCommand(trimmed, currentDir)

            _uiState.update {
                it.copy(
                    terminalLines = it.terminalLines + outputLines,
                    isTerminalRunning = false
                )
            }
        }
    }

    fun clearTerminal() {
        _uiState.update { it.copy(terminalLines = emptyList()) }
    }

    fun toggleTerminal(open: Boolean? = null) {
        _uiState.update {
            it.copy(isTerminalOpen = open ?: !it.isTerminalOpen)
        }
    }

    fun closePreview() {
        _uiState.update { it.copy(previewHtml = null) }
    }
}
