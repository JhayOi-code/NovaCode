package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.EditorTheme
import java.io.File
import com.example.storage.StoragePermissionHelper
import com.example.permissions.rememberPermissionManager
import com.example.ui.components.GitPanelSheet
import com.example.ui.components.PermissionWizardDialog
import com.example.ui.components.WebPreviewDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize crash reporting hook and memory manager
        com.example.crash.CrashReporter.init(this)
        com.example.memory.MemoryManager.init(this)

        // Clean up any empty/corrupted dummy cache directories left from previous builds
        cleanupCorruptedWebViewCache()

        // Check if opened via "Open with" / "Share to" Intent
        intent?.data?.let { uri ->
            viewModel.openFromUri(uri)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val theme = uiState.currentTheme
            val permissionManager = rememberPermissionManager()

            // Dynamic screen orientation: flip to landscape when phone is rotated
            LaunchedEffect(uiState.screenOrientationMode) {
                val targetOrientation = when (uiState.screenOrientationMode) {
                    "portrait" -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    "landscape" -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                }
                if (requestedOrientation != targetOrientation) {
                    requestedOrientation = targetOrientation
                }
            }

            com.example.ui.components.GlobalErrorBoundary(
                theme = theme,
                onRestartApp = {
                    val restartIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (restartIntent != null) {
                        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(restartIntent)
                    }
                    finish()
                },
                onResetEditor = {
                    viewModel.restartEditorService()
                }
            ) {

            // Activity result launcher for SAF folder tree grant
            val safFolderLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val uri = result.data?.data
                if (uri != null) {
                    val success = permissionManager.handleSafFolderResult(uri)
                    if (success) {
                        viewModel.refreshPermissions()
                    }
                }
            }

            // Activity result launcher for All Files Access settings
            val allFilesSettingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) {
                permissionManager.refreshState()
                viewModel.refreshPermissions()
            }

            // Sync MaterialTheme with Linq Studio Theme Engine
            val m3Colors = if (theme.isDark) {
                darkColorScheme(
                    primary = theme.primaryColor,
                    onPrimary = theme.onPrimaryColor,
                    secondary = theme.secondaryColor,
                    background = theme.editorBackground,
                    surface = theme.surfaceColor,
                    onSurface = theme.onSurfaceColor
                )
            } else {
                lightColorScheme(
                    primary = theme.primaryColor,
                    onPrimary = theme.onPrimaryColor,
                    secondary = theme.secondaryColor,
                    background = theme.editorBackground,
                    surface = theme.surfaceColor,
                    onSurface = theme.onSurfaceColor
                )
            }

            MaterialTheme(colorScheme = m3Colors) {
                // Handle system back button
                BackHandler(enabled = uiState.activeNavTab != 0) {
                    viewModel.setNavTab(0)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = theme.surfaceColor,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            val items = listOf(
                                Triple(0, "Home", Icons.Default.Home),
                                Triple(1, "Editor", Icons.Default.Code),
                                Triple(2, "Files", Icons.Default.Folder),
                                Triple(3, "Terminal", Icons.Default.Terminal),
                                Triple(4, "Settings", Icons.Default.Settings),
                                Triple(5, "About", Icons.Default.Info)
                            )

                            items.forEach { (tabIndex, label, icon) ->
                                val isSelected = uiState.activeNavTab == tabIndex
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setNavTab(tabIndex) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) theme.primaryColor else theme.lineNumberColor
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            color = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = theme.primaryColor.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_$label")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(theme.editorBackground)
                    ) {
                        when (uiState.activeNavTab) {
                            0 -> HomeScreen(
                                theme = theme,
                                permissionStatus = uiState.permissionStatus,
                                recentFiles = uiState.openFiles,
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
                                onOpenWebDemo = { viewModel.openWebDemo() },
                                onOpenPythonDemo = {
                                    viewModel.openPythonDemo()
                                    viewModel.setNavTab(1)
                                },
                                onOpenStorageWizard = { viewModel.togglePermissionWizard(true) },
                                onNavigateToEditor = { viewModel.setNavTab(1) },
                                serverStatus = uiState.serverStatus,
                                onStartDevServer = { viewModel.startDevServer() },
                                onStopDevServer = { viewModel.stopDevServer() },
                                githubAccount = uiState.githubAccount,
                                onOpenGitPanel = { viewModel.toggleGitPanel(true) },
                                isLinqStudioCardVisible = uiState.isLinqStudioCardVisible
                            )

                            1 -> EditorScreen(
                                theme = theme,
                                openFiles = uiState.openFiles,
                                activeFileIndex = uiState.activeFileIndex,
                                onSelectTab = { viewModel.selectTab(it) },
                                onCloseTab = { viewModel.closeTab(it) },
                                onNewTab = { viewModel.newTab() },
                                onContentChange = { viewModel.updateActiveFileContent(it) },
                                onSaveCurrent = { viewModel.saveActiveFile() },
                                fontSizeSp = uiState.fontSizeSp,
                                isWordWrapEnabled = uiState.isWordWrapEnabled,
                                currentDirectory = uiState.currentDirectory,
                                directoryFiles = uiState.directoryFiles,
                                onOpenFile = { viewModel.openFile(it) },
                                onNavigateDir = { viewModel.loadDirectory(it) },
                                onNavigateUp = { viewModel.navigateUp() },
                                onNewFileInExplorer = { viewModel.createNewFileInExplorer(it) },
                                onNewFolderInExplorer = { viewModel.createNewFolderInExplorer(it) },
                                onDeleteInExplorer = { viewModel.deleteFileInExplorer(it) },
                                onRenameInExplorer = { old, newName -> viewModel.renameFileInExplorer(old, newName) },
                                onDuplicateInExplorer = { viewModel.duplicateFileInExplorer(it) },
                                onMoveInExplorer = { old, newDir -> viewModel.moveFileInExplorer(old, newDir) },
                                onShareInExplorer = { item -> viewModel.shareFile(this@MainActivity, item) },
                                onCloneRepo = { url, folder, branch, token -> viewModel.gitClone(url, folder, branch, token) },
                                onExportZip = { viewModel.exportZip(it) },
                                onRefreshFiles = { viewModel.loadDirectory(uiState.currentDirectory) },
                                onRunDemo = { viewModel.runActiveFile(it) },
                                onOpenWebPreview = { viewModel.openWebPreviewForActiveFile(it) },
                                isTerminalOpen = uiState.isTerminalOpen,
                                terminalLines = uiState.terminalLines,
                                isTerminalRunning = uiState.isTerminalRunning,
                                onToggleTerminal = { viewModel.toggleTerminal() },
                                onSendTerminalCommand = { viewModel.sendTerminalCommand(it) },
                                onClearTerminal = { viewModel.clearTerminal() },
                                onRerunTerminal = { viewModel.rerunActiveFile() },
                                onOpenGitPanel = { viewModel.toggleGitPanel(true) },
                                isGitLinked = uiState.githubAccount.isLinked,
                                githubAccount = uiState.githubAccount,
                                isBirdsEyeViewEnabled = uiState.isBirdsEyeViewEnabled,
                                onSelectTheme = { viewModel.selectTheme(it) },
                                onScreenOrientationChange = { viewModel.setScreenOrientationMode(it) },
                                onNavigatePath = { path ->
                                    val target = File(path)
                                    if (target.isDirectory) {
                                        viewModel.loadDirectory(target)
                                        viewModel.setNavTab(2)
                                    }
                                }
                            )

                            2 -> FilesScreen(
                                theme = theme,
                                workspaceDir = viewModel.fileRepository.internalWorkspaceDir,
                                currentDirectory = uiState.currentDirectory,
                                currentPathOrUri = uiState.currentPathOrUri,
                                storageRoots = uiState.storageRoots,
                                files = uiState.directoryFiles,
                                onOpenFile = {
                                    viewModel.openFile(it)
                                    viewModel.setNavTab(1)
                                },
                                onNavigateDir = { viewModel.loadDirectory(it) },
                                onNavigatePathOrUri = { viewModel.loadDirectory(it) },
                                onNavigateUp = { viewModel.navigateUp() },
                                onNewFile = { viewModel.createNewFileInExplorer(it) },
                                onNewFolder = { viewModel.createNewFolderInExplorer(it) },
                                onDelete = { viewModel.deleteFileInExplorer(it) },
                                onRename = { old, newName -> viewModel.renameFileInExplorer(old, newName) },
                                onMove = { old, newDir -> viewModel.moveFileInExplorer(old, newDir) },
                                onShare = { item -> viewModel.shareFile(this@MainActivity, item) },
                                onClone = { url, folder, branch, token -> viewModel.gitClone(url, folder, branch, token) },
                                onExportZip = { viewModel.exportZip(it) },
                                onRefresh = { viewModel.loadDirectory(uiState.currentPathOrUri) }
                            )

                            3 -> TerminalScreen(
                                theme = theme,
                                sessions = uiState.terminalSessions,
                                activeSessionId = uiState.activeSessionId,
                                onSelectSession = { viewModel.selectTerminalSession(it) },
                                onNewSession = { viewModel.newTerminalSession() },
                                onCloseSession = { viewModel.closeTerminalSession(it) },
                                onSendCommand = { viewModel.sendTerminalCommand(it) },
                                onCtrlC = { viewModel.ctrlCTerminal() },
                                onClearTerminal = { viewModel.clearTerminal() },
                                installedPackages = uiState.installedPackages,
                                onUninstallPackage = { viewModel.uninstallPackage(it) },
                                onOpenFileInEditor = {
                                    viewModel.openFile(it)
                                    viewModel.setNavTab(1)
                                }
                            )

                            4 -> SettingsScreen(
                                currentTheme = theme,
                                onSelectTheme = { viewModel.selectTheme(it) },
                                fontSizeSp = uiState.fontSizeSp,
                                onFontSizeChange = { viewModel.setFontSize(it) },
                                isWordWrapEnabled = uiState.isWordWrapEnabled,
                                onToggleWordWrap = { viewModel.toggleWordWrap(it) },
                                isBirdsEyeViewEnabled = uiState.isBirdsEyeViewEnabled,
                                onToggleBirdsEyeView = { viewModel.toggleBirdsEyeView(it) },
                                isLinqStudioCardVisible = uiState.isLinqStudioCardVisible,
                                onToggleLinqStudioCard = { viewModel.toggleLinqStudioCard(it) },
                                screenOrientationMode = uiState.screenOrientationMode,
                                onScreenOrientationChange = { viewModel.setScreenOrientationMode(it) },
                                reduceTransparency = uiState.reduceTransparency,
                                onToggleReduceTransparency = { viewModel.toggleReduceTransparency(it) },
                                onOpenStorageWizard = { viewModel.togglePermissionWizard(true) },
                                onImportJsonTheme = { viewModel.importJsonTheme(it) }
                            )

                            5 -> AboutScreen(theme = theme)
                        }

                        // Storage Permission Wizard Dialog
                        if (uiState.showPermissionWizard) {
                            PermissionWizardDialog(
                                theme = theme,
                                status = uiState.permissionStatus,
                                onRequestSafFolder = {
                                    safFolderLauncher.launch(permissionManager.createSafFolderIntent())
                                },
                                onRequestAllFilesAccess = {
                                    allFilesSettingsLauncher.launch(permissionManager.createManageAllFilesIntent())
                                },
                                onDismiss = { viewModel.togglePermissionWizard(false) }
                            )
                        }

                        // Web Live Preview Dialog with Local Dev Server
                        if (uiState.previewUrl != null || uiState.previewHtml != null) {
                            WebPreviewDialog(
                                theme = theme,
                                serverUrl = uiState.previewUrl,
                                htmlContent = uiState.previewHtml,
                                serverLogs = uiState.serverLogs,
                                serverStatus = uiState.serverStatus,
                                onRestartServer = { viewModel.restartDevServer() },
                                onClose = { viewModel.closePreview() }
                            )
                        }

                        // Git & GitHub VCS Bottom Sheet
                        if (uiState.isGitPanelOpen) {
                            GitPanelSheet(
                                theme = theme,
                                isOpen = uiState.isGitPanelOpen,
                                onClose = { viewModel.toggleGitPanel(false) },
                                githubAccount = uiState.githubAccount,
                                gitStatus = uiState.gitStatus,
                                gitCurrentBranch = uiState.gitCurrentBranch,
                                gitRemoteUrl = uiState.gitRemoteUrl,
                                gitLogs = uiState.gitLogs,
                                isLoading = uiState.isGitLoading,
                                currentDirectory = uiState.currentDirectory,
                                onLinkGitHub = { username, token, email ->
                                    viewModel.linkGitHubAccount(username, token, email)
                                },
                                onUnlinkGitHub = { viewModel.unlinkGitHubAccount() },
                                onInitRepo = { viewModel.gitInitRepo() },
                                onSetRemote = { url -> viewModel.gitSetRemote(url) },
                                onPull = { viewModel.gitPull() },
                                onPush = { viewModel.gitPush() },
                                onCommit = { message -> viewModel.gitCommit(message) },
                                onClone = { repoUrl -> viewModel.gitClone(repoUrl) },
                                onRefreshStatus = { viewModel.refreshGitStatus() }
                            )
                        }
                    }
                }
            }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            viewModel.openFromUri(uri)
        }
    }

    private fun cleanupCorruptedWebViewCache() {
        try {
            val codeCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            if (codeCacheDir.exists()) {
                val files = codeCacheDir.listFiles()
                // If it contains empty mock directories without Chromium indexes, wipe it cleanly
                if (files.isNullOrEmpty() || files.any { it.isDirectory && it.name in listOf("js", "wasm") && it.list().isNullOrEmpty() }) {
                    codeCacheDir.deleteRecursively()
                }
            }
        } catch (_: Exception) {
        }
    }
}
