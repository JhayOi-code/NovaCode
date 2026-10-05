package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.TerminalLine
import com.example.ui.components.CodeEditorView
import com.example.ui.components.FileExplorerPane
import com.example.ui.components.TerminalSheet
import java.io.File

@Composable
fun EditorScreen(
    theme: EditorTheme,
    openFiles: List<CodeFile>,
    activeFileIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onNewTab: () -> Unit,
    onContentChange: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    fontSizeSp: Int,
    isWordWrapEnabled: Boolean,
    currentDirectory: File,
    directoryFiles: List<FileItem>,
    onOpenFile: (FileItem) -> Unit,
    onNavigateDir: (File) -> Unit,
    onNavigateUp: () -> Unit,
    onNewFileInExplorer: (String) -> Unit,
    onNewFolderInExplorer: (String) -> Unit,
    onDeleteInExplorer: (String) -> Unit,
    onRenameInExplorer: (String, String) -> Unit,
    onExportZip: (File) -> Unit,
    onRefreshFiles: () -> Unit,
    onRunDemo: (CodeFile) -> Unit,
    onOpenWebPreview: (CodeFile) -> Unit = {},
    isTerminalOpen: Boolean = false,
    terminalLines: List<TerminalLine> = emptyList(),
    isTerminalRunning: Boolean = false,
    onToggleTerminal: () -> Unit = {},
    onSendTerminalCommand: (String) -> Unit = {},
    onClearTerminal: () -> Unit = {},
    onRerunTerminal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showExplorerDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .testTag("editor_screen")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Optional Side Drawer for File Explorer
            AnimatedVisibility(visible = showExplorerDrawer) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                    color = theme.surfaceColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                ) {
                    FileExplorerPane(
                        theme = theme,
                        currentDirectory = currentDirectory,
                        files = directoryFiles,
                        onOpenFile = { fileItem ->
                            onOpenFile(fileItem)
                            showExplorerDrawer = false
                        },
                        onNavigateDir = onNavigateDir,
                        onNavigateUp = onNavigateUp,
                        onNewFile = onNewFileInExplorer,
                        onNewFolder = onNewFolderInExplorer,
                        onDelete = onDeleteInExplorer,
                        onRename = onRenameInExplorer,
                        onExportZip = onExportZip,
                        onRefresh = onRefreshFiles
                    )
                }
            }

            // Main Editor & Terminal Component
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // Top control bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.surfaceColor,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showExplorerDrawer = !showExplorerDrawer },
                            modifier = Modifier.testTag("btn_toggle_explorer_drawer")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Toggle Files",
                                tint = if (showExplorerDrawer) theme.primaryColor else theme.onSurfaceColor
                            )
                        }

                        val active = openFiles.getOrNull(activeFileIndex)
                        Text(
                            text = active?.name ?: "Editor",
                            color = theme.onSurfaceColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )

                        if (active != null) {
                            val isWebFile = active.name.endsWith(".html", ignoreCase = true) ||
                                    active.name.endsWith(".htm", ignoreCase = true) ||
                                    active.name.endsWith(".js", ignoreCase = true) ||
                                    active.name.endsWith(".css", ignoreCase = true)

                            if (isWebFile) {
                                IconButton(
                                    onClick = { onOpenWebPreview(active) },
                                    modifier = Modifier.testTag("btn_web_preview_active_file")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Web Preview with Dev Server",
                                        tint = theme.primaryColor
                                    )
                                }
                            }

                            // Run code in terminal
                            IconButton(
                                onClick = { onRunDemo(active) },
                                modifier = Modifier.testTag("btn_run_active_file")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run in Terminal",
                                    tint = Color(0xFF22C55E)
                                )
                            }
                        }

                        // Terminal Toggle Button
                        IconButton(
                            onClick = onToggleTerminal,
                            modifier = Modifier.testTag("btn_toggle_terminal_top")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Toggle Terminal",
                                tint = if (isTerminalOpen) theme.primaryColor else theme.onSurfaceColor
                            )
                        }

                        if (active != null) {
                            IconButton(
                                onClick = onSaveCurrent,
                                modifier = Modifier.testTag("btn_save_top")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save file",
                                    tint = theme.primaryColor
                                )
                            }
                        }
                    }
                }

                // Editor View (fills remaining space)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CodeEditorView(
                        theme = theme,
                        openFiles = openFiles,
                        activeFileIndex = activeFileIndex,
                        onSelectTab = onSelectTab,
                        onCloseTab = onCloseTab,
                        onNewTab = onNewTab,
                        onContentChange = onContentChange,
                        onSaveCurrent = onSaveCurrent,
                        fontSizeSp = fontSizeSp,
                        isWordWrapEnabled = isWordWrapEnabled
                    )
                }

                // Built-in Terminal Drawer at the bottom
                AnimatedVisibility(visible = isTerminalOpen) {
                    TerminalSheet(
                        theme = theme,
                        lines = terminalLines,
                        isRunning = isTerminalRunning,
                        onSendCommand = onSendTerminalCommand,
                        onClear = onClearTerminal,
                        onRerun = onRerunTerminal,
                        onClose = onToggleTerminal,
                        modifier = Modifier.height(240.dp)
                    )
                }
            }
        }
    }
}
