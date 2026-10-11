package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.git.model.GitHubAccount
import com.example.model.BreadcrumbHelper
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.TerminalLine
import com.example.ui.components.CodeEditorView
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.IdeCommand
import com.example.ui.components.LanguageFileIcon
import com.example.ui.components.SidebarFileExplorerDrawer
import com.example.ui.components.TerminalSheet
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
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
    onDuplicateInExplorer: (String) -> Unit = {},
    onMoveInExplorer: (String, String) -> Unit = { _, _ -> },
    onShareInExplorer: (FileItem) -> Unit = {},
    onCloneRepo: (String, String, String, String) -> Unit = { _, _, _, _ -> },
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
    onOpenGitPanel: () -> Unit = {},
    isGitLinked: Boolean = false,
    githubAccount: GitHubAccount? = null,
    isBirdsEyeViewEnabled: Boolean = true,
    onNavigatePath: ((String) -> Unit)? = null,
    onSelectTheme: ((EditorTheme) -> Unit)? = null,
    onScreenOrientationChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showExplorerDrawer by remember { mutableStateOf(false) }
    var showCommandPalette by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val drawerWidth = (configuration.screenWidthDp * 0.75f).coerceIn(280f, 420f).dp
    val active = openFiles.getOrNull(activeFileIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .testTag("editor_screen")
    ) {
        // Main Editor & Terminal Component (Full width)
        Column(modifier = Modifier.fillMaxSize()) {
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
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Toggle Files Drawer",
                            tint = if (showExplorerDrawer) theme.primaryColor else theme.onSurfaceColor
                        )
                    }

                    // Dynamic Breadcrumbs in Editor Header
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp)
                            .testTag("editor_header_breadcrumbs"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (active == null) {
                            Text(
                                text = "Editor",
                                color = theme.onSurfaceColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            val breadcrumbSegments = remember(active.path, active.name, currentDirectory) {
                                BreadcrumbHelper.getSegments(
                                    filePath = active.path,
                                    fileName = active.name,
                                    workspaceDir = currentDirectory
                                )
                            }
                            breadcrumbSegments.forEachIndexed { idx, segment ->
                                val isLast = idx == breadcrumbSegments.lastIndex
                                if (idx > 0) {
                                    Text(
                                        text = "›",
                                        color = theme.lineNumberColor.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                                Surface(
                                    color = if (isLast) theme.primaryColor.copy(alpha = 0.12f) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(enabled = segment.isDirectory) {
                                            onNavigateDir(File(segment.path))
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        if (isLast) {
                                            LanguageFileIcon(filename = active.name, isDirectory = false, size = 14.dp)
                                        }
                                        Text(
                                            text = segment.label,
                                            color = if (isLast) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

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

                    // Global Command Palette Button
                    IconButton(
                        onClick = { showCommandPalette = true },
                        modifier = Modifier.testTag("btn_command_palette_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Command Palette (Ctrl+Shift+P)",
                            tint = theme.primaryColor
                        )
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

                    // Git & GitHub VCS Button
                    IconButton(
                        onClick = onOpenGitPanel,
                        modifier = Modifier.testTag("btn_open_git_panel")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallSplit,
                            contentDescription = "Git VCS & GitHub",
                            tint = if (isGitLinked) theme.primaryColor else theme.onSurfaceColor
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
                    isWordWrapEnabled = isWordWrapEnabled,
                    isBirdsEyeViewEnabled = isBirdsEyeViewEnabled,
                    onNavigatePath = onNavigatePath
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

        // ==========================================
        // SLIDE-OVER DRAWER OVERLAY & BACKDROP
        // ==========================================
        // Dimmed backdrop (tap outside to close)
        AnimatedVisibility(
            visible = showExplorerDrawer,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showExplorerDrawer = false
                    }
                    .testTag("sidebar_drawer_backdrop")
            )
        }

        // Drawer sliding from left edge (~75% width)
        AnimatedVisibility(
            visible = showExplorerDrawer,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
            ) {
                SidebarFileExplorerDrawer(
                    theme = theme,
                    currentDirectory = currentDirectory,
                    activeFile = active,
                    onOpenFile = { fileItem ->
                        onOpenFile(fileItem)
                        showExplorerDrawer = false
                    },
                    onCloseDrawer = { showExplorerDrawer = false },
                    onNewFile = onNewFileInExplorer,
                    onNewFolder = onNewFolderInExplorer,
                    onRename = onRenameInExplorer,
                    onDelete = onDeleteInExplorer,
                    onDuplicate = onDuplicateInExplorer,
                    onMove = onMoveInExplorer,
                    onShare = onShareInExplorer,
                    onRefresh = onRefreshFiles,
                    onRunActive = {
                        if (active != null) onRunDemo(active)
                    },
                    onOpenGitPanel = onOpenGitPanel,
                    githubAccount = githubAccount
                )
            }
        }

        // Global Command Palette Dialog
        if (showCommandPalette) {
            val ideCommands = remember(active, isTerminalOpen, isGitLinked) {
                listOf(
                    IdeCommand(
                        id = "file_new",
                        title = "File: New File",
                        description = "Create a new file in workspace",
                        category = "File",
                        shortcut = "Ctrl+N",
                        icon = Icons.Default.Add,
                        action = { onNewTab() }
                    ),
                    IdeCommand(
                        id = "file_save",
                        title = "File: Save Active File",
                        description = "Save changes to disk",
                        category = "File",
                        shortcut = "Ctrl+S",
                        icon = Icons.Default.Save,
                        action = { onSaveCurrent() }
                    ),
                    IdeCommand(
                        id = "view_explorer",
                        title = "View: Toggle File Explorer Drawer",
                        description = "Browse files, create folders, git clone",
                        category = "View",
                        shortcut = "Ctrl+B",
                        icon = Icons.Default.Folder,
                        action = { showExplorerDrawer = !showExplorerDrawer }
                    ),
                    IdeCommand(
                        id = "theme_linq",
                        title = "Theme: Linq Dark (Default)",
                        description = "Modern dark IDE theme",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.LinqDark) }
                    ),
                    IdeCommand(
                        id = "theme_onedark",
                        title = "Theme: One Dark Pro",
                        description = "Atom-style balanced dark theme",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.OneDark) }
                    ),
                    IdeCommand(
                        id = "theme_monokai",
                        title = "Theme: Monokai",
                        description = "High-contrast classic developer theme",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.Monokai) }
                    ),
                    IdeCommand(
                        id = "theme_dracula",
                        title = "Theme: Dracula",
                        description = "Vibrant dark theme for vampire devs",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.Dracula) }
                    ),
                    IdeCommand(
                        id = "theme_github",
                        title = "Theme: GitHub Dark",
                        description = "GitHub modern slate dark theme",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.GithubDark) }
                    ),
                    IdeCommand(
                        id = "theme_cyberpunk",
                        title = "Theme: Cyberpunk Neon",
                        description = "High-energy neon cyberpunk palette",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.Cyberpunk) }
                    ),
                    IdeCommand(
                        id = "theme_light",
                        title = "Theme: Light Theme",
                        description = "High-contrast clean light theme",
                        category = "Theme",
                        icon = Icons.Default.Palette,
                        action = { onSelectTheme?.invoke(EditorTheme.Light) }
                    ),
                    IdeCommand(
                        id = "orient_auto",
                        title = "Orientation: Auto-Rotate (Landscape on Flip)",
                        description = "Adapt between portrait and landscape on phone flip",
                        category = "Orientation",
                        shortcut = "Auto",
                        icon = Icons.Default.ScreenRotation,
                        action = { onScreenOrientationChange?.invoke("sensor") }
                    ),
                    IdeCommand(
                        id = "orient_landscape",
                        title = "Orientation: Lock Landscape Mode",
                        description = "Force landscape orientation",
                        category = "Orientation",
                        shortcut = "Land",
                        icon = Icons.Default.ScreenRotation,
                        action = { onScreenOrientationChange?.invoke("landscape") }
                    ),
                    IdeCommand(
                        id = "orient_portrait",
                        title = "Orientation: Lock Portrait Mode",
                        description = "Force portrait orientation",
                        category = "Orientation",
                        shortcut = "Port",
                        icon = Icons.Default.ScreenRotation,
                        action = { onScreenOrientationChange?.invoke("portrait") }
                    ),
                    IdeCommand(
                        id = "run_demo",
                        title = "Run: Execute Active File",
                        description = "Run current script in terminal",
                        category = "Run",
                        shortcut = "F5",
                        icon = Icons.Default.PlayArrow,
                        action = { if (active != null) onRunDemo(active) }
                    ),
                    IdeCommand(
                        id = "view_terminal",
                        title = "View: Toggle Built-in Terminal",
                        description = "Open or close the bash command line",
                        category = "View",
                        shortcut = "Ctrl+`",
                        icon = Icons.Default.Terminal,
                        action = onToggleTerminal
                    ),
                    IdeCommand(
                        id = "view_git",
                        title = "Git: VCS & GitHub Integration",
                        description = "Manage branches, commit, push, and remotes",
                        category = "Git",
                        icon = Icons.AutoMirrored.Filled.CallSplit,
                        action = onOpenGitPanel
                    )
                )
            }

            CommandPaletteDialog(
                theme = theme,
                commands = ideCommands,
                onDismiss = { showCommandPalette = false }
            )
        }
    }
}
