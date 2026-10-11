package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.editor.EditorColorScheme
import com.example.editor.VirtualizedCodeEditor
import com.example.model.CodeFile
import com.example.model.EditorTheme

@Composable
fun CodeEditorView(
    theme: EditorTheme,
    openFiles: List<CodeFile>,
    activeFileIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onNewTab: () -> Unit,
    onContentChange: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    fontSizeSp: Int = 13,
    isWordWrapEnabled: Boolean = false,
    isBirdsEyeViewEnabled: Boolean = true,
    onNavigatePath: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeFile = openFiles.getOrNull(activeFileIndex)

    val editorColors = EditorColorScheme(
        background = theme.editorBackground,
        gutterBackground = theme.gutterBackground,
        gutterBorder = theme.borderColor,
        lineNumber = theme.lineNumberColor,
        currentLineNumber = theme.currentLineNumberColor,
        currentLineHighlight = theme.currentLineBackground,
        textColor = theme.onSurfaceColor,
        cursorColor = theme.cursorColor,
        selectionColor = theme.selectionColor,
        searchHighlight = theme.searchHighlightColor,
        keyword = theme.keywordColor,
        string = theme.stringColor,
        comment = theme.commentColor,
        number = theme.numberColor,
        type = theme.typeColor,
        function = theme.functionColor,
        operator = theme.operatorColor
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .testTag("code_editor_view")
    ) {
        // Minimalist Clean Tab Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            color = theme.gutterBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                openFiles.forEachIndexed { index, file ->
                    val isActive = index == activeFileIndex
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(if (isActive) theme.editorBackground else theme.gutterBackground)
                            .clickable { onSelectTab(index) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (file.isDirty) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                            )
                        }

                        Text(
                            text = file.name,
                            color = if (isActive) theme.onSurfaceColor else theme.lineNumberColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onCloseTab(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close tab",
                                tint = theme.lineNumberColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("btn_new_tab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New file",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Breadcrumb Navigation Row for Active File
        if (activeFile != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                color = theme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, theme.borderColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val segments = com.example.model.BreadcrumbHelper.getSegments(
                        filePath = activeFile.path,
                        fileName = activeFile.name
                    )
                    segments.forEachIndexed { idx, segment ->
                        val isLast = idx == segments.lastIndex

                        Text(
                            text = segment.label,
                            color = if (isLast) theme.primaryColor else theme.lineNumberColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier
                                .clickable(enabled = onNavigatePath != null && segment.isDirectory) {
                                    onNavigatePath?.invoke(segment.path)
                                }
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        )

                        if (!isLast) {
                            Text(
                                text = "›",
                                color = theme.lineNumberColor.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // High-Performance Virtualized Code Editor
        if (activeFile != null) {
            VirtualizedCodeEditor(
                text = activeFile.content,
                onTextChange = onContentChange,
                colorScheme = editorColors,
                fontSizeSp = fontSizeSp,
                isWordWrap = isWordWrapEnabled,
                canUndo = activeFile.undoStack.isNotEmpty(),
                canRedo = activeFile.redoStack.isNotEmpty(),
                onUndo = {
                    val undone = activeFile.undo()
                    onContentChange(undone.content)
                },
                onRedo = {
                    val redone = activeFile.redo()
                    onContentChange(redone.content)
                },
                isBirdsEyeViewEnabled = isBirdsEyeViewEnabled,
                initialCursorPosition = activeFile.cursorPosition,
                fileName = activeFile.name,
                fileId = activeFile.id,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No open files",
                        color = theme.lineNumberColor,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = onNewTab,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor,
                            contentColor = theme.onPrimaryColor
                        )
                    ) {
                        Text("Create File")
                    }
                }
            }
        }
    }
}
