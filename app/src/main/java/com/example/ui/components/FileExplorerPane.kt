package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
    onMove: (String, String) -> Unit = { _, _ -> },
    onShare: (FileItem) -> Unit = {},
    onClone: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onExportZip: (File) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<FileItem?>(null) }
    var itemToDelete by remember { mutableStateOf<FileItem?>(null) }
    var itemToMove by remember { mutableStateOf<FileItem?>(null) }
    var inputName by remember { mutableStateOf("") }
    var destinationPathInput by remember { mutableStateOf("") }

    // State for Clone Dialog
    var cloneRepoUrl by remember { mutableStateOf("") }
    var cloneFolderName by remember { mutableStateOf("") }
    var cloneBranch by remember { mutableStateOf("main") }
    var cloneToken by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("GitHub") }

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

                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = currentDirectory.name.ifEmpty { "Root" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor
                        )
                        // Interactive Clickable Breadcrumb Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val pathSegments = currentDirectory.absolutePath.split("/").filter { it.isNotEmpty() }
                            Text(
                                text = "root",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.primaryColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .clickable { onNavigateDir(File("/")) }
                                    .padding(end = 2.dp)
                            )
                            pathSegments.forEachIndexed { idx, segment ->
                                Text(text = " / ", color = theme.lineNumberColor.copy(alpha = 0.5f), fontSize = 10.sp)
                                val segmentPath = "/" + pathSegments.take(idx + 1).joinToString("/")
                                val isCurrent = idx == pathSegments.lastIndex
                                Text(
                                    text = segment,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCurrent) theme.onSurfaceColor else theme.primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clickable(enabled = !isCurrent) {
                                            onNavigateDir(File(segmentPath))
                                        }
                                        .padding(horizontal = 1.dp)
                                )
                            }
                        }
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
                        onClick = {
                            cloneRepoUrl = ""
                            cloneFolderName = ""
                            cloneBranch = "main"
                            cloneToken = ""
                            showCloneDialog = true
                        },
                        modifier = Modifier.testTag("btn_clone_repo_explorer")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Clone Repository", tint = theme.primaryColor)
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
                            onShare = { onShare(item) },
                            onMove = {
                                itemToMove = item
                                destinationPathInput = currentDirectory.parentFile?.absolutePath ?: currentDirectory.absolutePath
                            },
                            onRename = {
                                itemToRename = item
                                inputName = item.name
                            },
                            onDelete = {
                                itemToDelete = item
                            },
                            onExportZip = if (item.isDirectory) {
                                { onExportZip(File(item.path)) }
                            } else null
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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

    // Dialog: Move File / Directory
    itemToMove?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToMove = null },
            title = { Text("Move \"${target.name}\"", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current location: ${target.path}",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Quick Destination:",
                        color = theme.onSurfaceColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Quick Destination Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentDirectory.parentFile?.let { parent ->
                            FilterChip(
                                selected = destinationPathInput == parent.absolutePath,
                                onClick = { destinationPathInput = parent.absolutePath },
                                label = { Text("Parent (..)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor
                                )
                            )
                        }

                        // Local folders in current directory
                        files.filter { it.isDirectory && it.path != target.path }.forEach { folder ->
                            FilterChip(
                                selected = destinationPathInput == folder.path,
                                onClick = { destinationPathInput = folder.path },
                                label = { Text(folder.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = destinationPathInput,
                        onValueChange = { destinationPathInput = it },
                        label = { Text("Destination Directory Path") },
                        placeholder = { Text("/path/to/destination/folder") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_move_destination")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (destinationPathInput.isNotBlank()) {
                            onMove(target.path, destinationPathInput.trim())
                            itemToMove = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                    modifier = Modifier.testTag("btn_confirm_move")
                ) {
                    Text("Move Here")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToMove = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Clone Repository (GitHub, GitLab, Bitbucket, Codeberg, Custom)
    if (showCloneDialog) {
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = theme.primaryColor)
                    Text("Clone Git Repository", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Clone any public or private repository from GitHub, GitLab, Bitbucket, Codeberg, or custom Git URLs into your workspace.",
                        fontSize = 12.sp,
                        color = theme.lineNumberColor
                    )

                    // Provider selector row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val providers = listOf(
                            "GitHub" to "https://github.com/octocat/Hello-World.git",
                            "GitLab" to "https://gitlab.com/gitlab-org/gitlab-runner.git",
                            "Bitbucket" to "https://bitbucket.org/atlassian/maven-jgit-buildnumber.git",
                            "Codeberg" to "https://codeberg.org/forgejo/forgejo.git",
                            "Custom" to ""
                        )
                        providers.forEach { (name, sampleUrl) ->
                            FilterChip(
                                selected = selectedProvider == name,
                                onClick = {
                                    selectedProvider = name
                                    if (sampleUrl.isNotEmpty()) {
                                        cloneRepoUrl = sampleUrl
                                        cloneFolderName = sampleUrl.substringAfterLast('/').removeSuffix(".git")
                                    }
                                },
                                label = { Text(name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primaryColor,
                                    selectedLabelColor = theme.onPrimaryColor
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cloneRepoUrl,
                        onValueChange = {
                            cloneRepoUrl = it
                            if (cloneFolderName.isBlank() && it.contains("/")) {
                                cloneFolderName = it.trim().removeSuffix("/").substringAfterLast('/').removeSuffix(".git")
                            }
                        },
                        label = { Text("Repository URL *") },
                        placeholder = { Text("https://github.com/user/repository.git") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_clone_repo_url")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cloneFolderName,
                            onValueChange = { cloneFolderName = it },
                            label = { Text("Folder Name") },
                            placeholder = { Text("Auto") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_clone_folder_name")
                        )

                        OutlinedTextField(
                            value = cloneBranch,
                            onValueChange = { cloneBranch = it },
                            label = { Text("Branch") },
                            placeholder = { Text("main") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_clone_branch")
                        )
                    }

                    OutlinedTextField(
                        value = cloneToken,
                        onValueChange = { cloneToken = it },
                        label = { Text("Personal Access Token (optional)") },
                        placeholder = { Text("Required for private repositories") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_clone_token")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cloneRepoUrl.isNotBlank()) {
                            onClone(cloneRepoUrl.trim(), cloneFolderName.trim(), cloneBranch.trim(), cloneToken.trim())
                            showCloneDialog = false
                        }
                    },
                    enabled = cloneRepoUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor),
                    modifier = Modifier.testTag("btn_confirm_clone_explorer")
                ) {
                    Text("Clone Repository")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneDialog = false }) { Text("Cancel") }
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
    onShare: () -> Unit,
    onMove: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExportZip: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

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

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("file_menu_button_${item.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "File Options",
                    tint = theme.lineNumberColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(theme.surfaceColor)
            ) {
                DropdownMenuItem(
                    text = { Text("Open", color = theme.onSurfaceColor) },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onClick()
                    },
                    modifier = Modifier.testTag("menu_item_open_${item.name}")
                )

                DropdownMenuItem(
                    text = { Text("Share", color = theme.onSurfaceColor) },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onShare()
                    },
                    modifier = Modifier.testTag("menu_item_share_${item.name}")
                )

                DropdownMenuItem(
                    text = { Text("Move", color = theme.onSurfaceColor) },
                    leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onMove()
                    },
                    modifier = Modifier.testTag("menu_item_move_${item.name}")
                )

                DropdownMenuItem(
                    text = { Text("Rename", color = theme.onSurfaceColor) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onRename()
                    },
                    modifier = Modifier.testTag("menu_item_rename_${item.name}")
                )

                if (onExportZip != null) {
                    DropdownMenuItem(
                        text = { Text("Export as ZIP", color = theme.onSurfaceColor) },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            menuExpanded = false
                            onExportZip()
                        },
                        modifier = Modifier.testTag("menu_item_zip_${item.name}")
                    )
                }

                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF4444)) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                    modifier = Modifier.testTag("menu_item_delete_${item.name}")
                )
            }
        }
    }
}
