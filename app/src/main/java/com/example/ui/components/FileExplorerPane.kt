package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.FileItem
import java.io.File

@Composable
fun FileExplorerPane(
    theme: EditorTheme,
    currentDirectory: File,
    files: List<FileItem>,
    onOpenFile: (FileItem) -> Unit,
    onNavigateDir: (File) -> Unit,
    onNavigateUp: () -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onExportZip: (File) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<FileItem?>(null) }
    var itemToDelete by remember { mutableStateOf<FileItem?>(null) }
    var inputName by remember { mutableStateOf("") }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("file_explorer_pane"),
        color = theme.surfaceColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        enabled = currentDirectory.parentFile != null,
                        modifier = Modifier.testTag("btn_dir_up")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Up",
                            tint = if (currentDirectory.parentFile != null) theme.primaryColor else theme.lineNumberColor
                        )
                    }

                    Column {
                        Text(
                            text = currentDirectory.name.ifEmpty { "Root" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor
                        )
                        Text(
                            text = currentDirectory.absolutePath,
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.lineNumberColor,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            inputName = ""
                            showNewFileDialog = true
                        },
                        modifier = Modifier.testTag("btn_create_file")
                    ) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = theme.primaryColor)
                    }

                    IconButton(
                        onClick = {
                            inputName = ""
                            showNewFolderDialog = true
                        },
                        modifier = Modifier.testTag("btn_create_folder")
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = theme.primaryColor)
                    }

                    IconButton(
                        onClick = { onExportZip(currentDirectory) },
                        modifier = Modifier.testTag("btn_export_zip")
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = "Export ZIP", tint = theme.primaryColor)
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("btn_refresh_files")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = theme.onSurfaceColor)
                    }
                }
            }

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("file_search_field"),
                placeholder = { Text("Filter files in folder...", color = theme.lineNumberColor, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.lineNumberColor) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primaryColor,
                    unfocusedBorderColor = theme.borderColor,
                    focusedTextColor = theme.onSurfaceColor,
                    unfocusedTextColor = theme.onSurfaceColor
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // File List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                if (filteredFiles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No files found in folder", color = theme.lineNumberColor, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filteredFiles, key = { it.path }) { item ->
                        FileRowItem(
                            item = item,
                            theme = theme,
                            onClick = {
                                if (item.isDirectory) {
                                    onNavigateDir(File(item.path))
                                } else {
                                    onOpenFile(item)
                                }
                            },
                            onRename = {
                                itemToRename = item
                                inputName = item.name
                            },
                            onDelete = {
                                itemToDelete = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog: New File
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", color = theme.onSurfaceColor) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("File Name (e.g. main.py, Index.kt)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onNewFile(inputName.trim())
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: New Folder
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create New Folder", color = theme.onSurfaceColor) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onNewFolder(inputName.trim())
                            showNewFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Rename
    itemToRename?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = { Text("Rename ${if (target.isDirectory) "Folder" else "File"}", color = theme.onSurfaceColor) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onRename(target.path, inputName.trim())
                            itemToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Delete
    itemToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Confirmation", color = theme.onSurfaceColor) },
            text = {
                Text(
                    "Are you sure you want to permanently delete \"${target.name}\"?\n${if (target.isDirectory) "All contents inside will be deleted." else ""}",
                    color = theme.onSurfaceColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(target.path)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FileRowItem(
    item: FileItem,
    theme: EditorTheme,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("file_item_${item.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.Code,
                contentDescription = null,
                tint = if (item.isDirectory) Color(0xFFFBBF24) else theme.primaryColor,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.name,
                    color = theme.onSurfaceColor,
                    fontSize = 14.sp,
                    fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (item.isDirectory) "Directory" else "${item.formattedSize} • ${item.formattedDate}",
                    color = theme.lineNumberColor,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRename,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.DriveFileMove, contentDescription = "Rename", tint = theme.lineNumberColor, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444).copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
