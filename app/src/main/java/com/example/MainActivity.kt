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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.EditorTheme
import com.example.storage.StoragePermissionHelper
import com.example.permissions.rememberPermissionManager
import com.example.ui.components.PermissionWizardDialog
import com.example.ui.components.WebPreviewDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if opened via "Open with" / "Share to" Intent
        intent?.data?.let { uri ->
            viewModel.openFromUri(uri)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val theme = uiState.currentTheme
            val permissionManager = rememberPermissionManager()

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
                                Triple(3, "Settings", Icons.Default.Settings),
                                Triple(4, "About", Icons.Default.Info)
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
                                            color = if (isSelected) theme.primaryColor else theme.lineNumberColor
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
                                onStopDevServer = { viewModel.stopDevServer() }
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
                                onRerunTerminal = { viewModel.rerunActiveFile() }
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
                                onExportZip = { viewModel.exportZip(it) },
                                onRefresh = { viewModel.loadDirectory(uiState.currentPathOrUri) }
                            )

                            3 -> SettingsScreen(
                                currentTheme = theme,
                                onSelectTheme = { viewModel.selectTheme(it) },
                                fontSizeSp = uiState.fontSizeSp,
                                onFontSizeChange = { viewModel.setFontSize(it) },
                                isWordWrapEnabled = uiState.isWordWrapEnabled,
                                onToggleWordWrap = { viewModel.toggleWordWrap(it) },
                                reduceTransparency = uiState.reduceTransparency,
                                onToggleReduceTransparency = { viewModel.toggleReduceTransparency(it) },
                                onOpenStorageWizard = { viewModel.togglePermissionWizard(true) },
                                onImportJsonTheme = { viewModel.importJsonTheme(it) }
                            )

                            4 -> AboutScreen(theme = theme)
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
}
