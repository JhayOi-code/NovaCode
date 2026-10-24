package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.model.CodeFile
import com.example.ui.components.AiSettingsDialog
import com.example.ui.components.AppLoadingScreen
import com.example.ui.components.UniqueWelcomeDialog
import com.example.ui.components.WebPreviewDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageGuideData
import com.example.ui.screens.LanguageGuideScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.LinqStudioTheme
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val theme = uiState.currentTheme

            LinqStudioTheme {
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences("linq_prefs", Context.MODE_PRIVATE) }
                val hasSeenWelcome = remember { prefs.getBoolean("has_seen_welcome", false) }

                var isAppLoaded by remember { mutableStateOf(false) }
                var showWelcomeDialog by remember { mutableStateOf(false) }

                var selectedLanguageGuide by remember { mutableStateOf<LanguageGuideData?>(null) }
                var showAiSettingsDialog by remember { mutableStateOf(false) }

                if (!isAppLoaded) {
                    AppLoadingScreen(
                        theme = theme,
                        onLoadingComplete = {
                            isAppLoaded = true
                            if (!hasSeenWelcome) {
                                showWelcomeDialog = true
                            }
                        }
                    )
                } else {
                    BackHandler(enabled = uiState.isFullScreen || uiState.isZenMode || selectedLanguageGuide != null || uiState.activeNavTab != 0) {
                    if (uiState.isFullScreen || uiState.isZenMode) {
                        viewModel.exitFullScreen()
                    } else if (selectedLanguageGuide != null) {
                        selectedLanguageGuide = null
                    } else {
                        viewModel.setNavTab(0)
                    }
                }

                val navItems = listOf(
                    Triple(0, "Home", Icons.Default.Home),
                    Triple(1, "Editor", Icons.Default.Code),
                    Triple(2, "Files", Icons.Default.Folder),
                    Triple(3, "Terminal", Icons.Default.Terminal),
                    Triple(4, "Settings", Icons.Default.Settings),
                    Triple(5, "About", Icons.Default.Info)
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(theme.editorBackground)
                ) {
                    val isWideScreen = maxWidth >= 600.dp

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = theme.editorBackground,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            if (!uiState.isFullScreen && !uiState.isZenMode && !isWideScreen) {
                                NavigationBar(
                                    containerColor = theme.surfaceColor,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("bottom_nav_bar")
                                ) {
                                    navItems.forEach { (tabIndex, label, icon) ->
                                        val isSelected = uiState.activeNavTab == tabIndex
                                        NavigationBarItem(
                                            selected = isSelected,
                                            alwaysShowLabel = false,
                                            onClick = { viewModel.setNavTab(tabIndex) },
                                            icon = {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = theme.primaryColor.copy(alpha = 0.18f),
                                                selectedIconColor = theme.primaryColor,
                                                unselectedIconColor = theme.lineNumberColor
                                            ),
                                            modifier = Modifier.testTag("nav_tab_$label")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(if (uiState.isFullScreen || uiState.isZenMode) PaddingValues(0.dp) else innerPadding)
                            ) {
                            // Adaptive Navigation Rail for Wide Screens
                            if (!uiState.isFullScreen && !uiState.isZenMode && isWideScreen) {
                                NavigationRail(
                                    containerColor = theme.surfaceColor,
                                    header = {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = theme.primaryColor.copy(alpha = 0.15f),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_linq_user_logo),
                                                        contentDescription = "Linq Studios",
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.toggleFullScreen() },
                                                modifier = Modifier.size(32.dp).testTag("rail_fullscreen_toggle")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ZoomOutMap,
                                                    contentDescription = "Full Screen",
                                                    tint = if (uiState.isFullScreen) theme.primaryColor else theme.lineNumberColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxHeight().testTag("navigation_rail")
                                ) {
                                    navItems.forEach { (tabIndex, label, icon) ->
                                        val isSelected = uiState.activeNavTab == tabIndex
                                        NavigationRailItem(
                                            selected = isSelected,
                                            onClick = { viewModel.setNavTab(tabIndex) },
                                            icon = {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                indicatorColor = theme.primaryColor.copy(alpha = 0.18f)
                                            ),
                                            modifier = Modifier.testTag("nav_rail_$label")
                                        )
                                    }
                                }
                                VerticalDivider(color = theme.borderColor, thickness = 1.dp)
                            }

                            // Main Viewport with Animated Transitions
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(theme.editorBackground)
                            ) {
                                AnimatedContent(
                                    targetState = uiState.activeNavTab,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "tab_content_transition"
                                ) { activeTab ->
                                    when (activeTab) {
                                        0 -> {
                                            if (selectedLanguageGuide != null) {
                                                LanguageGuideScreen(
                                                    theme = theme,
                                                    guide = selectedLanguageGuide!!,
                                                    onBack = { selectedLanguageGuide = null },
                                                    onOpenStarterCode = { name, code ->
                                                        viewModel.createNewFileInExplorer(name, code)
                                                        viewModel.setNavTab(1)
                                                        selectedLanguageGuide = null
                                                    }
                                                )
                                            } else {
                                                HomeScreen(
                                                    theme = theme,
                                                    recentFiles = uiState.openFiles,
                                                    serverStatus = uiState.serverStatus,
                                                    userAccount = uiState.userAccount,
                                                    isFullScreen = uiState.isFullScreen,
                                                    onToggleFullScreen = { viewModel.toggleFullScreen() },
                                                    onOpenRecent = { file ->
                                                        val idx = uiState.openFiles.indexOf(file)
                                                        if (idx != -1) viewModel.selectTab(idx)
                                                        viewModel.setNavTab(1)
                                                    },
                                                    onNewFile = {
                                                        viewModel.newTab()
                                                        viewModel.setNavTab(1)
                                                    },
                                                    onOpenWorkspace = { viewModel.setNavTab(2) },
                                                    onOpenLanguageGuide = { guide ->
                                                        selectedLanguageGuide = guide
                                                    },
                                                    onOpenWebDemo = { viewModel.setNavTab(1) },
                                                    onStartDevServer = { viewModel.startDevServer() },
                                                    onStopDevServer = { viewModel.stopDevServer() },
                                                    onOpenWebPreview = { viewModel.openWebPreview(it) },
                                                    onUpdateProfile = { u, t, b, a, c, s, id, uri -> viewModel.updateUserProfile(u, t, b, a, c, s, id, uri) },
                                                    onSignInWithGoogle = { name, email -> viewModel.signInWithGoogle(name, email) },
                                                    onSignOut = { viewModel.signOut() },
                                                    onResetWorkspace = { viewModel.resetWorkspace() }
                                                )
                                            }
                                        }

                                        1 -> EditorScreen(
                                            theme = theme,
                                            openFiles = uiState.openFiles,
                                            activeFileIndex = uiState.activeFileIndex,
                                            isExplorerSidebarOpen = uiState.isExplorerSidebarOpen,
                                            isSearchReplaceOpen = uiState.isSearchReplaceOpen,
                                            lintIssues = uiState.lintIssues,
                                            isFullScreen = uiState.isFullScreen,
                                            isZenMode = uiState.isZenMode,
                                            isMinimapVisible = uiState.isMinimapVisible,
                                            gitStatus = uiState.gitStatus,
                                            autocompleteSuggestions = uiState.autocompleteSuggestions,
                                            prettierFormatNotice = uiState.prettierFormatNotice,
                                            canUndo = uiState.canUndo,
                                            canRedo = uiState.canRedo,
                                            isAiPanelOpen = uiState.isAiPanelOpen,
                                            aiMessages = uiState.aiMessages,
                                            isAiLoading = uiState.isAiLoading,
                                            activeProposedDiff = uiState.activeProposedDiff,
                                            isDiffPreviewOpen = uiState.isDiffPreviewOpen,
                                            prefilledAiPrompt = uiState.prefilledAiPrompt,
                                            isSuggestionsEnabled = uiState.isSuggestionsEnabled,
                                            onSelectTab = { viewModel.selectTab(it) },
                                            onCloseTab = { viewModel.closeTab(it) },
                                            onCloseOtherTabs = { viewModel.closeOtherTabs(it) },
                                            onNewTab = { viewModel.newTab() },
                                            onContentChange = { viewModel.updateActiveFileContent(it) },
                                            onSaveCurrent = { viewModel.saveActiveFile() },
                                            onRunCode = { viewModel.runActiveFile() },
                                            onFormatWithPrettier = { viewModel.formatActiveFileWithPrettier() },
                                            onToggleZenMode = { viewModel.toggleZenMode() },
                                            onToggleMinimap = { viewModel.toggleMinimap() },
                                            onSelectAutocompleteSuggestion = {
                                                val currentFile = uiState.openFiles.getOrNull(uiState.activeFileIndex)
                                                if (currentFile != null) {
                                                    viewModel.updateActiveFileContent(currentFile.content + it.insertText)
                                                }
                                            },
                                            onDismissAutocomplete = { viewModel.dismissAutocomplete() },
                                            onStageGitFile = { viewModel.stageGitFile(it) },
                                            onStageAllGitFiles = { viewModel.stageAllGitFiles() },
                                            onUnstageGitFile = { viewModel.unstageGitFile(it) },
                                            onUnstageAllGitFiles = { viewModel.unstageAllGitFiles() },
                                            onCommitGit = { viewModel.commitGitChanges(it) },
                                            onPushGit = { viewModel.pushGitChanges() },
                                            onToggleExplorerSidebar = { viewModel.toggleExplorerSidebar() },
                                            onOpenFileFromSidebar = { viewModel.openFile(it) },
                                            onNewFileInSidebar = { viewModel.createNewFileInExplorer(it) },
                                            trackedFolders = uiState.trackedFolders,
                                            onNewFolderInSidebar = { viewModel.createFolderInExplorer(it) },
                                            onNewFolderInSidebarWithTemplate = { name, template -> viewModel.createProjectFolder(name, template) },
                                            onNewFileInSidebarWithFolder = { folder, name -> viewModel.createNewFileInExplorer(name, folder) },
                                            onDeleteFolderInSidebar = { viewModel.deleteFolderInExplorer(it) },
                                            onToggleSearchReplace = { viewModel.toggleSearchReplace() },
                                            onUndo = { viewModel.undo() },
                                            onRedo = { viewModel.redo() },
                                            onToggleAiPanel = { viewModel.toggleAiPanel() },
                                            onCloseAiPanel = { viewModel.setAiPanelOpen(false) },
                                            onSendAiMessage = { viewModel.sendAiMessage(it) },
                                            onStopAiGenerating = { viewModel.stopAiGeneration() },
                                            onClearAiChat = { viewModel.clearAiChat() },
                                            onOpenAiWithPrompt = { viewModel.openAiWithPrompt(it) },
                                            onReviewDiff = { orig, prop -> viewModel.reviewDiff(orig, prop) },
                                            onApplyDiff = { viewModel.applyProposedDiff(it) },
                                            onRejectDiff = { viewModel.dismissDiffPreview() },
                                            onToggleSuggestionsEnabled = { viewModel.toggleSuggestionsEnabled() },
                                            onOpenAiSettings = { showAiSettingsDialog = true },
                                            onOpenTerminal = { viewModel.setNavTab(3) }
                                        )

                                        2 -> FilesScreen(
                                            theme = theme,
                                            files = uiState.directoryFiles,
                                            currentDirectory = uiState.currentDirectory,
                                            trackedFolders = uiState.trackedFolders,
                                            onOpenFile = { file ->
                                                viewModel.openFile(file)
                                                viewModel.setNavTab(1)
                                            },
                                            onNavigateDir = { viewModel.loadDirectory(it) },
                                            onNavigateUp = { viewModel.navigateUp() },
                                            onNewFile = {
                                                viewModel.createNewFileInExplorer(it)
                                                viewModel.setNavTab(1)
                                            },
                                            onNewFolder = { viewModel.createFolderInExplorer(it) },
                                            onNewFolderWithTemplate = { name, template ->
                                                viewModel.createProjectFolder(name, template)
                                                viewModel.setNavTab(1)
                                            },
                                            onDeleteFolder = { viewModel.deleteFolderInExplorer(it) },
                                            onDelete = { viewModel.deleteFileInExplorer(it) }
                                        )

                                        3 -> TerminalScreen(
                                            theme = theme,
                                            terminalLines = uiState.terminalLines,
                                            isFullScreen = uiState.isFullScreen,
                                            onToggleFullScreen = { viewModel.toggleFullScreen() },
                                            onSendCommand = { viewModel.sendTerminalCommand(it) },
                                            onClearTerminal = { viewModel.clearTerminal() }
                                        )

                                        4 -> SettingsScreen(
                                            currentTheme = theme,
                                            userAccount = uiState.userAccount,
                                            aiSettings = uiState.aiSettings,
                                            hasApiKey = uiState.hasAiApiKey,
                                            isSuggestionsEnabled = uiState.isSuggestionsEnabled,
                                            isMinimapVisible = uiState.isMinimapVisible,
                                            openFilesCount = uiState.openFiles.size,
                                            activeFileName = uiState.openFiles.getOrNull(uiState.activeFileIndex)?.name ?: "",
                                            serverStatus = uiState.serverStatus,
                                            onSelectTheme = { viewModel.selectTheme(it) },
                                            onUpdateProfile = { u, t, b, a, c, s, id, uri -> viewModel.updateUserProfile(u, t, b, a, c, s, id, uri) },
                                            onSignInWithGoogle = { name, email -> viewModel.signInWithGoogle(name, email) },
                                            onSignOut = { viewModel.signOut() },
                                            onResetWorkspace = { viewModel.resetWorkspace() },
                                            onToggleSuggestionsEnabled = { viewModel.toggleSuggestionsEnabled() },
                                            onToggleMinimap = { viewModel.toggleMinimap() },
                                            onOpenAiSettings = { showAiSettingsDialog = true },
                                            onShowWelcomeGuide = { showWelcomeDialog = true }
                                        )

                                        5 -> AboutScreen(theme = theme)
                                    }
                                }
                            }
                        }

                        // FLOATING EXIT FULL SCREEN PILL (Ensures user is NEVER trapped in Fullscreen)
                        if (uiState.isFullScreen || uiState.isZenMode) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = theme.primaryColor,
                                shadowElevation = 12.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 48.dp, end = 16.dp)
                                    .clickable { viewModel.exitFullScreen() }
                                    .testTag("exit_fullscreen_floating_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FullscreenExit,
                                        contentDescription = "Exit Full Screen",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Exit Full Screen",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                    // AI Assistant Settings Dialog
                    if (showAiSettingsDialog) {
                        AiSettingsDialog(
                            theme = theme,
                            settings = uiState.aiSettings,
                            keyStore = viewModel.keyStore,
                            onSaveSettings = {
                                viewModel.updateAiSettings(it)
                                showAiSettingsDialog = false
                            },
                            onTestConnection = { testSettings, callback ->
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    val res = viewModel.aiService.testConnection(testSettings)
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        callback(res)
                                    }
                                }
                            },
                            onDismiss = { showAiSettingsDialog = false }
                        )
                    }

                    // Local Dev Server Web Preview Dialog (Tests multiple website files locally)
                    if (uiState.isWebPreviewOpen) {
                        WebPreviewDialog(
                            theme = theme,
                            files = uiState.openFiles,
                            initialFileName = uiState.activeWebPreviewFileName,
                            serverStatus = viewModel.localDevServer.status,
                            currentHostFolder = uiState.hostedFolder,
                            availableFolders = uiState.availableHostFolders,
                            onSelectHostFolder = { viewModel.setDevServerHostFolder(it) },
                            onCreateHostFolder = { viewModel.setDevServerHostFolder(it) },
                            onRestartServer = { viewModel.startDevServer() },
                            onClose = { viewModel.closeWebPreview() }
                        )
                    }

                    if (showWelcomeDialog) {
                        UniqueWelcomeDialog(
                            theme = theme,
                            userAccount = uiState.userAccount,
                            onDismiss = {
                                showWelcomeDialog = false
                                prefs.edit().putBoolean("has_seen_welcome", true).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}
}
