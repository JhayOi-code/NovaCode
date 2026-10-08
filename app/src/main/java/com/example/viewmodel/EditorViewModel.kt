package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.git.GitService
import com.example.git.GitServiceImpl
import com.example.git.model.GitAuthor
import com.example.git.model.GitCommit
import com.example.git.model.GitCredentials
import com.example.git.model.GitHubAccount
import com.example.git.model.GitResult
import com.example.git.model.GitStatus
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.PermissionStatus
import com.example.model.SupportedLanguage
import com.example.model.TerminalLine
import com.example.permissions.StoragePermissionState
import com.example.server.LocalDevServer
import com.example.server.NanoHttpDevServer
import com.example.server.ServerRequestLog
import com.example.server.ServerStartResult
import com.example.server.ServerStatus
import com.example.storage.FileRepository
import com.example.storage.FileRepositoryImpl
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
    val currentTheme: EditorTheme = EditorTheme.LinqDark,
    val fontSizeSp: Int = 13,
    val isWordWrapEnabled: Boolean = false,
    val reduceTransparency: Boolean = false,
    val permissionStatus: PermissionStatus = PermissionStatus(),
    val showPermissionWizard: Boolean = false,
    val previewHtml: String? = null,
    val previewUrl: String? = null,
    val serverStatus: ServerStatus = ServerStatus.Stopped,
    val serverLogs: List<ServerRequestLog> = emptyList(),
    val isTerminalOpen: Boolean = false,
    val terminalLines: List<TerminalLine> = emptyList(),
    val isTerminalRunning: Boolean = false,
    val isGitPanelOpen: Boolean = false,
    val githubAccount: GitHubAccount = GitHubAccount(),
    val gitStatus: GitStatus? = null,
    val gitLogs: List<GitCommit> = emptyList(),
    val gitCurrentBranch: String = "main",
    val gitRemoteUrl: String = "",
    val isGitLoading: Boolean = false,
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

    val localDevServer: LocalDevServer = NanoHttpDevServer()
    val gitService: GitService = GitServiceImpl()

    private val terminalEngine by lazy {
        TerminalEngine(fileRepository.internalWorkspaceDir, gitService)
    }

    init {
        val account = loadGitHubAccount()
        _uiState.update { it.copy(githubAccount = account) }
        refreshPermissions()
        observeStorageRoots()
        loadDirectory(fileRepository.internalWorkspaceDir.absolutePath)
        openInitialFiles()

        // Sync Dev Server status and logs into UI state
        viewModelScope.launch {
            localDevServer.status.collect { status ->
                _uiState.update { it.copy(serverStatus = status) }
            }
        }
        viewModelScope.launch {
            localDevServer.requestLogs.collect { logs ->
                _uiState.update { it.copy(serverLogs = logs) }
            }
        }
    }

    private fun observeStorageRoots() {
        viewModelScope.launch {
            fileRepository.getStorageRoots().collect { roots ->
                _uiState.update { it.copy(storageRoots = roots) }
            }
        }
    }

    fun refreshPermissions() {
        val isAllFiles = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
        val grantedUris = getApplication<Application>().contentResolver.persistedUriPermissions.map { it.uri }
        _uiState.update {
            it.copy(
                permissionStatus = PermissionStatus(
                    tier1Granted = true,
                    tier2Granted = grantedUris.isNotEmpty(),
                    tier2Uri = grantedUris.firstOrNull()?.toString(),
                    tier3Granted = isAllFiles,
                    tier4Active = false,
                    activeStoragePath = it.currentPathOrUri
                )
            )
        }
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

    fun loadDirectory(file: File) {
        loadDirectory(file.absolutePath)
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

    fun navigateUp() {
        val current = _uiState.value.currentDirectory
        val parent = current.parentFile
        if (parent != null && parent.canRead()) {
            loadDirectory(parent.absolutePath)
        } else {
            Toast.makeText(getApplication(), "Already at storage root", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFile(item: FileItem) {
        if (item.isDirectory) {
            loadDirectory(item.path)
            return
        }

        viewModelScope.launch {
            val existingIndex = _uiState.value.openFiles.indexOfFirst { it.path == item.path }
            if (existingIndex != -1) {
                _uiState.update {
                    it.copy(activeFileIndex = existingIndex, activeNavTab = 1)
                }
                return@launch
            }

            fileRepository.readFile(item.path)
                .catch { error ->
                    Toast.makeText(getApplication(), "Could not read ${item.name}: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                .collect { content ->
                    val lang = SupportedLanguage.fromFilename(item.name)
                    val newCodeFile = CodeFile(
                        name = item.name,
                        path = item.path,
                        content = content,
                        language = lang
                    )
                    _uiState.update { state ->
                        val updated = state.openFiles + newCodeFile
                        state.copy(
                            openFiles = updated,
                            activeFileIndex = updated.size - 1,
                            activeNavTab = 1
                        )
                    }
                }
        }
    }

    fun openFromUri(uri: Uri) {
        loadDirectory(uri.toString())
    }

    fun openInitialFiles() {
        viewModelScope.launch {
            val starterFile = File(fileRepository.internalWorkspaceDir, "PythonStarter/main.py")
            if (starterFile.exists()) {
                openFile(FileItem(name = starterFile.name, path = starterFile.absolutePath, isDirectory = false))
            }
        }
    }

    fun switchActiveFile(index: Int) {
        if (index in _uiState.value.openFiles.indices) {
            _uiState.update { it.copy(activeFileIndex = index) }
        }
    }

    fun selectTab(index: Int) {
        switchActiveFile(index)
    }

    fun closeFile(index: Int) {
        _uiState.update { state ->
            val updated = state.openFiles.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
                val newActive = if (updated.isEmpty()) 0 else (index.coerceAtMost(updated.size - 1))
                state.copy(openFiles = updated, activeFileIndex = newActive)
            } else {
                state
            }
        }
    }

    fun closeTab(index: Int) {
        closeFile(index)
    }

    fun newTab() {
        val untitledName = "untitled_${System.currentTimeMillis() % 1000}.txt"
        val tempFile = File(fileRepository.internalWorkspaceDir, untitledName)
        val newCode = CodeFile(name = untitledName, path = tempFile.absolutePath, content = "")
        _uiState.update {
            val list = it.openFiles + newCode
            it.copy(openFiles = list, activeFileIndex = list.size - 1, activeNavTab = 1)
        }
    }

    fun updateActiveFileContent(newContent: String) {
        val activeIndex = _uiState.value.activeFileIndex
        val currentList = _uiState.value.openFiles
        if (activeIndex in currentList.indices) {
            val target = currentList[activeIndex]
            val updated = target.copy(content = newContent, isDirty = true)
            val newList = currentList.toMutableList()
            newList[activeIndex] = updated
            _uiState.update { it.copy(openFiles = newList) }
        }
    }

    fun saveActiveFile() {
        val activeIndex = _uiState.value.activeFileIndex
        val currentList = _uiState.value.openFiles
        if (activeIndex in currentList.indices) {
            val target = currentList[activeIndex]
            viewModelScope.launch {
                fileRepository.writeFile(target.path, target.content)
                    .onSuccess {
                        val updated = target.copy(isDirty = false)
                        val newList = currentList.toMutableList()
                        newList[activeIndex] = updated
                        _uiState.update { it.copy(openFiles = newList) }
                        Toast.makeText(getApplication(), "Saved ${target.name}", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { error ->
                        Toast.makeText(getApplication(), "Failed to save ${target.name}: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    fun createNewFileInExplorer(fileName: String) {
        viewModelScope.launch {
            fileRepository.createFile(_uiState.value.currentPathOrUri, fileName, false)
                .onSuccess { item ->
                    loadDirectory(_uiState.value.currentPathOrUri)
                    openFile(item)
                    Toast.makeText(getApplication(), "Created $fileName", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(getApplication(), "Failed to create file: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun createNewFolderInExplorer(folderName: String) {
        viewModelScope.launch {
            fileRepository.createFile(_uiState.value.currentPathOrUri, folderName, true)
                .onSuccess {
                    loadDirectory(_uiState.value.currentPathOrUri)
                    Toast.makeText(getApplication(), "Created folder $folderName", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(getApplication(), "Failed to create directory: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun deleteFileInExplorer(target: Any) {
        val path = when (target) {
            is FileItem -> target.path
            is File -> target.absolutePath
            else -> target.toString()
        }
        viewModelScope.launch {
            fileRepository.deleteFile(path)
                .onSuccess {
                    loadDirectory(_uiState.value.currentPathOrUri)
                    val openIndex = _uiState.value.openFiles.indexOfFirst { it.path == path }
                    if (openIndex != -1) {
                        closeFile(openIndex)
                    }
                    Toast.makeText(getApplication(), "Deleted file", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(getApplication(), "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun renameFileInExplorer(oldTarget: Any, newName: String) {
        val oldPath = when (oldTarget) {
            is FileItem -> oldTarget.path
            is File -> oldTarget.absolutePath
            else -> oldTarget.toString()
        }
        viewModelScope.launch {
            fileRepository.renameFile(oldPath, newName)
                .onSuccess {
                    loadDirectory(_uiState.value.currentPathOrUri)
                    Toast.makeText(getApplication(), "Renamed to $newName", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(getApplication(), "Rename failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun exportZip(destination: Any) {
        viewModelScope.launch {
            val currentDir = _uiState.value.currentDirectory
            val targetFile = when (destination) {
                is File -> destination
                is Uri -> File(getApplication<Application>().cacheDir, "workspace_${System.currentTimeMillis()}.zip")
                else -> File(getApplication<Application>().cacheDir, "workspace.zip")
            }
            fileRepository.zipDirectory(currentDir.absolutePath, targetFile)
                .onSuccess {
                    Toast.makeText(getApplication(), "Exported ZIP successfully to ${targetFile.name}", Toast.LENGTH_LONG).show()
                }
                .onFailure { err ->
                    Toast.makeText(getApplication(), "Zip export failed: ${err.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // --- Local Development HTTP Server & Web Preview ---

    fun openWebDemo() {
        val webDir = File(fileRepository.internalWorkspaceDir, "WebStarter")
        startDevServerAndPreview(webDir, "index.html")
    }

    fun openWebPreviewForActiveFile(file: CodeFile) {
        val targetFile = File(file.path)
        val parentDir = if (targetFile.parentFile != null && targetFile.parentFile.exists()) {
            targetFile.parentFile
        } else {
            _uiState.value.currentDirectory
        }
        startDevServerAndPreview(parentDir, targetFile.name)
    }

    fun startDevServerAndPreview(dir: File? = null, targetPath: String = "") {
        val targetDir = dir ?: _uiState.value.currentDirectory
        when (val result = localDevServer.start(targetDir)) {
            is ServerStartResult.Success -> {
                val fullUrl = if (targetPath.isNotEmpty()) {
                    "${result.url}${targetPath.trimStart('/')}"
                } else {
                    result.url
                }
                _uiState.update {
                    it.copy(
                        previewUrl = fullUrl,
                        previewHtml = null
                    )
                }
                Toast.makeText(getApplication(), "Dev Server running at ${result.url}", Toast.LENGTH_SHORT).show()
            }
            is ServerStartResult.Failure -> {
                Toast.makeText(getApplication(), result.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun startDevServer(rootDir: File? = null, port: Int = 8080): ServerStartResult {
        val dir = rootDir ?: _uiState.value.currentDirectory
        return localDevServer.start(dir, port)
    }

    fun stopDevServer() {
        localDevServer.stop()
        Toast.makeText(getApplication(), "Local dev server stopped", Toast.LENGTH_SHORT).show()
    }

    fun restartDevServer() {
        val dir = (_uiState.value.serverStatus as? ServerStatus.Running)?.rootDir
            ?: _uiState.value.currentDirectory
        val port = (_uiState.value.serverStatus as? ServerStatus.Running)?.port ?: 8080
        val result = localDevServer.restart(dir, port)
        if (result is ServerStartResult.Success) {
            Toast.makeText(getApplication(), "Dev Server restarted on port ${result.port}", Toast.LENGTH_SHORT).show()
        }
    }

    fun closePreview() {
        _uiState.update { it.copy(previewHtml = null, previewUrl = null) }
        localDevServer.stop()
    }

    // --- Git & GitHub Account Operations ---

    private fun loadGitHubAccount(): GitHubAccount {
        val prefs = getApplication<Application>().getSharedPreferences("github_prefs", Context.MODE_PRIVATE)
        val isLinked = prefs.getBoolean("is_linked", false)
        val username = prefs.getString("username", "") ?: ""
        val token = prefs.getString("token", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val displayName = prefs.getString("display_name", "") ?: ""
        val publicRepos = prefs.getInt("public_repos", 0)
        val linkedAt = prefs.getLong("linked_at", 0L)
        return GitHubAccount(
            username = username,
            token = token,
            email = email,
            isLinked = isLinked && username.isNotBlank(),
            linkedAt = linkedAt,
            displayName = displayName,
            publicRepos = publicRepos
        )
    }

    fun linkGitHubAccount(username: String, token: String, email: String = "", onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val cleanUser = username.trim().removePrefix("@")
        val cleanToken = token.trim()
        val cleanEmail = email.trim().ifEmpty { "$cleanUser@users.noreply.github.com" }

        if (cleanUser.isEmpty()) {
            Toast.makeText(getApplication(), "GitHub username cannot be empty", Toast.LENGTH_SHORT).show()
            onResult(false, "Username is required")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            val verifyResult = gitService.verifyGitHubCredentials(cleanUser, cleanToken)
            _uiState.update { it.copy(isGitLoading = false) }

            when (verifyResult) {
                is GitResult.Success -> {
                    val verifiedAccount = verifyResult.data
                    val finalEmail = if (cleanEmail.isNotEmpty() && cleanEmail != "$cleanUser@users.noreply.github.com") cleanEmail else verifiedAccount.email
                    val accountToSave = verifiedAccount.copy(email = finalEmail)

                    val prefs = getApplication<Application>().getSharedPreferences("github_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("is_linked", true)
                        .putString("username", accountToSave.username)
                        .putString("token", accountToSave.token)
                        .putString("email", accountToSave.email)
                        .putString("display_name", accountToSave.displayName)
                        .putInt("public_repos", accountToSave.publicRepos)
                        .putLong("linked_at", accountToSave.linkedAt)
                        .apply()

                    _uiState.update { it.copy(githubAccount = accountToSave) }
                    Toast.makeText(getApplication(), verifyResult.message.ifEmpty { "Linked GitHub account: @${accountToSave.username}" }, Toast.LENGTH_SHORT).show()
                    onResult(true, verifyResult.message)
                }
                is GitResult.Failure -> {
                    Toast.makeText(getApplication(), verifyResult.error, Toast.LENGTH_LONG).show()
                    onResult(false, verifyResult.error)
                }
            }
        }
    }

    fun unlinkGitHubAccount() {
        val prefs = getApplication<Application>().getSharedPreferences("github_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _uiState.update { it.copy(githubAccount = GitHubAccount()) }
        Toast.makeText(getApplication(), "Unlinked GitHub account", Toast.LENGTH_SHORT).show()
    }

    fun toggleGitPanel(open: Boolean? = null) {
        val nextOpen = open ?: !_uiState.value.isGitPanelOpen
        _uiState.update { it.copy(isGitPanelOpen = nextOpen) }
        if (nextOpen) {
            refreshGitStatus()
        }
    }

    fun refreshGitStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            val dir = _uiState.value.currentDirectory
            val statusResult = gitService.status(dir)
            val branchResult = gitService.currentBranch(dir)
            val remoteResult = gitService.getRemote(dir, "origin")
            val logsResult = gitService.log(dir, 15)

            _uiState.update {
                it.copy(
                    isGitLoading = false,
                    gitStatus = statusResult.getOrNull(),
                    gitCurrentBranch = branchResult.getOrNull() ?: "main",
                    gitRemoteUrl = remoteResult.getOrNull() ?: "",
                    gitLogs = logsResult.getOrNull() ?: emptyList()
                )
            }
        }
    }

    fun gitInitRepo(initialBranch: String = "main", onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            when (val res = gitService.init(_uiState.value.currentDirectory, initialBranch)) {
                is GitResult.Success -> {
                    // Auto-configure remote if user has linked GitHub account
                    val account = _uiState.value.githubAccount
                    if (account.isLinked && account.username.isNotBlank()) {
                        val folderName = _uiState.value.currentDirectory.name.lowercase().replace(' ', '-')
                        val defaultRemote = "https://github.com/${account.username}/$folderName.git"
                        gitService.setRemote(_uiState.value.currentDirectory, "origin", defaultRemote)
                    }
                    refreshGitStatus()
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    _uiState.update { it.copy(isGitLoading = false) }
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitSetRemote(url: String, remote: String = "origin", onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            when (val res = gitService.setRemote(_uiState.value.currentDirectory, remote, url)) {
                is GitResult.Success -> {
                    refreshGitStatus()
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    _uiState.update { it.copy(isGitLoading = false) }
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitPull(remote: String = "origin", branch: String = "main", onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            val account = _uiState.value.githubAccount
            val creds = if (account.isLinked) GitCredentials(account.username, account.token) else null
            when (val res = gitService.pull(_uiState.value.currentDirectory, remote, branch, creds)) {
                is GitResult.Success -> {
                    refreshGitStatus()
                    loadDirectory(_uiState.value.currentDirectory)
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    _uiState.update { it.copy(isGitLoading = false) }
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitPush(remote: String = "origin", branch: String = "main", onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            val account = _uiState.value.githubAccount
            val dir = _uiState.value.currentDirectory

            // Ensure at least one commit exists before pushing
            val statusRes = gitService.status(dir)
            val logsRes = gitService.log(dir, 1)
            val commitsExist = (logsRes as? GitResult.Success)?.data?.isNotEmpty() == true
            val hasChanges = (statusRes as? GitResult.Success)?.data?.isClean == false

            if (!commitsExist && hasChanges) {
                val author = if (account.isLinked) GitAuthor(name = account.username, email = account.email) else GitAuthor()
                gitService.add(dir, ".")
                gitService.commit(dir, "Initial commit from Linq Studio", author)
            } else if (!commitsExist && !hasChanges) {
                val readme = File(dir, "README.md")
                if (!readme.exists()) {
                    readme.writeText("# ${dir.name}\n\nManaged with Linq Studio Git Engine.\n")
                }
                val author = if (account.isLinked) GitAuthor(name = account.username, email = account.email) else GitAuthor()
                gitService.add(dir, ".")
                gitService.commit(dir, "Initial commit", author)
            }

            val creds = if (account.isLinked) GitCredentials(account.username, account.token) else null
            when (val res = gitService.push(dir, remote, branch, creds)) {
                is GitResult.Success -> {
                    refreshGitStatus()
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    _uiState.update { it.copy(isGitLoading = false) }
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitCommit(message: String, onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            val account = _uiState.value.githubAccount
            val author = if (account.isLinked) {
                GitAuthor(name = account.username, email = account.email)
            } else {
                GitAuthor()
            }
            when (val res = gitService.commit(_uiState.value.currentDirectory, message, author)) {
                is GitResult.Success -> {
                    refreshGitStatus()
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    _uiState.update { it.copy(isGitLoading = false) }
                    Toast.makeText(getApplication(), res.error, Toast.LENGTH_LONG).show()
                    onFinished(false, res.error)
                }
            }
        }
    }

    fun gitClone(repoUrl: String, onFinished: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGitLoading = true) }
            val account = _uiState.value.githubAccount
            val creds = if (account.isLinked) GitCredentials(account.username, account.token) else null
            val targetName = repoUrl.substringAfterLast('/').removeSuffix(".git").ifEmpty { "repo" }
            val targetDir = File(fileRepository.internalWorkspaceDir, targetName)
            when (val res = gitService.clone(repoUrl, targetDir, creds)) {
                is GitResult.Success -> {
                    loadDirectory(_uiState.value.currentPathOrUri)
                    refreshGitStatus()
                    Toast.makeText(getApplication(), res.message, Toast.LENGTH_SHORT).show()
                    onFinished(true, res.message)
                }
                is GitResult.Failure -> {
                    _uiState.update { it.copy(isGitLoading = false) }
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
                    isTerminalRunning = false
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

    fun setNavTab(tabIndex: Int) {
        _uiState.update { it.copy(activeNavTab = tabIndex) }
    }

    override fun onCleared() {
        super.onCleared()
        localDevServer.stop()
    }
}
