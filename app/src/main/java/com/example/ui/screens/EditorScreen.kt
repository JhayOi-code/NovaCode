package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autocomplete.AutocompleteSuggestion
import com.example.linter.LintIssue
import com.example.linter.LintSeverity
import com.example.model.CapturedLog
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.GitStatus
import com.example.runner.CodeExecutionEngine
import com.example.runner.ExecutionResult
import com.example.ui.components.AutocompleteBar
import com.example.ui.components.CodeMinimap
import com.example.ui.components.CodeOutputConsole
import com.example.ui.components.ExpandableTerminalDrawer
import com.example.ui.components.FileExplorerSidebar
import com.example.ui.components.SearchReplaceOverlay
import com.example.ui.components.TemplatesSidebar
import com.example.ui.editor.PrismSyntaxVisualTransformation
import java.io.File

@Composable
fun EditorScreen(
    theme: EditorTheme,
    openFiles: List<CodeFile>,
    activeFileIndex: Int,
    directoryFiles: List<FileItem> = emptyList(),
    isExplorerSidebarOpen: Boolean = false,
    isTemplatesPanelOpen: Boolean = false,
    isSearchReplaceOpen: Boolean = false,
    lintIssues: List<LintIssue> = emptyList(),
    capturedLogs: List<CapturedLog> = emptyList(),
    showTerminalDrawer: Boolean = false,
    isTerminalDrawerExpanded: Boolean = false,
    executionResult: ExecutionResult? = null,
    showOutputConsole: Boolean = false,
    isFullScreen: Boolean = false,
    isZenMode: Boolean = false,
    isMinimapVisible: Boolean = true,
    gitStatus: GitStatus? = null,
    autocompleteSuggestions: List<AutocompleteSuggestion> = emptyList(),
    prettierFormatNotice: String? = null,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onCloseAllTabs: () -> Unit = {},
    onCloseOtherTabs: (Int) -> Unit = {},
    onSaveAllTabs: () -> Unit = {},
    onNewTab: () -> Unit,
    onContentChange: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    onRunCode: () -> Unit,
    onFormatWithPrettier: () -> Unit = {},
    onToggleZenMode: () -> Unit = {},
    onToggleMinimap: () -> Unit = {},
    onSelectAutocompleteSuggestion: (AutocompleteSuggestion) -> Unit = {},
    onDismissAutocomplete: () -> Unit = {},
    onStageGitFile: (String) -> Unit = {},
    onStageAllGitFiles: () -> Unit = {},
    onUnstageGitFile: (String) -> Unit = {},
    onUnstageAllGitFiles: () -> Unit = {},
    onCommitGit: (String) -> Unit = {},
    onPushGit: () -> Unit = {},
    onCreateGitBranch: (String) -> Unit = {},
    onSwitchGitBranch: (String) -> Unit = {},
    onToggleExplorerSidebar: () -> Unit = {},
    onOpenFileFromSidebar: (File) -> Unit = {},
    onNewFileInSidebar: (String) -> Unit = {},
    onToggleTemplatesPanel: () -> Unit = {},
    onInsertTemplateSnippet: (String) -> Unit = {},
    onCreateFileFromTemplate: (fileName: String, content: String) -> Unit = { _, _ -> },
    onToggleSearchReplace: () -> Unit = {},
    onToggleTerminalDrawer: () -> Unit = {},
    onToggleTerminalDrawerExpand: () -> Unit = {},
    onClearSandboxLogs: () -> Unit = {},
    onCloseTerminalDrawer: () -> Unit = {},
    onSendTerminalDrawerCommand: (String) -> Unit = {},
    onCloseOutputConsole: () -> Unit = {},
    onToggleFullScreen: () -> Unit = {},
    onOpenWebPreview: () -> Unit,
    onOpenPlugins: () -> Unit
) {
    val activeFile = openFiles.getOrNull(activeFileIndex)
    val detectedLanguage = activeFile?.let { CodeExecutionEngine.detectLanguage(it.name) } ?: "Plain Text"

    var showTabMenu by remember { mutableStateOf(false) }
    var showProblemsDrawer by remember { mutableStateOf(false) }

    // In Zen Mode, back press exits Zen Mode
    BackHandler(enabled = isZenMode) {
        onToggleZenMode()
    }

    // Map line number to lint issue
    val lineToLintIssue = remember(lintIssues) {
        lintIssues.groupBy { it.line }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
    ) {
        // TOP FILE TABS BAR WITH FULL TAB MANAGEMENT (Hidden in Zen Mode)
        if (!isZenMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Explorer Sidebar Quick Toggle
                IconButton(
                    onClick = onToggleExplorerSidebar,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_tab_toggle_sidebar")
                ) {
                    Icon(
                        imageVector = if (isExplorerSidebarOpen) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = "Project Files",
                        tint = if (isExplorerSidebarOpen) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Templates Panel Quick Toggle
                IconButton(
                    onClick = onToggleTemplatesPanel,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_tab_toggle_templates")
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = "Code Templates",
                        tint = if (isTemplatesPanelOpen) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                VerticalDivider(
                    color = theme.borderColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(20.dp)
                        .padding(horizontal = 4.dp)
                )

                // Dynamic File Tabs
                openFiles.forEachIndexed { index, file ->
                    val isSelected = index == activeFileIndex
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        color = if (isSelected) theme.editorBackground else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.4f)) else null,
                        modifier = Modifier
                            .clickable { onSelectTab(index) }
                            .testTag("editor_tab_$index")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            if (file.isModified) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(Color(0xFFF59E0B), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }

                            Text(
                                text = file.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) theme.onSurfaceColor else theme.lineNumberColor,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Close single tab button
                            IconButton(
                                onClick = { onCloseTab(index) },
                                modifier = Modifier
                                    .size(16.dp)
                                    .testTag("btn_close_tab_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Tab",
                                    tint = theme.lineNumberColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }

                // New Tab '+' Button
                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("btn_new_tab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New File",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Tab Management Dropdown Menu Button
                Box {
                    IconButton(
                        onClick = { showTabMenu = true },
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("btn_tab_overflow_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Tab Options",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showTabMenu,
                        onDismissRequest = { showTabMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save Current File", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                onSaveCurrent()
                                showTabMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Save All Files", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.SaveAs, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                onSaveAllTabs()
                                showTabMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Close Other Tabs", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.TabUnselected, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                onCloseOtherTabs(activeFileIndex)
                                showTabMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Close All Tabs", fontSize = 12.sp, color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                onCloseAllTabs()
                                showTabMenu = false
                            }
                        )
                    }
                }
            }

            // QUICK EDITOR ACTION BAR (Hidden in Zen Mode)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor.copy(alpha = 0.65f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sidebar Toggle Button
                IconButton(
                    onClick = onToggleExplorerSidebar,
                    modifier = Modifier.size(32.dp).testTag("btn_toggle_explorer_sidebar")
                ) {
                    Icon(
                        imageVector = if (isExplorerSidebarOpen) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = "Project Files Sidebar",
                        tint = if (isExplorerSidebarOpen) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Templates Panel Toggle Button
                IconButton(
                    onClick = onToggleTemplatesPanel,
                    modifier = Modifier.size(32.dp).testTag("btn_editor_templates")
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = "Boilerplate Templates",
                        tint = if (isTemplatesPanelOpen) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Search and Replace Overlay Toggle Button
                IconButton(
                    onClick = onToggleSearchReplace,
                    modifier = Modifier.size(32.dp).testTag("btn_editor_search_replace")
                ) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Search and Replace",
                        tint = if (isSearchReplaceOpen) Color(0xFF38BDF8) else theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save Button
                IconButton(
                    onClick = onSaveCurrent,
                    modifier = Modifier.size(32.dp).testTag("btn_save_file")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                }

                // PRETTIER STYLE CODE FORMATTING BUTTON
                Button(
                    onClick = onFormatWithPrettier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_editor_prettier")
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prettier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // ZEN MODE TOGGLE BUTTON (Distraction-Free)
                IconButton(
                    onClick = onToggleZenMode,
                    modifier = Modifier.size(32.dp).testTag("btn_editor_zen_mode")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Zen Mode (Distraction-Free)",
                        tint = if (isZenMode) Color(0xFF10B981) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // CODE MINIMAP TOGGLE BUTTON
                IconButton(
                    onClick = onToggleMinimap,
                    modifier = Modifier.size(32.dp).testTag("btn_editor_minimap")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Code Minimap",
                        tint = if (isMinimapVisible) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Expandable Terminal Drawer Toggle Button
                IconButton(
                    onClick = onToggleTerminalDrawer,
                    modifier = Modifier.size(32.dp).testTag("btn_toggle_terminal_drawer")
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Console & Sandbox Terminal",
                        tint = if (showTerminalDrawer) Color(0xFF10B981) else theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Extensions / Plugins Button
                IconButton(
                    onClick = onOpenPlugins,
                    modifier = Modifier.size(32.dp).testTag("btn_editor_plugins")
                ) {
                    Icon(Icons.Default.Extension, contentDescription = "VS Code Plugins", tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                }

                // Full Screen / Fit Screen Button
                IconButton(
                    onClick = onToggleFullScreen,
                    modifier = Modifier.size(32.dp).testTag("btn_editor_fullscreen")
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                        tint = if (isFullScreen) Color(0xFF10B981) else theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Active Language Badge (with Prism indicator)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.primaryColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = detectedLanguage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryColor,
                            maxLines = 1
                        )
                        Text(
                            text = "• Prism",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // Linter Status Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (lintIssues.isNotEmpty()) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (lintIssues.isNotEmpty()) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .clickable { showProblemsDrawer = !showProblemsDrawer }
                        .testTag("btn_linter_status")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (lintIssues.isNotEmpty()) Icons.Default.ErrorOutline else Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = if (lintIssues.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (lintIssues.isNotEmpty()) "${lintIssues.size} Issues" else "0 Issues",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (lintIssues.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }

                // Web Preview quick button if HTML/Web
                if (detectedLanguage == "HTML / Web") {
                    IconButton(
                        onClick = onOpenWebPreview,
                        modifier = Modifier.size(30.dp).testTag("btn_editor_web_preview_icon")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Web Preview", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }
                }

                // Primary Run Code button for ANY language (Python, JS, Bash, Kotlin, C, etc.)
                Button(
                    onClick = onRunCode,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_editor_run_code")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (detectedLanguage == "HTML / Web") "Preview" else "Run",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(color = theme.borderColor)
        }

        // MAIN WORKSPACE (SIDEBARS + CODE EDITOR BODY + OVERLAYS + TERMINAL DRAWER)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // FILE EXPLORER SIDEBAR WITH GIT SIDE PANEL (Hidden in Zen Mode)
                if (!isZenMode) {
                    AnimatedVisibility(
                        visible = isExplorerSidebarOpen,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        FileExplorerSidebar(
                            theme = theme,
                            directoryFiles = directoryFiles,
                            openFiles = openFiles,
                            activeFileIndex = activeFileIndex,
                            gitStatus = gitStatus,
                            onStageFile = onStageGitFile,
                            onStageAll = onStageAllGitFiles,
                            onUnstageFile = onUnstageGitFile,
                            onUnstageAll = onUnstageAllGitFiles,
                            onCommit = onCommitGit,
                            onPush = onPushGit,
                            onCreateBranch = onCreateGitBranch,
                            onSwitchBranch = onSwitchGitBranch,
                            onOpenFile = onOpenFileFromSidebar,
                            onNewFile = onNewFileInSidebar,
                            onSaveActive = onSaveCurrent,
                            onCloseSidebar = onToggleExplorerSidebar
                        )
                    }

                    // TEMPLATES SIDEBAR PANEL
                    AnimatedVisibility(
                        visible = isTemplatesPanelOpen,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        TemplatesSidebar(
                            theme = theme,
                            onInsertSnippet = onInsertTemplateSnippet,
                            onCreateFileWithSnippet = onCreateFileFromTemplate,
                            onClose = onToggleTemplatesPanel
                        )
                    }
                }

                // CODE EDITOR BODY (With Line Numbers, Linter Gutter Squiggle Dots, Prism Highlighting, and Minimap)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (activeFile != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(theme.editorBackground)
                        ) {
                            // Dynamic line numbers gutter with Linter status dots
                            val lineCount = remember(activeFile.content) {
                                maxOf(1, activeFile.content.count { it == '\n' } + 1)
                            }

                            Column(
                                modifier = Modifier
                                    .widthIn(min = 44.dp, max = 56.dp)
                                    .fillMaxHeight()
                                    .background(theme.surfaceColor.copy(alpha = 0.35f))
                                    .padding(top = 8.dp, end = 4.dp, start = 4.dp)
                            ) {
                                for (lineNum in 1..lineCount) {
                                    val issuesForLine = lineToLintIssue[lineNum]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (issuesForLine != null) {
                                            val hasError = issuesForLine.any { it.severity == LintSeverity.ERROR }
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .background(
                                                        if (hasError) Color(0xFFEF4444) else Color(0xFFF59E0B),
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(
                                            text = "$lineNum",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = if (issuesForLine != null) Color(0xFFEF4444) else theme.lineNumberColor.copy(alpha = 0.6f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }

                            VerticalDivider(color = theme.borderColor.copy(alpha = 0.5f), thickness = 1.dp)

                            // Editor Code Body with Prism.js Real-Time Syntax Highlighting
                            OutlinedTextField(
                                value = activeFile.content,
                                onValueChange = onContentChange,
                                visualTransformation = remember(detectedLanguage) {
                                    PrismSyntaxVisualTransformation(detectedLanguage)
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = theme.onSurfaceColor,
                                    lineHeight = 18.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = theme.editorBackground,
                                    unfocusedContainerColor = theme.editorBackground
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("code_editor_text_field")
                            )

                            // CODE MINIMAP (Overview scrollbar on right side of editor pane)
                            if (isMinimapVisible) {
                                CodeMinimap(
                                    theme = theme,
                                    codeContent = activeFile.content,
                                    lintIssues = lintIssues,
                                    scrollFraction = 0f,
                                    onScrollToLineFraction = { /* Minimap click handled */ },
                                    modifier = Modifier.fillMaxHeight()
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("No file open", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = theme.onSurfaceColor)
                                Text("Tap '+' to create a new file or select from Templates", fontSize = 12.sp, color = theme.lineNumberColor)
                                Button(onClick = onToggleTemplatesPanel, shape = RoundedCornerShape(8.dp)) {
                                    Text("Open Templates")
                                }
                            }
                        }
                    }

                    // CONTEXT-AWARE AUTOCOMPLETE & AI COPILOT SUGGESTIONS BAR
                    if (autocompleteSuggestions.isNotEmpty() && activeFile != null) {
                        AutocompleteBar(
                            theme = theme,
                            suggestions = autocompleteSuggestions,
                            onSelectSuggestion = onSelectAutocompleteSuggestion,
                            onDismiss = onDismissAutocomplete,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    bottom = if (!isZenMode && (showTerminalDrawer || (showOutputConsole && executionResult != null))) 220.dp else 12.dp,
                                    start = 48.dp,
                                    end = if (isMinimapVisible) 48.dp else 12.dp
                                )
                        )
                    }

                    // PRETTIER FORMAT NOTIFICATION BANNER
                    if (!prettierFormatNotice.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFA855F7).copy(alpha = 0.95f),
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                                .testTag("prettier_notice_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(prettierFormatNotice, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SEARCH AND REPLACE OVERLAY MODAL
                    if (isSearchReplaceOpen && activeFile != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                        ) {
                            SearchReplaceOverlay(
                                theme = theme,
                                activeFileContent = activeFile.content,
                                onContentChange = onContentChange,
                                onClose = onToggleSearchReplace
                            )
                        }
                    }

                    // LINTER PROBLEMS PANEL (EXPANDABLE)
                    if (showProblemsDrawer && lintIssues.isNotEmpty() && !isZenMode) {
                        Card(
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .testTag("problems_drawer")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Text("Syntax Issues (${lintIssues.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.onSurfaceColor)
                                    }
                                    IconButton(onClick = { showProblemsDrawer = false }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(14.dp))
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = theme.borderColor.copy(alpha = 0.4f))
                                LazyColumn {
                                    items(lintIssues) { issue ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Line ${issue.line}:",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                            Text(
                                                text = issue.message,
                                                fontSize = 11.sp,
                                                color = theme.onSurfaceColor,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FLOATING ACTION BUTTON (FAB) FOR 'RUN' (Hidden in Zen Mode)
                    if (!isZenMode) {
                        ExtendedFloatingActionButton(
                            onClick = onRunCode,
                            icon = {
                                Icon(
                                    imageVector = if (detectedLanguage == "HTML / Web") Icons.Default.Visibility else Icons.Default.PlayArrow,
                                    contentDescription = if (detectedLanguage == "HTML / Web") "Preview" else "Run"
                                )
                            },
                            text = {
                                Text(
                                    text = if (detectedLanguage == "HTML / Web") "Preview" else "Run",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    bottom = if (showTerminalDrawer || (showOutputConsole && executionResult != null)) 220.dp else 20.dp,
                                    end = if (isMinimapVisible) 56.dp else 20.dp
                                )
                                .testTag("btn_editor_run_fab")
                        )
                    }

                    // FLOATING 'EXIT ZEN MODE' PILL (Shown in Zen Mode)
                    if (isZenMode) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = theme.surfaceColor.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clickable { onToggleZenMode() }
                                .testTag("btn_exit_zen_mode")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CloseFullscreen,
                                    contentDescription = "Exit Zen Mode",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Exit Zen Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor
                                )
                            }
                        }
                    }
                }
            }

            // EXPANDABLE TERMINAL DRAWER AT BOTTOM OF APP (Hidden in Zen Mode)
            if (showTerminalDrawer && !isZenMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    ExpandableTerminalDrawer(
                        theme = theme,
                        logs = capturedLogs,
                        isExpanded = isTerminalDrawerExpanded,
                        onToggleExpand = onToggleTerminalDrawerExpand,
                        onClear = onClearSandboxLogs,
                        onClose = onCloseTerminalDrawer,
                        onSendCommand = onSendTerminalDrawerCommand
                    )
                }
            }

            // Interactive Output Console Drawer inside Editor (Hidden in Zen Mode)
            if (showOutputConsole && executionResult != null && !isZenMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    CodeOutputConsole(
                        theme = theme,
                        result = executionResult,
                        onRerun = onRunCode,
                        onClose = onCloseOutputConsole
                    )
                }
            }

            // Quick Exit Full Screen FAB when in full-screen mode (and not in Zen Mode)
            if (isFullScreen && !isZenMode) {
                SmallFloatingActionButton(
                    onClick = onToggleFullScreen,
                    containerColor = theme.surfaceColor.copy(alpha = 0.92f),
                    contentColor = Color(0xFF10B981),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .testTag("btn_floating_exit_fullscreen")
                ) {
                    Icon(
                        Icons.Default.FullscreenExit,
                        contentDescription = "Exit Full Screen",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
