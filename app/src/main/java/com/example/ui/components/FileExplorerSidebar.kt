package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.GitStatus
import java.io.File

@Composable
fun FileExplorerSidebar(
    theme: EditorTheme,
    directoryFiles: List<FileItem>,
    openFiles: List<CodeFile>,
    activeFileIndex: Int,
    gitStatus: GitStatus? = null,
    onStageFile: (String) -> Unit = {},
    onStageAll: () -> Unit = {},
    onUnstageFile: (String) -> Unit = {},
    onUnstageAll: () -> Unit = {},
    onCommit: (String) -> Unit = {},
    onPush: () -> Unit = {},
    onCreateBranch: (String) -> Unit = {},
    onSwitchBranch: (String) -> Unit = {},
    onOpenFile: (File) -> Unit,
    onNewFile: (String) -> Unit,
    onSaveActive: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Files, 1 = Git
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }

    val activeFile = openFiles.getOrNull(activeFileIndex)
    val gitChangesCount = (gitStatus?.stagedFiles?.size ?: 0) + (gitStatus?.unstagedFiles?.size ?: 0)

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(theme.surfaceColor)
            .border(androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor))
            .testTag("file_explorer_sidebar")
    ) {
        // TAB HEADER: FILES vs GIT SOURCE CONTROL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.editorBackground)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Files Tab
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (activeTab == 0) theme.surfaceColor else Color.Transparent,
                border = if (activeTab == 0) androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeTab = 0 }
                    .testTag("tab_explorer_files")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Files",
                        tint = if (activeTab == 0) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Files",
                        fontSize = 11.sp,
                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeTab == 0) theme.onSurfaceColor else theme.lineNumberColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Git Tab
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (activeTab == 1) theme.surfaceColor else Color.Transparent,
                border = if (activeTab == 1) androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeTab = 1 }
                    .testTag("tab_explorer_git")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Git",
                        tint = if (activeTab == 1) Color(0xFFF97316) else theme.lineNumberColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Git",
                        fontSize = 11.sp,
                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeTab == 1) theme.onSurfaceColor else theme.lineNumberColor
                    )
                    if (gitChangesCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFF97316), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$gitChangesCount",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onCloseSidebar,
                modifier = Modifier.size(24.dp).testTag("btn_close_sidebar")
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = theme.lineNumberColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        HorizontalDivider(color = theme.borderColor)

        if (activeTab == 1 && gitStatus != null) {
            // RENDER GIT SOURCE CONTROL PANEL
            GitSidePanel(
                theme = theme,
                gitStatus = gitStatus,
                onStageFile = onStageFile,
                onStageAll = onStageAll,
                onUnstageFile = onUnstageFile,
                onUnstageAll = onUnstageAll,
                onCommit = onCommit,
                onPush = onPush,
                onCreateBranch = onCreateBranch,
                onSwitchBranch = onSwitchBranch,
                onClose = onCloseSidebar,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // RENDER FILE EXPLORER
            Column(modifier = Modifier.fillMaxSize()) {
                // Action Buttons: New File & Save
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            newFileNameInput = ""
                            showNewFileDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("btn_sidebar_new_file")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("New File", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onSaveActive,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("btn_sidebar_save_file")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Save", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }

                HorizontalDivider(color = theme.borderColor)

                // PROJECT FILES LIST (Stored locally on device)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    item {
                        Text(
                            text = "PROJECT FILES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.lineNumberColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    items(directoryFiles) { fileItem ->
                        val isOpen = openFiles.any { it.file.absolutePath == fileItem.file.absolutePath }
                        val isSelected = activeFile?.file?.absolutePath == fileItem.file.absolutePath

                        val (badgeColor, extLabel) = when (fileItem.extension.lowercase()) {
                            "html", "htm" -> Pair(Color(0xFFF43F5E), "HTML")
                            "css" -> Pair(Color(0xFF38BDF8), "CSS")
                            "js", "ts" -> Pair(Color(0xFFF59E0B), "JS")
                            "py" -> Pair(Color(0xFF6366F1), "PY")
                            "sh", "bash" -> Pair(Color(0xFF10B981), "SH")
                            "kt" -> Pair(Color(0xFFA855F7), "KT")
                            "rs" -> Pair(Color(0xFFEA580C), "RS")
                            "go" -> Pair(Color(0xFF06B6D4), "GO")
                            "sql" -> Pair(Color(0xFF8B5CF6), "SQL")
                            else -> Pair(theme.lineNumberColor, fileItem.extension.uppercase().take(3))
                        }

                        Surface(
                            color = if (isSelected) theme.editorBackground else Color.Transparent,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)) else null,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .clickable {
                                    if (!fileItem.isDirectory) {
                                        onOpenFile(fileItem.file)
                                    }
                                }
                                .testTag("file_item_${fileItem.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (fileItem.isDirectory) {
                                        Icon(Icons.Default.Folder, contentDescription = "Folder", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(extLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                                        }
                                    }

                                    Text(
                                        text = fileItem.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) theme.primaryColor else theme.onSurfaceColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isOpen) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(theme.primaryColor, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // NEW FILE MODAL DIALOG
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = theme.primaryColor)
                    Text("Create New File", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter filename with extension (e.g. script.js, index.html, main.py):", fontSize = 12.sp, color = theme.onSurfaceColor)
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        placeholder = { Text("app.js") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_filename")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFileNameInput.trim()
                        if (name.isNotEmpty()) {
                            onNewFile(name)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.testTag("btn_confirm_new_file")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
