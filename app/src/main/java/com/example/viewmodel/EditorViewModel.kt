package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiChatMessage
import com.example.ai.AiKeyStore
import com.example.ai.AiProvider
import com.example.ai.AiRole
import com.example.ai.AiService
import com.example.ai.AiSettings
import com.example.ai.ProposedDiff
import com.example.autocomplete.AutocompleteEngine
import com.example.autocomplete.AutocompleteSuggestion
import com.example.format.PrettierFormatter
import com.example.git.GitManager
import com.example.linter.CodeLinter
import com.example.model.CapturedLog
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.GitRepositoryStatus
import com.example.model.LintIssue
import com.example.model.ProjectTemplateType
import com.example.model.UserAccount
import com.example.templates.NewFileTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.server.LocalDevServer
import com.example.server.LocalHttpDevServer
import java.io.File
import java.util.Stack
import java.util.UUID

data class EditorUiState(
    val activeNavTab: Int = 1, // 0: Home, 1: Editor, 2: Files, 3: Terminal, 4: Settings, 5: About
    val currentTheme: EditorTheme = EditorTheme.LinqDark,
    val openFiles: List<CodeFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val currentDirectory: String = "/workspace",
    val directoryFiles: List<CodeFile> = emptyList(),
    val trackedFolders: List<String> = listOf("src", "scripts", "styles", "docs"),
    val isExplorerSidebarOpen: Boolean = false,
    val isSearchReplaceOpen: Boolean = false,
    val isMinimapVisible: Boolean = false,
    val isZenMode: Boolean = false,
    val isFullScreen: Boolean = false,
    val isSuggestionsEnabled: Boolean = true,
    val lintIssues: List<LintIssue> = emptyList(),
    val autocompleteSuggestions: List<AutocompleteSuggestion> = emptyList(),
    val prettierFormatNotice: String? = null,
    val saveNotice: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val gitStatus: GitRepositoryStatus = GitRepositoryStatus(),
    val terminalLines: List<String> = listOf(
        "Linq Studio Shell v2.4.0 (android-x86_64)",
        "Type 'help' or 'run' to execute code."
    ),
    val serverStatus: String = "Stopped",
    val hostedFolder: String = "www",
    val availableHostFolders: List<String> = listOf("www", "src", "public", "docs", "dist", "site"),
    val isWebPreviewOpen: Boolean = false,
    val activeWebPreviewFileName: String = "index.html",
    val userAccount: UserAccount = UserAccount(),
    val aiSettings: AiSettings = AiSettings(),
    val isAiPanelOpen: Boolean = false,
    val aiMessages: List<AiChatMessage> = emptyList(),
    val isAiLoading: Boolean = false,
    val prefilledAiPrompt: String = "",
    val activeProposedDiff: ProposedDiff? = null,
    val isDiffPreviewOpen: Boolean = false,
    val hasAiApiKey: Boolean = false
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("linq_studio_prefs", Context.MODE_PRIVATE)
    val keyStore = AiKeyStore(application)
    val aiService = AiService(keyStore)
    val gitManager = GitManager()

    // Undo / Redo stacks
    private val undoStack = Stack<String>()
    private val redoStack = Stack<String>()

    private var lintJob: Job? = null
    private var aiStreamJob: Job? = null

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val localDevServer: LocalDevServer = LocalHttpDevServer()

    init {
        // Initial files setup: Java, Python (with typewriter sample), HTML, about.html, style.css, app.js
        val javaFile = CodeFile(
            name = "Example.java",
            path = "/workspace/Example.java",
            content = """
public class Example {
    public static void main(String[] args) {
        System.out.println("⚡ Welcome to Linq Studio Mobile Java Runner!");
        for (int i = 1; i <= 3; i++) {
            System.out.println("Processing thread queue task #" + i + " [OK]");
        }
        System.out.println("Execution finished cleanly.");
    }
}
""".trimIndent()
        )
        val pythonFile = CodeFile(
            name = "main.py",
            path = "/workspace/main.py",
            content = """
import sys, time

def typewriter(text, delay=0.03):
    for char in text:
        sys.stdout.write(char)
        sys.stdout.flush()
        time.sleep(delay)
    sys.stdout.write("\n")

print("=== Linq Python Typewriter Engine ===")
typewriter("⚡ Welcome to Linq Studio Mobile IDE!")
typewriter("🚀 Running python scripts directly on Android.")
typewriter("✨ Typewriter effect running seamlessly in Termux terminal!")
""".trimIndent()
        )
        val indexHtml = CodeFile(
            name = "index.html",
            path = "/workspace/index.html",
            content = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Linq Studio Local Website</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="container">
    <div class="logo">⚡ LINQ STUDIO</div>
    <h1>Local Dev Server Active</h1>
    <p>Test and run multiple website files locally with instant live preview.</p>
    <div class="badge">HTTP :8080 ONLINE</div>
    <div class="buttons">
      <button onclick="testAction()">Run Test Script</button>
      <a href="about.html" class="link-btn">Visit about.html</a>
    </div>
    <div id="output"></div>
  </div>
  <script src="app.js"></script>
</body>
</html>
""".trimIndent()
        )
        val aboutHtml = CodeFile(
            name = "about.html",
            path = "/workspace/about.html",
            content = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>About - Linq Studio</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="container">
    <div class="logo">PAGE: ABOUT.HTML</div>
    <h1>About Our Project</h1>
    <p>This is a secondary website file served by the Linq Studio local dev server to demonstrate multi-file web testing.</p>
    <a href="index.html" class="link-btn">← Back to index.html</a>
  </div>
</body>
</html>
""".trimIndent()
        )
        val styleCss = CodeFile(
            name = "style.css",
            path = "/workspace/style.css",
            content = """
body {
  margin: 0;
  padding: 24px;
  background: #0F172A;
  color: #F8FAFC;
  font-family: system-ui, -apple-system, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 85vh;
}
.container {
  background: #1E293B;
  border-radius: 18px;
  padding: 32px 24px;
  max-width: 480px;
  width: 100%;
  text-align: center;
  border: 1px solid #334155;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
}
.logo {
  font-weight: 800;
  font-size: 13px;
  letter-spacing: 2px;
  color: #38BDF8;
  margin-bottom: 8px;
}
h1 {
  margin: 0 0 10px;
  color: #F1F5F9;
  font-size: 22px;
}
p {
  color: #94A3B8;
  font-size: 14px;
  line-height: 1.6;
}
.badge {
  display: inline-block;
  background: #064E3B;
  color: #6EE7B7;
  font-weight: 700;
  font-size: 11px;
  padding: 5px 12px;
  border-radius: 99px;
  margin: 14px 0;
}
.buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 14px;
}
button, .link-btn {
  background: #6366F1;
  color: white;
  border: none;
  padding: 9px 16px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  font-size: 12px;
}
#output {
  margin-top: 14px;
  font-family: monospace;
  color: #38BDF8;
  font-size: 12px;
}
""".trimIndent()
        )
        val appJs = CodeFile(
            name = "app.js",
            path = "/workspace/app.js",
            content = """
function testAction() {
  const out = document.getElementById("output");
  out.innerHTML = "✓ Local JavaScript executed successfully at " + new Date().toLocaleTimeString();
}
""".trimIndent()
        )
        val initialFiles = listOf(pythonFile, javaFile, indexHtml, aboutHtml, styleCss, appJs)

        // Restore suggestion toggle setting
        val savedSuggestionsEnabled = prefs.getBoolean("suggestions_enabled", true)
        val savedProviderName = prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        val provider = try { AiProvider.valueOf(savedProviderName) } catch (_: Exception) { AiProvider.GEMINI }
        val savedModel = prefs.getString("ai_model", provider.defaultModel) ?: provider.defaultModel

        // Restore persisted theme
        val savedThemeId = prefs.getString("selected_theme_id", EditorTheme.LinqDark.id)
        val initialTheme = EditorTheme.findById(savedThemeId)

        // Restore permanent User Account
        val isUserSignedIn = prefs.getBoolean("user_is_signed_in", false)
        val userUsername = prefs.getString("user_username", "linq_dev") ?: "linq_dev"
        val userTitle = prefs.getString("user_title", "Mobile Software Engineer") ?: "Mobile Software Engineer"
        val userEmail = prefs.getString("user_email", "developer@linqstudio.dev") ?: "developer@linqstudio.dev"
        val userBio = prefs.getString("user_bio", "Building high-performance cross-platform software on mobile.") ?: "Building high-performance cross-platform software on mobile."
        val userAvatarUrl = prefs.getString("user_avatar_url", null)
        val userAuthProvider = prefs.getString("user_auth_provider", "Guest") ?: "Guest"
        val userAvatarColor = prefs.getLong("user_avatar_color_hex", 0xFF00E5FF)
        val userPrimaryStack = prefs.getString("user_primary_stack", "Kotlin • Python • JavaScript") ?: "Kotlin • Python • JavaScript"
        val userAvatarId = prefs.getString("user_avatar_id", "avatar_terminal") ?: "avatar_terminal"
        val userAvatarCustomUri = prefs.getString("user_avatar_custom_uri", null)

        val restoredAccount = UserAccount(
            username = userUsername,
            title = userTitle,
            email = userEmail,
            bio = userBio,
            avatarUrl = userAvatarUrl,
            isSignedIn = isUserSignedIn,
            authProvider = userAuthProvider,
            avatarColorHex = userAvatarColor,
            primaryStack = userPrimaryStack,
            avatarId = userAvatarId,
            avatarCustomUri = userAvatarCustomUri
        )

        _uiState.update {
            it.copy(
                openFiles = initialFiles,
                directoryFiles = initialFiles,
                activeFileIndex = 0,
                currentTheme = initialTheme,
                userAccount = restoredAccount,
                isSuggestionsEnabled = savedSuggestionsEnabled,
                aiSettings = AiSettings(provider = provider, selectedModel = savedModel),
                hasAiApiKey = keyStore.hasApiKey(provider)
            )
        }

        runLintForCurrentFile()
        updateSuggestions("")

        viewModelScope.launch {
            gitManager.repoStatus.collect { status ->
                _uiState.update { it.copy(gitStatus = status) }
            }
        }
    }

    // Navigation Tab
    fun setNavTab(index: Int) {
        _uiState.update { it.copy(activeNavTab = index) }
    }

    // Theme Selection (Persisted permanently!)
    fun selectTheme(theme: EditorTheme) {
        prefs.edit().putString("selected_theme_id", theme.id).apply()
        _uiState.update { it.copy(currentTheme = theme) }
    }

    // Toggle Suggestion Bar and Persist Setting (Part 5B)
    fun toggleSuggestionsEnabled() {
        val newState = !_uiState.value.isSuggestionsEnabled
        prefs.edit().putBoolean("suggestions_enabled", newState).apply()
        _uiState.update { it.copy(isSuggestionsEnabled = newState) }
    }

    // Tab Management System (Part 8)
    fun selectTab(index: Int) {
        if (index in _uiState.value.openFiles.indices) {
            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    activeFileIndex = index,
                    canUndo = false,
                    canRedo = false
                )
            }
            runLintForCurrentFile()
        }
    }

    fun closeTab(index: Int) {
        val currentList = _uiState.value.openFiles.toMutableList()
        if (index in currentList.indices && currentList.size > 1) {
            currentList.removeAt(index)
            val newActiveIndex = if (index >= currentList.size) currentList.size - 1 else index
            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    openFiles = currentList,
                    activeFileIndex = newActiveIndex,
                    canUndo = false,
                    canRedo = false
                )
            }
            runLintForCurrentFile()
        }
    }

    fun closeOtherTabs(keepIndex: Int) {
        val currentList = _uiState.value.openFiles
        if (keepIndex in currentList.indices) {
            val keptFile = currentList[keepIndex]
            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    openFiles = listOf(keptFile),
                    activeFileIndex = 0,
                    canUndo = false,
                    canRedo = false
                )
            }
        }
    }

    fun newTab() {
        createNewFileInExplorer("Untitled${_uiState.value.openFiles.size + 1}.java")
    }

    // File & Project Creation with real folder accuracy
    fun createProjectFolder(folderName: String, templateType: ProjectTemplateType = ProjectTemplateType.BLANK_FOLDER) {
        val clean = folderName.trim().replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "_").trim('_')
        if (clean.isBlank()) return

        val newFiles = templateType.defaultFiles.map { (fileName, content) ->
            CodeFile(
                name = "$clean/$fileName",
                path = "/workspace/$clean/$fileName",
                content = content
            )
        }

        val updatedDir = _uiState.value.directoryFiles + newFiles
        val updatedTracked = if (_uiState.value.trackedFolders.contains(clean)) _uiState.value.trackedFolders else _uiState.value.trackedFolders + clean

        // Automatically register web project folder into available dev server host folders
        val updatedHostFolders = if (templateType == ProjectTemplateType.WEB_PROJECT || clean.contains("web", ignoreCase = true) || clean.contains("site", ignoreCase = true)) {
            if (_uiState.value.availableHostFolders.contains(clean)) _uiState.value.availableHostFolders else _uiState.value.availableHostFolders + clean
        } else {
            _uiState.value.availableHostFolders
        }

        // Open the primary entry file if available
        val primaryFile = newFiles.firstOrNull { !it.name.endsWith(".gitkeep") && !it.name.endsWith(".md") } ?: newFiles.firstOrNull()
        val updatedOpen = if (primaryFile != null) _uiState.value.openFiles + primaryFile else _uiState.value.openFiles
        val newIndex = if (primaryFile != null) updatedOpen.size - 1 else _uiState.value.activeFileIndex

        undoStack.clear()
        redoStack.clear()

        _uiState.update {
            it.copy(
                directoryFiles = updatedDir,
                trackedFolders = updatedTracked,
                availableHostFolders = updatedHostFolders,
                openFiles = updatedOpen,
                activeFileIndex = newIndex,
                canUndo = false,
                canRedo = false,
                saveNotice = "Created folder '$clean' (${templateType.title})"
            )
        }

        runLintForCurrentFile()
        gitManager.updateFromFiles(updatedOpen)
    }

    fun createFolderInExplorer(folderName: String) {
        createProjectFolder(folderName, ProjectTemplateType.BLANK_FOLDER)
    }

    fun createNewFileInExplorer(fileName: String, parentFolder: String? = null, customContent: String? = null) {
        val cleanName = fileName.trim().removePrefix("/")
        val resolvedName = if (!parentFolder.isNullOrBlank() && parentFolder != "/" && parentFolder != "root") {
            val cleanFolder = parentFolder.trim().removePrefix("/").removeSuffix("/")
            if (cleanName.startsWith("$cleanFolder/")) cleanName else "$cleanFolder/$cleanName"
        } else {
            cleanName
        }

        val folderPart = resolvedName.substringBeforeLast('/', "").ifEmpty { null }
        val updatedTracked = if (folderPart != null && !_uiState.value.trackedFolders.contains(folderPart)) {
            _uiState.value.trackedFolders + folderPart
        } else {
            _uiState.value.trackedFolders
        }

        val defaultTemplate = NewFileTemplates.getTemplateForFile(resolvedName).first
        val finalContent = customContent ?: defaultTemplate
        val newFile = CodeFile(
            name = resolvedName,
            path = "/workspace/$resolvedName",
            content = finalContent
        )
        val updatedFiles = _uiState.value.openFiles + newFile
        val newIndex = updatedFiles.size - 1

        undoStack.clear()
        redoStack.clear()

        _uiState.update {
            it.copy(
                openFiles = updatedFiles,
                directoryFiles = _uiState.value.directoryFiles + newFile,
                trackedFolders = updatedTracked,
                activeFileIndex = newIndex,
                canUndo = false,
                canRedo = false,
                saveNotice = "Created ${newFile.name}"
            )
        }
        runLintForCurrentFile()
        gitManager.updateFromFiles(updatedFiles)
    }

    // Save Active File (Ctrl+S developer shortcut support)
    fun saveActiveFile(): String {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return "No active file"
        val updatedFiles = _uiState.value.openFiles.toMutableList()
        val savedFile = currentFile.copy(
            originalContent = currentFile.content,
            isModified = false
        )
        updatedFiles[_uiState.value.activeFileIndex] = savedFile

        val updatedDir = _uiState.value.directoryFiles.map {
            if (it.path == savedFile.path) savedFile else it
        }

        try {
            val context = getApplication<Application>()
            val targetDir = File(context.cacheDir, _uiState.value.hostedFolder).apply { mkdirs() }
            val localName = savedFile.name.substringAfterLast('/')
            File(targetDir, localName).writeText(savedFile.content)
        } catch (_: Exception) {}

        _uiState.update {
            it.copy(
                openFiles = updatedFiles,
                directoryFiles = updatedDir,
                saveNotice = "Saved ${savedFile.name} (Ctrl+S)"
            )
        }
        gitManager.updateFromFiles(updatedFiles)
        return "Saved ${savedFile.name}"
    }

    fun clearSaveNotice() {
        _uiState.update { it.copy(saveNotice = null) }
    }

    // Content modification & Undo/Redo
    fun updateActiveFileContent(newContent: String) {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        if (currentFile.content != newContent) {
            undoStack.push(currentFile.content)
            redoStack.clear()

            val updatedFiles = _uiState.value.openFiles.toMutableList()
            updatedFiles[_uiState.value.activeFileIndex] = currentFile.copy(
                content = newContent,
                isModified = newContent != currentFile.originalContent
            )

            _uiState.update {
                it.copy(
                    openFiles = updatedFiles,
                    canUndo = true,
                    canRedo = false
                )
            }

            gitManager.updateFromFiles(updatedFiles)
            debounceLintAnalysis(newContent, currentFile.language)
            updateSuggestions(newContent)
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
            redoStack.push(currentFile.content)
            val previousContent = undoStack.pop()

            val updatedFiles = _uiState.value.openFiles.toMutableList()
            updatedFiles[_uiState.value.activeFileIndex] = currentFile.copy(
                content = previousContent,
                isModified = previousContent != currentFile.originalContent
            )

            _uiState.update {
                it.copy(
                    openFiles = updatedFiles,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true
                )
            }
            runLintForCurrentFile()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
            undoStack.push(currentFile.content)
            val nextContent = redoStack.pop()

            val updatedFiles = _uiState.value.openFiles.toMutableList()
            updatedFiles[_uiState.value.activeFileIndex] = currentFile.copy(
                content = nextContent,
                isModified = nextContent != currentFile.originalContent
            )

            _uiState.update {
                it.copy(
                    openFiles = updatedFiles,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty()
                )
            }
            runLintForCurrentFile()
        }
    }

    // Applying AI Diff proposals - Fully undoable via existing undo stack (Part 9)
    fun reviewDiff(originalCode: String, proposedCode: String) {
        _uiState.update {
            it.copy(
                activeProposedDiff = ProposedDiff(originalCode, proposedCode),
                isDiffPreviewOpen = true
            )
        }
    }

    fun applyProposedDiff(newCode: String) {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        undoStack.push(currentFile.content)
        redoStack.clear()

        val updatedFiles = _uiState.value.openFiles.toMutableList()
        updatedFiles[_uiState.value.activeFileIndex] = currentFile.copy(
            content = newCode,
            isModified = true
        )

        _uiState.update {
            it.copy(
                openFiles = updatedFiles,
                canUndo = true,
                canRedo = false,
                isDiffPreviewOpen = false,
                activeProposedDiff = null
            )
        }
        runLintForCurrentFile()
        gitManager.updateFromFiles(updatedFiles)
    }

    fun dismissDiffPreview() {
        _uiState.update { it.copy(isDiffPreviewOpen = false, activeProposedDiff = null) }
    }

    // Prettier formatting
    fun formatActiveFileWithPrettier() {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val formatted = PrettierFormatter.format(currentFile.content, currentFile.language)
        if (formatted != currentFile.content) {
            updateActiveFileContent(formatted)
        }
    }

    // Linter live analysis (Part 4)
    private fun debounceLintAnalysis(content: String, language: String) {
        lintJob?.cancel()
        lintJob = viewModelScope.launch(Dispatchers.Default) {
            delay(300) // 300ms debounced live updates
            val issues = CodeLinter.analyze(content, language)
            _uiState.update { it.copy(lintIssues = issues) }
        }
    }

    private fun runLintForCurrentFile() {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        viewModelScope.launch(Dispatchers.Default) {
            val issues = CodeLinter.analyze(currentFile.content, currentFile.language)
            _uiState.update { it.copy(lintIssues = issues) }
        }
    }

    // Suggestions engine
    private fun updateSuggestions(content: String) {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val suggestions = AutocompleteEngine.getSuggestions(content, content.length, currentFile.language)
        _uiState.update { it.copy(autocompleteSuggestions = suggestions) }
    }

    fun dismissAutocomplete() {
        _uiState.update { it.copy(autocompleteSuggestions = emptyList()) }
    }

    // Sidebar & View toggles
    fun toggleExplorerSidebar() {
        _uiState.update { it.copy(isExplorerSidebarOpen = !it.isExplorerSidebarOpen) }
    }

    fun toggleSearchReplace() {
        _uiState.update { it.copy(isSearchReplaceOpen = !it.isSearchReplaceOpen) }
    }

    fun toggleMinimap() {
        _uiState.update { it.copy(isMinimapVisible = !it.isMinimapVisible) }
    }

    fun toggleZenMode() {
        _uiState.update { it.copy(isZenMode = !it.isZenMode) }
    }

    fun toggleFullScreen() {
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    // AI Assistant panel & messaging (Part 9)
    fun toggleAiPanel() {
        _uiState.update { it.copy(isAiPanelOpen = !it.isAiPanelOpen) }
    }

    fun setAiPanelOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isAiPanelOpen = isOpen) }
    }

    fun openAiWithPrompt(prompt: String) {
        _uiState.update {
            it.copy(
                isAiPanelOpen = true,
                prefilledAiPrompt = prompt
            )
        }
    }

    fun sendAiMessage(prompt: String) {
        val userMsg = AiChatMessage(role = AiRole.USER, content = prompt)
        val updatedHistory = _uiState.value.aiMessages + userMsg
        val assistantMsgId = UUID.randomUUID().toString()
        val initialAssistantMsg = AiChatMessage(id = assistantMsgId, role = AiRole.ASSISTANT, content = "Thinking...", isStreaming = true)

        _uiState.update {
            it.copy(
                aiMessages = updatedHistory + initialAssistantMsg,
                isAiLoading = true,
                prefilledAiPrompt = ""
            )
        }

        aiStreamJob?.cancel()
        aiStreamJob = viewModelScope.launch {
            aiService.streamChatResponse(_uiState.value.aiSettings, updatedHistory)
                .collect { chunk ->
                    _uiState.update { state ->
                        val messages = state.aiMessages.map { msg ->
                            if (msg.id == assistantMsgId) {
                                val newContent = if (msg.content == "Thinking...") chunk else msg.content + chunk
                                msg.copy(content = newContent, isStreaming = false)
                            } else msg
                        }
                        state.copy(aiMessages = messages)
                    }
                }
            _uiState.update { it.copy(isAiLoading = false) }
        }
    }

    fun stopAiGeneration() {
        aiStreamJob?.cancel()
        _uiState.update { it.copy(isAiLoading = false) }
    }

    fun clearAiChat() {
        _uiState.update { it.copy(aiMessages = emptyList()) }
    }

    fun updateAiSettings(newSettings: AiSettings) {
        prefs.edit()
            .putString("ai_provider", newSettings.provider.name)
            .putString("ai_model", newSettings.selectedModel)
            .apply()

        _uiState.update {
            it.copy(
                aiSettings = newSettings,
                hasAiApiKey = keyStore.hasApiKey(newSettings.provider)
            )
        }
    }

    // Git Actions
    fun stageGitFile(path: String) = gitManager.stageFile(path)
    fun unstageGitFile(path: String) = gitManager.unstageFile(path)
    fun stageAllGitFiles() = gitManager.stageAll()
    fun unstageAllGitFiles() = gitManager.unstageAll()
    fun commitGitChanges(msg: String) = gitManager.commit(msg)
    fun pushGitChanges(): String = gitManager.push()

    // File Explorer File Actions
    fun openFile(file: CodeFile) {
        val existingIndex = _uiState.value.openFiles.indexOfFirst { it.path == file.path }
        if (existingIndex != -1) {
            selectTab(existingIndex)
        } else {
            val updated = _uiState.value.openFiles + file
            undoStack.clear()
            redoStack.clear()
            _uiState.update {
                it.copy(
                    openFiles = updated,
                    activeFileIndex = updated.size - 1,
                    canUndo = false,
                    canRedo = false
                )
            }
            runLintForCurrentFile()
        }
    }

    fun loadDirectory(dir: String) {
        _uiState.update { it.copy(currentDirectory = dir) }
    }

    fun navigateUp() {
        val cur = _uiState.value.currentDirectory
        val parent = cur.substringBeforeLast('/', "").ifEmpty { "/workspace" }
        _uiState.update { it.copy(currentDirectory = parent) }
    }

    fun deleteFileInExplorer(file: CodeFile) {
        val updatedDir = _uiState.value.directoryFiles.filter { it.path != file.path }
        val updatedOpen = _uiState.value.openFiles.filter { it.path != file.path }
        _uiState.update {
            it.copy(
                directoryFiles = updatedDir,
                openFiles = if (updatedOpen.isEmpty()) listOf(CodeFile(name = "Example.java", path = "/workspace/Example.java", content = "")) else updatedOpen,
                activeFileIndex = 0
            )
        }
    }

    // Real Multi-Language Code Execution Engine
    // Real Multi-Language Code Execution Engine
    fun runActiveFile() {
        val currentFile = _uiState.value.openFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val ext = currentFile.name.substringAfterLast('.', "").lowercase()
        if (ext == "html" || ext == "htm") {
            startDevServer(8080)
            openWebPreview(currentFile.name)
        }
        val result = com.example.engine.CodeExecutionEngine.execute(currentFile, _uiState.value.openFiles)
        val newLines = _uiState.value.terminalLines + listOf(
            "$ ${result.command}",
            "[Runtime] Executing ${currentFile.language} (${currentFile.name})..."
        ) + result.output + listOf("[Completed in ${result.executionTimeMs}ms with exit code ${result.exitCode}]")
        _uiState.update { it.copy(terminalLines = newLines) }
    }

    // Dev Server
    fun startDevServer(port: Int = 8080, folderName: String? = null) {
        val context = getApplication<Application>()
        val folder = folderName ?: _uiState.value.hostedFolder
        val targetDir = File(context.cacheDir, folder).apply { mkdirs() }
        // Sync open files to target directory so all website files are available
        _uiState.value.openFiles.forEach { file ->
            try {
                File(targetDir, file.name).writeText(file.content)
            } catch (_: Exception) {}
        }
        val started = localDevServer.start(targetDir, port)
        _uiState.update {
            it.copy(
                hostedFolder = folder,
                serverStatus = if (started) "Running on http://localhost:$port (/$folder)" else "Offline"
            )
        }
    }

    fun setDevServerHostFolder(folderName: String) {
        val cleanName = folderName.trim().replace(Regex("[/\\\\]"), "").ifEmpty { "www" }
        val currentList = _uiState.value.availableHostFolders
        val updatedFolders = if (currentList.contains(cleanName)) currentList else currentList + cleanName
        _uiState.update { it.copy(hostedFolder = cleanName, availableHostFolders = updatedFolders) }
        startDevServer(folderName = cleanName)
    }

    fun stopDevServer() {
        localDevServer.stop()
        _uiState.update { it.copy(serverStatus = "Stopped") }
    }

    fun openWebPreview(fileName: String? = null) {
        startDevServer()
        val target = fileName ?: _uiState.value.openFiles.find { it.name.endsWith(".html", ignoreCase = true) }?.name ?: "index.html"
        _uiState.update {
            it.copy(
                isWebPreviewOpen = true,
                activeWebPreviewFileName = target
            )
        }
    }

    fun closeWebPreview() {
        _uiState.update { it.copy(isWebPreviewOpen = false) }
    }

    // Interactive Termux-compatible Terminal Shell
    fun sendTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed == "clear") {
            _uiState.update { it.copy(terminalLines = emptyList()) }
            return
        }

        val promptLine = "$ $trimmed"
        val outputLines = com.example.engine.CodeExecutionEngine.executeCommand(
            rawCommand = trimmed,
            openFiles = _uiState.value.openFiles,
            onNewFile = { name, content -> createNewFileInExplorer(name, content) },
            onDeleteFile = { name ->
                val f = _uiState.value.openFiles.find { it.name.equals(name, ignoreCase = true) }
                if (f != null) deleteFileInExplorer(f)
            }
        )
        val updated = _uiState.value.terminalLines + listOf(promptLine) + outputLines
        _uiState.update { it.copy(terminalLines = updated) }
    }

    fun clearTerminal() {
        _uiState.update { it.copy(terminalLines = emptyList()) }
    }

    fun exitFullScreen() {
        _uiState.update { it.copy(isFullScreen = false, isZenMode = false) }
    }

    // Permanent Google Authentication & Profile Management
    fun signInWithGoogle(name: String, email: String) {
        val updated = _uiState.value.userAccount.copy(
            username = name.ifBlank { "Google Developer" },
            email = email.ifBlank { "developer@gmail.com" },
            title = "Verified Google Developer",
            isSignedIn = true,
            authProvider = "Google"
        )
        saveUserAccountToPrefs(updated)
        _uiState.update { it.copy(userAccount = updated) }
    }

    fun signOut() {
        val reset = UserAccount(
            username = "linq_dev",
            title = "Mobile Software Engineer",
            email = "developer@linqstudio.dev",
            bio = "Building high-performance cross-platform software on mobile.",
            isSignedIn = false,
            authProvider = "Guest"
        )
        saveUserAccountToPrefs(reset)
        _uiState.update { it.copy(userAccount = reset) }
    }

    fun deleteFolderInExplorer(folderName: String) {
        val clean = folderName.trim().removePrefix("/").removeSuffix("/")
        val updatedTracked = _uiState.value.trackedFolders.filter { it != clean }
        val updatedDir = _uiState.value.directoryFiles.filter { !it.name.startsWith("$clean/") }
        val updatedOpen = _uiState.value.openFiles.filter { !it.name.startsWith("$clean/") }
        val newIndex = if (updatedOpen.isEmpty()) 0 else _uiState.value.activeFileIndex.coerceIn(0, updatedOpen.size - 1)
        _uiState.update {
            it.copy(
                trackedFolders = updatedTracked,
                directoryFiles = updatedDir,
                openFiles = if (updatedOpen.isEmpty() && updatedDir.isNotEmpty()) listOf(updatedDir.first()) else updatedOpen,
                activeFileIndex = newIndex,
                saveNotice = "Deleted folder '$clean'"
            )
        }
    }

    fun updateUserProfile(
        username: String,
        title: String,
        bio: String,
        avatar: String?,
        colorHex: Long = 0xFF00E5FF,
        stack: String = "Kotlin • Python • JavaScript",
        avatarId: String = "avatar_terminal",
        avatarCustomUri: String? = null
    ) {
        val updated = _uiState.value.userAccount.copy(
            username = username.ifBlank { "linq_dev" },
            title = title.ifBlank { "Software Engineer" },
            bio = bio,
            avatarUrl = avatar,
            avatarColorHex = colorHex,
            primaryStack = stack.ifBlank { "Kotlin • Python • JavaScript" },
            avatarId = avatarId,
            avatarCustomUri = avatarCustomUri ?: _uiState.value.userAccount.avatarCustomUri
        )
        saveUserAccountToPrefs(updated)
        _uiState.update { it.copy(userAccount = updated) }
    }

    fun resetWorkspace() {
        val javaFile = CodeFile(
            name = "Example.java",
            path = "/workspace/Example.java",
            content = "public class Example {\n    public static void main(String[] args) {\n        System.out.println(\"Welcome to Linq Studio!\");\n    }\n}\n"
        )
        val pythonFile = CodeFile(
            name = "main.py",
            path = "/workspace/main.py",
            content = "def calculate_factorial(n):\n    if n <= 1:\n        return 1\n    return n * calculate_factorial(n - 1)\n\nprint(\"Factorial 5:\", calculate_factorial(5))\n"
        )
        val initial = listOf(javaFile, pythonFile)
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                openFiles = initial,
                directoryFiles = initial,
                activeFileIndex = 0,
                canUndo = false,
                canRedo = false
            )
        }
        runLintForCurrentFile()
    }

    private fun saveUserAccountToPrefs(account: UserAccount) {
        prefs.edit()
            .putBoolean("user_is_signed_in", account.isSignedIn)
            .putString("user_username", account.username)
            .putString("user_title", account.title)
            .putString("user_email", account.email)
            .putString("user_bio", account.bio)
            .putString("user_avatar_url", account.avatarUrl)
            .putString("user_auth_provider", account.authProvider)
            .putLong("user_avatar_color_hex", account.avatarColorHex)
            .putString("user_primary_stack", account.primaryStack)
            .putString("user_avatar_id", account.avatarId)
            .putString("user_avatar_custom_uri", account.avatarCustomUri)
            .apply()
    }
}
