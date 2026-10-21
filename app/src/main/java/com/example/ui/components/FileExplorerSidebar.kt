package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.GitRepositoryStatus

@Composable
fun FileExplorerSidebar(
    theme: EditorTheme,
    files: List<CodeFile>,
    gitStatus: GitRepositoryStatus,
    onOpenFile: (CodeFile) -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit = {},
    onStageFile: (String) -> Unit,
    onUnstageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onCommit: (String) -> Unit,
    onPush: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGitTabSelected by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }

    val filteredFiles = remember(files, filterText) {
        if (filterText.isBlank()) files
        else files.filter { it.name.contains(filterText, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .testTag("file_explorer_sidebar"),
        color = theme.surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with tab switcher (Files vs Git)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(theme.surfaceColor, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isGitTabSelected) theme.primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { isGitTabSelected = false }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("explorer_files_tab")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = if (!isGitTabSelected) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Files",
                                color = if (!isGitTabSelected) theme.primaryColor else theme.lineNumberColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isGitTabSelected) theme.primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { isGitTabSelected = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("explorer_git_tab")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Commit,
                                contentDescription = null,
                                tint = if (isGitTabSelected) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Git",
                                color = if (isGitTabSelected) theme.primaryColor else theme.lineNumberColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (gitStatus.changes.isNotEmpty()) {
                                Text(
                                    text = "(${gitStatus.changes.size})",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (!isGitTabSelected) {
                        IconButton(
                            onClick = {
                                newFileNameInput = ""
                                showNewFileDialog = true
                            },
                            modifier = Modifier.size(30.dp).testTag("explorer_new_file_button")
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = theme.primaryColor, modifier = Modifier.size(17.dp))
                        }
                        IconButton(
                            onClick = {
                                newFolderNameInput = ""
                                showNewFolderDialog = true
                            },
                            modifier = Modifier.size(30.dp).testTag("explorer_new_folder_button")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = theme.primaryColor, modifier = Modifier.size(17.dp))
                        }
                    }
                    IconButton(
                        onClick = onCloseSidebar,
                        modifier = Modifier.size(30.dp).testTag("close_explorer_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Sidebar", tint = theme.lineNumberColor, modifier = Modifier.size(17.dp))
                    }
                }
            }

            if (isGitTabSelected) {
                // Git Panel view
                GitSidePanel(
                    theme = theme,
                    gitStatus = gitStatus,
                    onStageFile = onStageFile,
                    onUnstageFile = onUnstageFile,
                    onStageAll = onStageAll,
                    onUnstageAll = onUnstageAll,
                    onCommit = onCommit,
                    onPush = onPush,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // File Explorer view
                Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                    // Filter files input - properly unclipped! (Part 5C)
                    OutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        placeholder = {
                            Text(
                                text = "Filter files in folder...",
                                color = theme.lineNumberColor,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = theme.onSurfaceColor
                        ),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .testTag("filter_files_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.onSurfaceColor,
                            unfocusedTextColor = theme.onSurfaceColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor,
                            focusedContainerColor = theme.editorBackground,
                            unfocusedContainerColor = theme.editorBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredFiles) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onOpenFile(file) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .testTag("file_item_${file.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = when {
                                        file.name.endsWith(".java") -> Color(0xFFEF4444)
                                        file.name.endsWith(".kt") -> Color(0xFFA78BFA)
                                        file.name.endsWith(".py") -> Color(0xFF3B82F6)
                                        file.name.endsWith(".js") -> Color(0xFFFBBF24)
                                        file.name.endsWith(".html") -> Color(0xFFF97316)
                                        else -> theme.primaryColor
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = file.name,
                                    color = theme.onSurfaceColor,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                if (file.isModified) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New File Dialog (Part 1 and 5C: unclipped inputs)
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
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
                        text = "Enter file name with extension (e.g. Example.java, script.py, app.js):",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        placeholder = { Text("Example.java", color = theme.lineNumberColor, fontSize = 13.sp, lineHeight = 18.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, color = theme.onSurfaceColor),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("create_file_name_input"),
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
                        val trimmed = newFileNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onNewFile(trimmed)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_file_button")
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showNewFileDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = theme.onSurfaceColor)
                }
            },
            containerColor = theme.surfaceColor,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = theme.primaryColor)
                    Text("Create New Folder", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a name for the new folder:",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newFolderNameInput,
                        onValueChange = { newFolderNameInput = it },
                        placeholder = { Text("e.g. components, utils, scripts", color = theme.lineNumberColor, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("create_folder_name_input"),
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
                        val trimmed = newFolderNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onNewFolder(trimmed)
                            showNewFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_folder_button")
                ) {
                    Text("Create Folder", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showNewFolderDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = theme.onSurfaceColor)
                }
            },
            containerColor = theme.surfaceColor,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
