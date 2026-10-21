package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun FilesScreen(
    theme: EditorTheme,
    files: List<CodeFile>,
    currentDirectory: String,
    onOpenFile: (CodeFile) -> Unit,
    onNavigateDir: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit,
    onDelete: (CodeFile) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .padding(16.dp)
            .testTag("files_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Path navigation bar & actions (Responsive non-overlapping row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier.size(34.dp).testTag("files_nav_up")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Up", tint = theme.onSurfaceColor)
                }
                Text(
                    text = currentDirectory.ifEmpty { "/workspace" },
                    color = theme.onSurfaceColor,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        newFolderName = ""
                        showNewFolderDialog = true
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("files_create_folder_button")
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Folder", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryColor)
                }

                Button(
                    onClick = {
                        newFileName = ""
                        showNewFileDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("files_create_file_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar (Part 5C: unclipped text)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter files in folder...", color = theme.lineNumberColor, fontSize = 13.sp, lineHeight = 18.sp) },
            textStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, color = theme.onSurfaceColor),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("files_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.onSurfaceColor,
                unfocusedTextColor = theme.onSurfaceColor,
                focusedBorderColor = theme.primaryColor,
                unfocusedBorderColor = theme.borderColor,
                focusedContainerColor = theme.surfaceColor,
                unfocusedContainerColor = theme.surfaceColor
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // File List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredFiles) { file ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFile(file) }
                        .testTag("file_row_${file.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
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
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = file.name,
                                    color = theme.onSurfaceColor,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${file.content.length} bytes • ${file.language}",
                                    color = theme.lineNumberColor,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDelete(file) },
                            modifier = Modifier.size(32.dp).testTag("delete_file_${file.name}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create File in Workspace", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter filename with extension (e.g. Example.java, script.py, app.js):",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        placeholder = { Text("Example.java", color = theme.lineNumberColor, fontSize = 13.sp, lineHeight = 18.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, color = theme.onSurfaceColor),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("new_file_dialog_input"),
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
                        val trimmed = newFileName.trim()
                        if (trimmed.isNotEmpty()) {
                            onNewFile(trimmed)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_file")
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
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("e.g. components, utils, scripts", color = theme.lineNumberColor, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("new_folder_dialog_input"),
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
                        val trimmed = newFolderName.trim()
                        if (trimmed.isNotEmpty()) {
                            onNewFolder(trimmed)
                            showNewFolderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_folder")
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
