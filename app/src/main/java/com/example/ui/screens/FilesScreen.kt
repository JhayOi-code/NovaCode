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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.ProjectTemplateType

@Composable
fun FilesScreen(
    theme: EditorTheme,
    files: List<CodeFile>,
    currentDirectory: String,
    trackedFolders: List<String> = emptyList(),
    onOpenFile: (CodeFile) -> Unit,
    onNavigateDir: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit,
    onNewFolderWithTemplate: (String, ProjectTemplateType) -> Unit = { name, _ -> onNewFolder(name) },
    onDeleteFolder: (String) -> Unit = {},
    onDelete: (CodeFile) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentSubFolder by remember { mutableStateOf<String?>(null) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var selectedProjectTemplate by remember { mutableStateOf(ProjectTemplateType.BLANK_FOLDER) }
    var newFolderName by remember { mutableStateOf("my_folder") }

    val allFolders = remember(files, trackedFolders) {
        val set = linkedSetOf<String>()
        set.addAll(trackedFolders)
        files.forEach { file ->
            val clean = file.name.trim().removePrefix("/")
            if (clean.contains('/')) set.add(clean.substringBeforeLast('/'))
        }
        set.toList()
    }

    val displayedFiles = remember(files, currentSubFolder, searchQuery) {
        val base = if (currentSubFolder != null) {
            files.filter { it.name.startsWith("$currentSubFolder/") }
        } else {
            files.filter { !it.name.trim().removePrefix("/").contains('/') }
        }
        if (searchQuery.isBlank()) base
        else base.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
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
                    onClick = {
                        if (currentSubFolder != null) {
                            currentSubFolder = null
                        } else {
                            onNavigateUp()
                        }
                    },
                    modifier = Modifier.size(34.dp).testTag("files_nav_up")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Up", tint = theme.onSurfaceColor)
                }
                Text(
                    text = if (currentSubFolder != null) "/workspace/$currentSubFolder" else currentDirectory.ifEmpty { "/workspace" },
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

        // Folders & Files List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Folders Section (visible at root directory when not searching)
            if (currentSubFolder == null && searchQuery.isBlank() && allFolders.isNotEmpty()) {
                item(key = "folders_header") {
                    Text(
                        text = "FOLDERS (${allFolders.size})",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(allFolders, key = { "folder_$it" }) { folder ->
                    val fileCount = files.count { it.name.startsWith("$folder/") }
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSubFolder = folder }
                            .testTag("folder_card_$folder")
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
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = folder,
                                        color = theme.onSurfaceColor,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (fileCount == 0) "Empty folder • Tap to open & add files" else "$fileCount files in directory",
                                        color = theme.lineNumberColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onDeleteFolder(folder) },
                                    modifier = Modifier.size(32.dp).testTag("delete_folder_$folder")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Folder", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                item(key = "files_header") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROOT FILES (${displayedFiles.size})",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Empty state for subfolder
            if (currentSubFolder != null && displayedFiles.isEmpty()) {
                item(key = "empty_subfolder") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(36.dp))
                            Text(
                                text = "This folder is empty",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Tap '+ File' in the top bar to create your first file inside '$currentSubFolder'.",
                                color = theme.lineNumberColor,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(displayedFiles) { file ->
                val displayName = if (currentSubFolder != null) file.name.removePrefix("$currentSubFolder/") else file.name
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
                                    text = displayName,
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
                            val full = if (currentSubFolder != null && !trimmed.startsWith("$currentSubFolder/")) "$currentSubFolder/$trimmed" else trimmed
                            onNewFile(full)
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
                    Text("Create Folder or Project", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose template or create empty folder:",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp
                    )

                    // Template Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProjectTemplateType.values().forEach { template ->
                            val isSelected = selectedProjectTemplate == template
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor) else null,
                                modifier = Modifier.clickable {
                                    selectedProjectTemplate = template
                                    newFolderName = when (template) {
                                        ProjectTemplateType.BLANK_FOLDER -> "my_folder"
                                        ProjectTemplateType.BLANK_PROJECT -> "my_project"
                                        ProjectTemplateType.PYTHON_PROJECT -> "python_app"
                                        ProjectTemplateType.WEB_PROJECT -> "web_site"
                                        ProjectTemplateType.JAVA_PROJECT -> "java_app"
                                        ProjectTemplateType.NODE_PROJECT -> "node_service"
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = template.iconVector,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = if (isSelected) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = template.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) theme.primaryColor else theme.onSurfaceColor
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(13.dp),
                                            tint = theme.primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Template Info Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.editorBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = selectedProjectTemplate.description,
                                color = theme.onSurfaceColor,
                                fontSize = 11.sp
                            )
                            val filesList = selectedProjectTemplate.defaultFiles.joinToString(", ") { it.first }
                            Text(
                                text = "Files created: $filesList",
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Folder Name Input
                    Text(
                        text = "Folder Name:",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        placeholder = { Text("e.g. python_app, web_site, utils", color = theme.lineNumberColor, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp).testTag("new_folder_dialog_input"),
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
                            onNewFolderWithTemplate(trimmed, selectedProjectTemplate)
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
