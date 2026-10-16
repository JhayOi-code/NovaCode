package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PluginQuickSheet
import com.example.ui.components.WebPreviewDialog
import com.example.ui.screens.*
import com.example.ui.theme.LinqStudioTheme
import com.example.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        try {
            java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js").mkdirs()
            java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm").mkdirs()
        } catch (_: Exception) {}

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val theme = uiState.currentTheme

            LinqStudioTheme {
                BackHandler(enabled = uiState.activeNavTab != 0) {
                    viewModel.setNavTab(0)
                }

                val navItems = listOf(
                    Triple(0, "Home", Icons.Default.Home),
                    Triple(1, "Editor", Icons.Default.Code),
                    Triple(2, "Files", Icons.Default.Folder),
                    Triple(3, "Terminal", Icons.Default.Terminal),
                    Triple(4, "Plugins", Icons.Default.Extension),
                    Triple(5, "Settings", Icons.Default.Settings),
                    Triple(6, "About", Icons.Default.Info)
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
                                    tonalElevation = 4.dp,
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
                                                    maxLines = 1,
                                                    softWrap = false
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
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (uiState.isFullScreen || uiState.isZenMode) PaddingValues(0.dp) else innerPadding)
                        ) {
                            // WIDE SCREEN ADAPTIVE NAVIGATION RAIL (Tablets, Foldables, Landscape)
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
                                                        painter = painterResource(id = R.drawable.ic_linq_logo),
                                                        contentDescription = "Linq Studio",
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                            }

                                            // Full Screen toggle button in rail header
                                            IconButton(
                                                onClick = { viewModel.toggleFullScreen() },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("btn_rail_fullscreen")
                                            ) {
                                                Icon(
                                                    Icons.Default.Fullscreen,
                                                    contentDescription = "Full Screen",
                                                    tint = theme.primaryColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .testTag("navigation_rail")
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
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) theme.primaryColor else theme.lineNumberColor
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

                            // MAIN SCREEN VIEWPORT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(theme.editorBackground)
                            ) {
                                when (uiState.activeNavTab) {
                                    0 -> HomeScreen(
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
                                        onOpenPlugins = { viewModel.setNavTab(4) },
                                        onOpenStarterFile = { fileName ->
                                            viewModel.openFileByName(fileName)
                                            viewModel.setNavTab(1)
                                        },
                                        onOpenWebDemo = { viewModel.openWebDemo() },
                                        onStartDevServer = { viewModel.startDevServer() },
                                        onStopDevServer = { viewModel.stopDevServer() },
                                        onUpdateProfile = { u, t, a, p -> viewModel.updateUserProfile(u, t, a, p) }
                                    )

                                    1 -> EditorScreen(
                                        theme = theme,
                                        openFiles = uiState.openFiles,
                                        activeFileIndex = uiState.activeFileIndex,
                                        directoryFiles = uiState.directoryFiles,
                                        isExplorerSidebarOpen = uiState.isExplorerSidebarOpen,
                                        isTemplatesPanelOpen = uiState.isTemplatesPanelOpen,
                                        isSearchReplaceOpen = uiState.isSearchReplaceOpen,
                                        lintIssues = uiState.lintIssues,
                                        capturedLogs = uiState.capturedSandboxLogs,
                                        showTerminalDrawer = uiState.showTerminalDrawer,
                                        isTerminalDrawerExpanded = uiState.isTerminalDrawerExpanded,
                                        executionResult = uiState.executionResult,
                                        showOutputConsole = uiState.showOutputConsole,
                                        isFullScreen = uiState.isFullScreen,
                                        isZenMode = uiState.isZenMode,
                                        isMinimapVisible = uiState.isMinimapVisible,
                                        gitStatus = uiState.gitStatus,
                                        autocompleteSuggestions = uiState.autocompleteSuggestions,
                                        prettierFormatNotice = uiState.prettierFormatNotice,
                                        onSelectTab = { viewModel.selectTab(it) },
                                        onCloseTab = { viewModel.closeTab(it) },
                                        onCloseAllTabs = { viewModel.closeAllTabs() },
                                        onCloseOtherTabs = { viewModel.closeOtherTabs(it) },
                                        onSaveAllTabs = { viewModel.saveAllTabs() },
                                        onNewTab = { viewModel.newTab() },
                                        onContentChange = { viewModel.updateActiveFileContent(it) },
                                        onSaveCurrent = { viewModel.saveActiveFile() },
                                        onRunCode = { viewModel.runActiveFile() },
                                        onFormatWithPrettier = { viewModel.formatActiveFileWithPrettier() },
                                        onToggleZenMode = { viewModel.toggleZenMode() },
                                        onToggleMinimap = { viewModel.toggleMinimap() },
                                        onSelectAutocompleteSuggestion = { viewModel.acceptSuggestion(it) },
                                        onDismissAutocomplete = { viewModel.dismissAutocomplete() },
                                        onStageGitFile = { viewModel.stageGitFile(it) },
                                        onStageAllGitFiles = { viewModel.stageAllGitFiles() },
                                        onUnstageGitFile = { viewModel.unstageGitFile(it) },
                                        onUnstageAllGitFiles = { viewModel.unstageAllGitFiles() },
                                        onCommitGit = { viewModel.commitGitChanges(it) },
                                        onPushGit = { viewModel.pushGitChanges() },
                                        onCreateGitBranch = { viewModel.createGitBranch(it) },
                                        onSwitchGitBranch = { viewModel.switchGitBranch(it) },
                                        onToggleExplorerSidebar = { viewModel.toggleExplorerSidebar() },
                                        onOpenFileFromSidebar = { file -> viewModel.openFile(file) },
                                        onNewFileInSidebar = { name -> viewModel.createNewFileInExplorer(name) },
                                        onToggleTemplatesPanel = { viewModel.toggleTemplatesPanel() },
                                        onInsertTemplateSnippet = { viewModel.insertTemplateSnippet(it) },
                                        onCreateFileFromTemplate = { name, content -> viewModel.createFileFromTemplate(name, content) },
                                        onToggleSearchReplace = { viewModel.toggleSearchReplace() },
                                        onToggleTerminalDrawer = { viewModel.toggleTerminalDrawer() },
                                        onToggleTerminalDrawerExpand = { viewModel.setTerminalDrawerExpanded(!uiState.isTerminalDrawerExpanded) },
                                        onClearSandboxLogs = { viewModel.clearSandboxLogs() },
                                        onCloseTerminalDrawer = { viewModel.setShowTerminalDrawer(false) },
                                        onSendTerminalDrawerCommand = { cmd -> viewModel.sendTerminalCommand(cmd) },
                                        onCloseOutputConsole = { viewModel.closeOutputConsole() },
                                        onToggleFullScreen = { viewModel.toggleFullScreen() },
                                        onOpenWebPreview = { viewModel.openWebPreviewForActiveFile() },
                                        onOpenPlugins = { viewModel.togglePluginSheet(true) }
                                    )

                                    2 -> FilesScreen(
                                        theme = theme,
                                        files = uiState.directoryFiles,
                                        currentDirectory = uiState.currentDirectory,
                                        onOpenFile = { file ->
                                            viewModel.openFile(file)
                                            viewModel.setNavTab(1)
                                        },
                                        onNavigateDir = { viewModel.loadDirectory(it) },
                                        onNavigateUp = { viewModel.navigateUp() },
                                        onNewFile = { viewModel.createNewFileInExplorer(it) },
                                        onNewFolder = { viewModel.createNewFolderInExplorer(it) },
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

                                    4 -> PluginsScreen(
                                        theme = theme,
                                        pluginManager = viewModel.pluginManager,
                                        plugins = uiState.plugins,
                                        onOpenEditorWithPlugin = { viewModel.setNavTab(1) }
                                    )

                                    5 -> SettingsScreen(
                                        currentTheme = theme,
                                        userAccount = uiState.userAccount,
                                        onSelectTheme = { viewModel.selectTheme(it) },
                                        onUpdateProfile = { u, t, a, p -> viewModel.updateUserProfile(u, t, a, p) },
                                        onOpenPlugins = { viewModel.setNavTab(4) }
                                    )

                                    6 -> AboutScreen(theme = theme)
                                }

                                // GLOBAL FLOATING EXIT FULL SCREEN BUTTON
                                if (uiState.isFullScreen) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = theme.surfaceColor.copy(alpha = 0.92f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                                        shadowElevation = 6.dp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(14.dp)
                                            .testTag("btn_global_exit_fullscreen")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.toggleFullScreen() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.FullscreenExit,
                                                    contentDescription = "Exit Full Screen",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Text(
                                                text = "Fit Screen",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }

                                // Web Live Preview Dialog
                                if (uiState.previewUrl != null || uiState.previewHtml != null) {
                                    WebPreviewDialog(
                                        theme = theme,
                                        serverUrl = uiState.previewUrl,
                                        htmlContent = uiState.previewHtml,
                                        serverStatus = uiState.serverStatus,
                                        onRestartServer = { viewModel.restartDevServer() },
                                        onClose = { viewModel.closePreview() },
                                        onConsoleLog = { level, msg, src, line ->
                                            viewModel.addSandboxConsoleLog(level, msg, src, line)
                                        }
                                    )
                                }

                                // Plugins Quick Action Sheet
                                if (uiState.showPluginSheet) {
                                    PluginQuickSheet(
                                        theme = theme,
                                        installedPlugins = uiState.plugins.filter { it.isInstalled },
                                        onDismiss = { viewModel.togglePluginSheet(false) },
                                        onFormatDocument = { viewModel.formatActiveFileWithPrettier() },
                                        onRunDiagnostics = { viewModel.lintActiveFile() },
                                        onShowStats = { viewModel.showActiveStats() },
                                        onShowTodos = { viewModel.showActiveTodos() },
                                        onOpenMarketplace = { viewModel.setNavTab(4) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
