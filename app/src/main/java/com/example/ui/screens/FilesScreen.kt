package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.FileItem
import java.io.File

@Composable
fun FilesScreen(
    theme: EditorTheme,
    files: List<FileItem>,
    currentDirectory: File,
    onOpenFile: (File) -> Unit,
    onNavigateDir: (File) -> Unit,
    onNavigateUp: () -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit,
    onDelete: (File) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var itemNameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .padding(16.dp)
    ) {
        // Explorer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Workspace Files",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor
                )
                Text(
                    text = currentDirectory.name,
                    fontSize = 12.sp,
                    color = theme.lineNumberColor
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = theme.lineNumberColor)
                }
                IconButton(onClick = {
                    isCreatingFolder = false
                    itemNameInput = ""
                    showCreateDialog = true
                }, modifier = Modifier.testTag("btn_new_file_explorer")) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = theme.primaryColor)
                }
                IconButton(onClick = {
                    isCreatingFolder = true
                    itemNameInput = ""
                    showCreateDialog = true
                }, modifier = Modifier.testTag("btn_new_folder_explorer")) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = Color(0xFF06B6D4))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Files List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(files) { item ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.surfaceColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (item.isDirectory) onNavigateDir(item.file) else onOpenFile(item.file)
                        }
                        .testTag("file_item_${item.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val icon = if (item.isDirectory) Icons.Default.Folder else when (item.extension.lowercase()) {
                                "kt", "java" -> Icons.Default.Code
                                "html", "htm" -> Icons.Default.Html
                                "css" -> Icons.Default.Palette
                                "js", "ts" -> Icons.Default.Javascript
                                else -> Icons.Default.Description
                            }
                            val iconTint = if (item.isDirectory) Color(0xFFF59E0B) else theme.primaryColor

                            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                            Text(
                                text = item.name,
                                color = theme.onSurfaceColor,
                                fontSize = 13.sp,
                                fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        IconButton(
                            onClick = { onDelete(item.file) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (isCreatingFolder) "New Folder" else "New File") },
            text = {
                OutlinedTextField(
                    value = itemNameInput,
                    onValueChange = { itemNameInput = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = itemNameInput.trim()
                        if (name.isNotEmpty()) {
                            if (isCreatingFolder) onNewFolder(name) else onNewFile(name)
                        }
                        showCreateDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
