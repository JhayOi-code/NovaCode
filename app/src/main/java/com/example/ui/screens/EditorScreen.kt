package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.example.model.ProjectTemplateType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.ai.AiChatMessage
import com.example.ai.ProposedDiff
import com.example.autocomplete.AutocompleteSuggestion
import com.example.engine.CodeExecutionEngine
import com.example.engine.ExecutionResult
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.GitRepositoryStatus
import com.example.model.LintIssue
import com.example.model.LintSeverity
import com.example.ui.components.AiAssistantPanel
import com.example.ui.components.AutocompleteBar
import com.example.ui.components.CodeMinimap
import com.example.ui.components.DiffPreviewDialog
import com.example.ui.components.ExecutionOutputSheet
import com.example.ui.components.FileExplorerSidebar
import com.example.ui.components.SearchReplaceOverlay
import com.example.ui.editor.PrismSyntaxHighlighter
import com.example.ui.editor.SmartCodeInputHandler
import com.example.ui.editor.SpecialCharactersToolbar

@Composable
fun EditorScreen(
    theme: EditorTheme,
    openFiles: List<CodeFile>,
    activeFileIndex: Int,
    isExplorerSidebarOpen: Boolean,
    isSearchReplaceOpen: Boolean,
    lintIssues: List<LintIssue>,
    isFullScreen: Boolean,
    isZenMode: Boolean,
    isMinimapVisible: Boolean,
    gitStatus: GitRepositoryStatus,
    autocompleteSuggestions: List<AutocompleteSuggestion>,
    prettierFormatNotice: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    isAiPanelOpen: Boolean,
    aiMessages: List<AiChatMessage>,
    isAiLoading: Boolean,
    activeProposedDiff: ProposedDiff?,
    isDiffPreviewOpen: Boolean,
    prefilledAiPrompt: String,
    isSuggestionsEnabled: Boolean,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onCloseOtherTabs: (Int) -> Unit,
    onNewTab: () -> Unit,
    onContentChange: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    onRunCode: () -> Unit,
    onFormatWithPrettier: () -> Unit,
    onToggleZenMode: () -> Unit,
    onToggleMinimap: () -> Unit,
    onSelectAutocompleteSuggestion: (AutocompleteSuggestion) -> Unit,
    onDismissAutocomplete: () -> Unit,
    onStageGitFile: (String) -> Unit,
    onStageAllGitFiles: () -> Unit,
    onUnstageGitFile: (String) -> Unit,
    onUnstageAllGitFiles: () -> Unit,
    onCommitGit: (String) -> Unit,
    onPushGit: () -> Unit,
    onToggleExplorerSidebar: () -> Unit,
    trackedFolders: List<String> = emptyList(),
    onOpenFileFromSidebar: (CodeFile) -> Unit,
    onNewFileInSidebar: (String) -> Unit,
    onNewFolderInSidebar: (String) -> Unit = {},
    onNewFolderInSidebarWithTemplate: (String, ProjectTemplateType) -> Unit = { name, _ -> onNewFolderInSidebar(name) },
    onNewFileInSidebarWithFolder: (String?, String) -> Unit = { _, name -> onNewFileInSidebar(name) },
    onDeleteFolderInSidebar: (String) -> Unit = {},
    onToggleSearchReplace: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleAiPanel: () -> Unit,
    onCloseAiPanel: () -> Unit,
    onSendAiMessage: (String) -> Unit,
    onStopAiGenerating: () -> Unit,
    onClearAiChat: () -> Unit,
    onOpenAiWithPrompt: (String) -> Unit,
    onReviewDiff: (String, String) -> Unit,
    onApplyDiff: (String) -> Unit,
    onRejectDiff: () -> Unit,
    onToggleSuggestionsEnabled: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onOpenTerminal: () -> Unit = {}
) {
    val activeFile = openFiles.getOrNull(activeFileIndex)

    var textFieldValue by remember(activeFile?.id) {
        val initialContent = activeFile?.content ?: ""
        mutableStateOf(TextFieldValue(initialContent, TextRange(initialContent.length)))
    }

    // Keep text in sync when external change happens
    LaunchedEffect(activeFile?.content) {
        val currentContent = activeFile?.content ?: ""
        if (currentContent != textFieldValue.text) {
            val cursor = minOf(textFieldValue.selection.start, currentContent.length)
            textFieldValue = TextFieldValue(currentContent, TextRange(cursor))
        }
    }

    val currentLineIndex by remember(textFieldValue.text, textFieldValue.selection.start) {
        derivedStateOf {
            val cursor = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
            textFieldValue.text.take(cursor).count { it == '\n' }
        }
    }

    val errorCount = remember(lintIssues) { lintIssues.count { it.severity == LintSeverity.ERROR } }
    val warningCount = remember(lintIssues) { lintIssues.count { it.severity == LintSeverity.WARNING } }
    val infoCount = remember(lintIssues) { lintIssues.count { it.severity == LintSeverity.INFO } }

    var selectedLintIssue by remember { mutableStateOf<LintIssue?>(null) }
    var executionResult by remember { mutableStateOf<ExecutionResult?>(null) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var createFileNameInput by remember { mutableStateOf("") }
    var showSaveToast by remember { mutableStateOf<String?>(null) }
    var showShortcutsHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showSaveToast) {
        if (showSaveToast != null) {
            kotlinx.coroutines.delay(2200)
            showSaveToast = null
        }
    }

    // Physical, Bluetooth, and Virtual Keyboard Shortcut Handler (Ctrl+S, Ctrl+F, Ctrl+Enter, etc.)
    val handleEditorKeyEvent: (KeyEvent) -> Boolean = { event ->
        if (event.type == KeyEventType.KeyDown) {
            val isCtrlOrMeta = event.isCtrlPressed || event.isMetaPressed
            if (isCtrlOrMeta) {
                when (event.key) {
                    Key.S -> {
                        onSaveCurrent()
                        val fileName = activeFile?.name ?: "file"
                        showSaveToast = "💾 Saved $fileName (Ctrl+S)"
                        true
                    }
                    Key.F -> {
                        onToggleSearchReplace()
                        true
                    }
                    Key.Enter, Key.NumPadEnter -> {
                        val current = activeFile ?: openFiles.firstOrNull()
                        if (current != null) {
                            executionResult = CodeExecutionEngine.execute(current, openFiles)
                            onRunCode()
                        }
                        true
                    }
                    Key.Z -> {
                        if (event.isShiftPressed) {
                            if (canRedo) onRedo()
                        } else {
                            if (canUndo) onUndo()
                        }
                        true
                    }
                    Key.Y -> {
                        if (canRedo) onRedo()
                        true
                    }
                    Key.B -> {
                        onToggleExplorerSidebar()
                        true
                    }
                    Key.W -> {
                        if (openFiles.size > 1) {
                            onCloseTab(activeFileIndex)
                        }
                        true
                    }
                    Key.N -> {
                        onNewTab()
                        true
                    }
                    Key.Slash -> {
                        showShortcutsHelpDialog = true
                        true
                    }
                    else -> false
                }
            } else if (event.key == Key.F1) {
                showShortcutsHelpDialog = true
                true
            } else false
        } else false
    }

    val coroutineScope = rememberCoroutineScope()
    val editorScrollState = rememberScrollState()
    val lines = remember(textFieldValue.text) { textFieldValue.text.lines() }
    val totalLines = maxOf(1, lines.size)
    val digitsCount = remember(totalLines) { totalLines.toString().length }
    val gutterWidthDp = (digitsCount * 8 + 24).dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .onPreviewKeyEvent(handleEditorKeyEvent)
            .testTag("editor_screen")
    ) {
        // TOP TOOLBAR
        if (!isZenMode) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = theme.surfaceColor,
                tonalElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left action icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sidebar toggle
                        IconButton(
                            onClick = onToggleExplorerSidebar,
                            modifier = Modifier.size(34.dp).testTag("toolbar_sidebar_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Toggle Files",
                                tint = if (isExplorerSidebarOpen) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Create File Button
                        IconButton(
                            onClick = {
                                createFileNameInput = ""
                                showCreateFileDialog = true
                            },
                            modifier = Modifier.size(34.dp).testTag("toolbar_create_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoteAdd,
                                contentDescription = "Create File",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Search & Replace
                        IconButton(
                            onClick = onToggleSearchReplace,
                            modifier = Modifier.size(34.dp).testTag("toolbar_search_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Find & Replace (Ctrl+F)",
                                tint = if (isSearchReplaceOpen) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Dedicated Save Button (Ctrl+S)
                        IconButton(
                            onClick = {
                                onSaveCurrent()
                                val fileName = activeFile?.name ?: "file"
                                showSaveToast = "💾 Saved $fileName (Ctrl+S)"
                            },
                            modifier = Modifier.size(34.dp).testTag("toolbar_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save (Ctrl+S)",
                                tint = if (activeFile?.isModified == true) Color(0xFFF59E0B) else theme.primaryColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Undo
                        IconButton(
                            onClick = onUndo,
                            enabled = canUndo,
                            modifier = Modifier.size(34.dp).testTag("toolbar_undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (canUndo) theme.onSurfaceColor else theme.lineNumberColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Redo
                        IconButton(
                            onClick = onRedo,
                            enabled = canRedo,
                            modifier = Modifier.size(34.dp).testTag("toolbar_redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (canRedo) theme.onSurfaceColor else theme.lineNumberColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Prettier Code Formatter
                        IconButton(
                            onClick = onFormatWithPrettier,
                            modifier = Modifier.size(34.dp).testTag("toolbar_prettier_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Format with Prettier",
                                tint = theme.secondaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center Lint Indicators Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(theme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("lint_indicators_badge")
                    ) {
                        if (errorCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).background(Color(0xFFEF4444), CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("$errorCount", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (warningCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).background(Color(0xFFF59E0B), CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("$warningCount", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (infoCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).background(Color(0xFF3B82F6), CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("$infoCount", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (errorCount == 0 && warningCount == 0 && infoCount == 0) {
                            Text("No issues", color = Color(0xFF10B981), fontSize = 11.sp)
                        }
                    }

                    // Right action icons: Minimap, Zen Mode, Ask AI, Shortcuts Help, and REAL RUN BUTTON
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Minimap Toggle
                        IconButton(
                            onClick = onToggleMinimap,
                            modifier = Modifier.size(34.dp).testTag("toolbar_minimap_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Minimap",
                                tint = if (isMinimapVisible) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Zen Mode Toggle
                        IconButton(
                            onClick = onToggleZenMode,
                            modifier = Modifier.size(34.dp).testTag("toolbar_zen_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "Zen Mode",
                                tint = if (isZenMode) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Ask AI Button in toolbar
                        IconButton(
                            onClick = onToggleAiPanel,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryColor.copy(alpha = 0.15f))
                                .testTag("toolbar_ask_ai_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask AI",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Keyboard Shortcuts Cheat Sheet Quick Button
                        IconButton(
                            onClick = { showShortcutsHelpDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surfaceVariant.copy(alpha = 0.6f))
                                .testTag("toolbar_shortcuts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Keyboard Shortcuts",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Open Terminal quick button
                        IconButton(
                            onClick = onOpenTerminal,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surfaceVariant)
                                .testTag("toolbar_open_terminal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // REAL RUN BUTTON (Runs active code & opens live execution output)
                        Button(
                            onClick = {
                                val current = activeFile ?: openFiles.firstOrNull()
                                if (current != null) {
                                    val result = CodeExecutionEngine.execute(current, openFiles)
                                    executionResult = result
                                    onRunCode()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("run_code_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run Active Code (Ctrl+Enter)", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            val ext = activeFile?.name?.substringAfterLast('.', "")?.uppercase() ?: ""
                            if (ext.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = ext,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD1FAE5),
                                    modifier = Modifier
                                        .background(Color(0xFF047857), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB BAR (Multi-file tabs with dirty indicators & close buttons)
        if (!isZenMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(theme.surfaceColor.copy(alpha = 0.7f))
                    .horizontalScroll(rememberScrollState())
                    .testTag("tab_bar"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                openFiles.forEachIndexed { index, file ->
                    val isActive = index == activeFileIndex
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(if (isActive) theme.editorBackground else Color.Transparent)
                            .clickable { onSelectTab(index) }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (file.isModified) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFFF59E0B), CircleShape)
                            )
                        }

                        Text(
                            text = file.name,
                            color = if (isActive) theme.primaryColor else theme.lineNumberColor,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )

                        if (openFiles.size > 1) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close tab",
                                tint = theme.lineNumberColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onCloseTab(index) }
                                    .testTag("close_tab_${file.name}")
                            )
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(theme.borderColor))
                }

                // Add new tab button
                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier.size(32.dp).testTag("add_new_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Search / Replace overlay
        if (isSearchReplaceOpen) {
            SearchReplaceOverlay(
                theme = theme,
                onFindNext = { query ->
                    if (query.isNotEmpty()) {
                        val text = textFieldValue.text
                        val start = (textFieldValue.selection.end).coerceAtMost(text.length)
                        val idx = text.indexOf(query, start, ignoreCase = true)
                        if (idx != -1) {
                            textFieldValue = TextFieldValue(text, TextRange(idx, idx + query.length))
                        } else {
                            val wrapIdx = text.indexOf(query, 0, ignoreCase = true)
                            if (wrapIdx != -1) {
                                textFieldValue = TextFieldValue(text, TextRange(wrapIdx, wrapIdx + query.length))
                            }
                        }
                    }
                },
                onFindPrevious = { query ->
                    if (query.isNotEmpty()) {
                        val text = textFieldValue.text
                        val start = (textFieldValue.selection.start - 1).coerceAtLeast(0)
                        val idx = text.lastIndexOf(query, start, ignoreCase = true)
                        if (idx != -1) {
                            textFieldValue = TextFieldValue(text, TextRange(idx, idx + query.length))
                        }
                    }
                },
                onReplace = { query, replaceWith ->
                    if (query.isNotEmpty()) {
                        val text = textFieldValue.text
                        val curSel = textFieldValue.selection
                        if (curSel.length > 0 && text.substring(curSel.start, curSel.end).equals(query, ignoreCase = true)) {
                            val newText = text.substring(0, curSel.start) + replaceWith + text.substring(curSel.end)
                            textFieldValue = TextFieldValue(newText, TextRange(curSel.start + replaceWith.length))
                            onContentChange(newText)
                        } else {
                            val idx = text.indexOf(query, curSel.start, ignoreCase = true)
                            if (idx != -1) {
                                val newText = text.substring(0, idx) + replaceWith + text.substring(idx + query.length)
                                textFieldValue = TextFieldValue(newText, TextRange(idx + replaceWith.length))
                                onContentChange(newText)
                            }
                        }
                    }
                },
                onReplaceAll = { query, replaceWith ->
                    if (query.isNotEmpty()) {
                        val newText = textFieldValue.text.replace(query, replaceWith, ignoreCase = true)
                        textFieldValue = TextFieldValue(newText, TextRange(0))
                        onContentChange(newText)
                    }
                },
                onClose = onToggleSearchReplace
            )
        }

        // MAIN WORKSPACE BODY (Sidebar + Editor + Minimap)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // File Explorer Sidebar with Project Templates
                if (isExplorerSidebarOpen && !isZenMode) {
                    FileExplorerSidebar(
                        theme = theme,
                        files = openFiles,
                        gitStatus = gitStatus,
                        trackedFolders = trackedFolders,
                        onOpenFile = onOpenFileFromSidebar,
                        onNewFile = onNewFileInSidebar,
                        onNewFolder = onNewFolderInSidebar,
                        onNewFolderWithTemplate = onNewFolderInSidebarWithTemplate,
                        onNewFileInFolder = onNewFileInSidebarWithFolder,
                        onDeleteFolder = onDeleteFolderInSidebar,
                        onStageFile = onStageGitFile,
                        onUnstageFile = onUnstageGitFile,
                        onStageAll = onStageAllGitFiles,
                        onUnstageAll = onUnstageAllGitFiles,
                        onCommit = onCommitGit,
                        onPush = onPushGit,
                        onCloseSidebar = onToggleExplorerSidebar
                    )
                }

                // Code Editor with Full Syntax Highlighting & Line Numbers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(theme.editorBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(editorScrollState)
                            .padding(bottom = 60.dp)
                    ) {
                        // Slim Gutter
                        Column(
                            modifier = Modifier
                                .width(gutterWidthDp)
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (lineIdx in 0 until totalLines) {
                                val lineNum = lineIdx + 1
                                val isCurrentLine = lineIdx == currentLineIndex
                                val issueOnLine = lintIssues.firstOrNull { it.line == lineNum }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                ) {
                                    if (issueOnLine != null) {
                                        val icon = when (issueOnLine.severity) {
                                            LintSeverity.ERROR -> Icons.Default.Error
                                            LintSeverity.WARNING -> Icons.Default.Warning
                                            LintSeverity.INFO -> Icons.Default.Info
                                        }
                                        val iconColor = when (issueOnLine.severity) {
                                            LintSeverity.ERROR -> Color(0xFFEF4444)
                                            LintSeverity.WARNING -> Color(0xFFF59E0B)
                                            LintSeverity.INFO -> Color(0xFF3B82F6)
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = issueOnLine.message,
                                            tint = iconColor,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { selectedLintIssue = issueOnLine }
                                                .testTag("lint_icon_line_$lineNum")
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }

                                    Text(
                                        text = "$lineNum",
                                        color = if (isCurrentLine) theme.activeLineNumberColor else theme.lineNumberColor,
                                        fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.testTag("line_number_$lineNum")
                                    )
                                }
                            }
                        }

                        // Code Text Area with FULL SYNTAX HIGHLIGHTING (PrismSyntaxHighlighter via VisualTransformation)
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newVal ->
                                val transformed = SmartCodeInputHandler.handleTextChange(textFieldValue, newVal)
                                textFieldValue = transformed
                                onContentChange(transformed.text)
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = Color.Unspecified
                            ),
                            cursorBrush = SolidColor(theme.primaryColor),
                            visualTransformation = { text ->
                                val lang = activeFile?.language ?: CodeFile.detectLanguage(activeFile?.name ?: "")
                                val annotated = PrismSyntaxHighlighter.highlightCode(text.text, lang, theme)
                                TransformedText(annotated, OffsetMapping.Identity)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 12.dp)
                                .onPreviewKeyEvent(handleEditorKeyEvent)
                                .testTag("code_editor_textarea")
                        )
                    }

                    // Popup for Lint Issue
                    selectedLintIssue?.let { issue ->
                        Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = { selectedLintIssue = null }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = theme.surfaceColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(0.9f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Line ${issue.line}: ${issue.severity.name}",
                                            color = when (issue.severity) {
                                                LintSeverity.ERROR -> Color(0xFFEF4444)
                                                LintSeverity.WARNING -> Color(0xFFF59E0B)
                                                LintSeverity.INFO -> Color(0xFF3B82F6)
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        IconButton(
                                            onClick = { selectedLintIssue = null },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = issue.message,
                                        color = theme.onSurfaceColor,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val prompt = "Fix the following issue on line ${issue.line}: ${issue.message}\nCode:\n```\n${textFieldValue.text}\n```"
                                            onOpenAiWithPrompt(prompt)
                                            selectedLintIssue = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("fix_with_ai_button")
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Fix with AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Code Minimap (Connected to editor scrolling)
                if (isMinimapVisible) {
                    val scrollFraction = if (editorScrollState.maxValue > 0) {
                        (editorScrollState.value.toFloat() / editorScrollState.maxValue.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    CodeMinimap(
                        content = textFieldValue.text,
                        theme = theme,
                        scrollProgress = scrollFraction,
                        onScrollToFraction = { fraction ->
                            coroutineScope.launch {
                                val target = (fraction * editorScrollState.maxValue).toInt()
                                editorScrollState.scrollTo(target)
                            }
                        },
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }

            // FLOATING ZEN MODE EXIT PILL (Always visible when Zen mode is active so user is NEVER stuck!)
            if (isZenMode) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.primaryColor,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable { onToggleZenMode() }
                        .testTag("exit_zen_mode_floating_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Zen Mode",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Exit Zen",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Floating Save Confirmation Toast Banner
            showSaveToast?.let { toastText ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF10B981),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Text(
                            text = toastText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // BOTTOM TOOLBARS (Strictly ordered):
        // 1. Suggestion bar row
        AutocompleteBar(
            theme = theme,
            suggestions = autocompleteSuggestions,
            isEnabled = isSuggestionsEnabled,
            onToggleEnabled = onToggleSuggestionsEnabled,
            onSelectSuggestion = { suggestion ->
                val text = textFieldValue.text
                val cursor = textFieldValue.selection.start
                val newText = text.substring(0, cursor) + suggestion.insertText + text.substring(cursor)
                val newCursor = cursor + suggestion.insertText.length
                textFieldValue = TextFieldValue(newText, TextRange(newCursor))
                onContentChange(newText)
                onDismissAutocomplete()
            },
            onDismiss = onDismissAutocomplete
        )

        // 2. Special-character toolbar row with Quick Developer Shortcuts
        SpecialCharactersToolbar(
            theme = theme,
            onInsertSymbol = { symbol ->
                val transformed = SmartCodeInputHandler.insertToolbarSymbol(textFieldValue, symbol)
                textFieldValue = transformed
                onContentChange(transformed.text)
            },
            onSave = {
                onSaveCurrent()
                val fileName = activeFile?.name ?: "file"
                showSaveToast = "💾 Saved $fileName"
            },
            onToggleSearch = onToggleSearchReplace,
            onRunCode = {
                val current = activeFile ?: openFiles.firstOrNull()
                if (current != null) {
                    executionResult = CodeExecutionEngine.execute(current, openFiles)
                    onRunCode()
                }
            },
            onUndo = onUndo,
            onRedo = onRedo,
            canUndo = canUndo,
            canRedo = canRedo,
            onShowShortcutsHelp = { showShortcutsHelpDialog = true }
        )
    }

    // LIVE EXECUTION OUTPUT SHEET (Appears immediately when code is run!)
    executionResult?.let { result ->
        ExecutionOutputSheet(
            theme = theme,
            result = result,
            onRerun = {
                val current = activeFile ?: openFiles.firstOrNull()
                if (current != null) {
                    executionResult = CodeExecutionEngine.execute(current, openFiles)
                }
            },
            onOpenInTerminal = {
                onOpenTerminal()
                executionResult = null
            },
            onDismiss = { executionResult = null }
        )
    }

    // AI Assistant Bottom Sheet Panel
    if (isAiPanelOpen) {
        AiAssistantPanel(
            theme = theme,
            messages = aiMessages,
            isLoading = isAiLoading,
            prefilledPrompt = prefilledAiPrompt,
            onSendMessage = onSendAiMessage,
            onStopGenerating = onStopAiGenerating,
            onClearChat = onClearAiChat,
            onClose = onCloseAiPanel,
            onReviewDiff = onReviewDiff,
            currentEditorCode = textFieldValue.text
        )
    }

    // Diff Preview Dialog for AI code proposals
    if (isDiffPreviewOpen && activeProposedDiff != null) {
        DiffPreviewDialog(
            theme = theme,
            diff = activeProposedDiff,
            onApply = onApplyDiff,
            onReject = onRejectDiff
        )
    }

    // Create New File Dialog in Editor
    if (showCreateFileDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = {
                Text(
                    text = "Create New File",
                    color = theme.onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter file name with extension (e.g. Main.java, script.py, index.html, app.js):",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = createFileNameInput,
                        onValueChange = { createFileNameInput = it },
                        placeholder = { Text("Main.java", color = theme.lineNumberColor, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("editor_create_file_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.onSurfaceColor,
                            unfocusedTextColor = theme.onSurfaceColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = createFileNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onNewFileInSidebar(trimmed)
                            showCreateFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("editor_confirm_create_file_button")
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCreateFileDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = theme.onSurfaceColor)
                }
            },
            containerColor = theme.surfaceColor,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Keyboard Shortcuts Cheat Sheet Dialog
    if (showShortcutsHelpDialog) {
        KeyboardShortcutsHelpDialog(
            theme = theme,
            onDismiss = { showShortcutsHelpDialog = false }
        )
    }
}

@Composable
fun KeyboardShortcutsHelpDialog(
    theme: EditorTheme,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = theme.primaryColor)
                Text("Keyboard Shortcuts", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            val shortcuts = listOf(
                Pair("Save active file", listOf("Ctrl", "S")),
                Pair("Find & Replace in file", listOf("Ctrl", "F")),
                Pair("Run code immediately", listOf("Ctrl", "↵ Enter")),
                Pair("Toggle Explorer sidebar", listOf("Ctrl", "B")),
                Pair("Undo last edit", listOf("Ctrl", "Z")),
                Pair("Redo edit", listOf("Ctrl", "Y")),
                Pair("Close active tab", listOf("Ctrl", "W")),
                Pair("Create new file", listOf("Ctrl", "N")),
                Pair("Shortcuts cheat sheet", listOf("Ctrl", "/"))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hardware, Bluetooth, and emulator keyboard shortcuts configured for Linq Studios:",
                    color = theme.lineNumberColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                shortcuts.forEach { (action, keys) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = action,
                            color = theme.onSurfaceColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            keys.forEach { key ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = theme.editorBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                                ) {
                                    Text(
                                        text = key,
                                        color = theme.primaryColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got it", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = theme.surfaceColor,
        shape = RoundedCornerShape(12.dp)
    )
}
