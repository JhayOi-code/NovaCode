package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val theme = uiState.currentTheme

            LinqStudioTheme {
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
                                Triple(4, "Plugins", Icons.Default.Extension),
                                Triple(5, "Settings", Icons.Default.Settings),
                                Triple(6, "About", Icons.Default.Info)
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
                                recentFiles = uiState.openFiles,
                                serverStatus = uiState.serverStatus,
                                userAccount = uiState.userAccount,
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
                                onStartDevServer = { viewModel.startDevServer() },
                                onStopDevServer = { viewModel.stopDevServer() },
                                onUpdateProfile = { u, t, a, p -> viewModel.updateUserProfile(u, t, a, p) }
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

                        // Web Live Preview Dialog
                        if (uiState.previewUrl != null || uiState.previewHtml != null) {
                            WebPreviewDialog(
                                theme = theme,
                                serverUrl = uiState.previewUrl,
                                htmlContent = uiState.previewHtml,
                                serverStatus = uiState.serverStatus,
                                onRestartServer = { viewModel.restartDevServer() },
                                onClose = { viewModel.closePreview() }
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
