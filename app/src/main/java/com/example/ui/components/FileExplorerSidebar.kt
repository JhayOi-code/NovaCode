package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontStyle
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
import com.example.model.ProjectTemplateType

@Composable
fun FileExplorerSidebar(
    theme: EditorTheme,
    files: List<CodeFile>,
    gitStatus: GitRepositoryStatus,
    trackedFolders: List<String> = emptyList(),
    recentFiles: List<CodeFile> = emptyList(),
    onOpenFile: (CodeFile) -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit = {},
    onNewFolderWithTemplate: (String, ProjectTemplateType) -> Unit = { name, _ -> onNewFolder(name) },
    onNewFileInFolder: (String?, String) -> Unit = { _, name -> onNewFile(name) },
    onDeleteFolder: (String) -> Unit = {},
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
    var targetFolderForNewFile by remember { mutableStateOf<String?>(null) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var selectedProjectTemplate by remember { mutableStateOf(ProjectTemplateType.BLANK_FOLDER) }
    var newFolderNameInput by remember { mutableStateOf("my_folder") }
    var isRecentFilesExpanded by remember { mutableStateOf(true) }

    // Derive all folders and group files accurately, supporting empty real folders
    val allDiscoveredFolders = remember(files, trackedFolders) {
        val folders = linkedSetOf<String>()
        folders.addAll(trackedFolders)
        files.forEach { file ->
            val cleanPath = file.name.trim().removePrefix("/")
            if (cleanPath.contains('/')) {
                folders.add(cleanPath.substringBeforeLast('/'))
            }
        }
        folders.toList()
    }

    var expandedFolders by remember(files, trackedFolders) {
        mutableStateOf(allDiscoveredFolders.toSet())
    }

    val filteredFiles = remember(files, filterText) {
        if (filterText.isBlank()) files
        else files.filter { it.name.contains(filterText, ignoreCase = true) }
    }

    // Group filtered files into Folders and Root
    val folderFileMap = remember(filteredFiles, allDiscoveredFolders) {
        val map = mutableMapOf<String, MutableList<CodeFile>>()
        allDiscoveredFolders.forEach { map[it] = mutableListOf() }
        filteredFiles.forEach { file ->
            val clean = file.name.trim().removePrefix("/")
            if (clean.contains('/')) {
                val folder = clean.substringBeforeLast('/')
                map.getOrPut(folder) { mutableListOf() }.add(file)
            }
        }
        map
    }

    val rootFiles = remember(filteredFiles) {
        filteredFiles.filter { !it.name.trim().removePrefix("/").contains('/') }
    }

    Surface(
        modifier = modifier
            .width(285.dp)
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
                                text = "Explorer",
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
                                targetFolderForNewFile = null
                                newFileNameInput = ""
                                showNewFileDialog = true
                            },
                            modifier = Modifier.size(30.dp).testTag("explorer_new_file_button")
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = theme.primaryColor, modifier = Modifier.size(17.dp))
                        }
                        IconButton(
                            onClick = {
                                selectedProjectTemplate = ProjectTemplateType.BLANK_FOLDER
                                newFolderNameInput = "my_folder"
                                showNewFolderDialog = true
                            },
                            modifier = Modifier.size(30.dp).testTag("explorer_new_folder_button")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder or Project", tint = theme.primaryColor, modifier = Modifier.size(17.dp))
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
                // File Explorer view with real tree representation
                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 6.dp)) {
                    // Filter input
                    OutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        placeholder = {
                            Text(
                                text = "Filter workspace files...",
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
                        // RECENT FILES SECTION (Last 5 files for quick navigation)
                        val recentFilesList = recentFiles.ifEmpty { files }.take(5)
                        if (recentFilesList.isNotEmpty() && filterText.isBlank()) {
                            item(key = "section_recent_files_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.primaryColor.copy(alpha = 0.12f))
                                        .clickable { isRecentFilesExpanded = !isRecentFilesExpanded }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("explorer_recent_files_header"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isRecentFilesExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recent Files",
                                        color = theme.onSurfaceColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = theme.primaryColor.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "${recentFilesList.size}",
                                            color = theme.primaryColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            if (isRecentFilesExpanded) {
                                items(recentFilesList, key = { "recent_${it.name}" }) { recentFile ->
                                    val ext = recentFile.name.substringAfterLast('.', "").uppercase()
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 14.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(theme.surfaceVariant.copy(alpha = 0.35f))
                                            .clickable { onOpenFile(recentFile) }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                            .testTag("explorer_recent_file_${recentFile.name}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.primaryColor.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = ext.take(3),
                                                    color = theme.primaryColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = recentFile.name.substringAfterLast('/'),
                                                color = theme.onSurfaceColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = "${recentFile.content.lines().size} lines",
                                            color = theme.lineNumberColor,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                item(key = "recent_files_divider") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }

                        // All Folders (with collapsible hierarchy and quick + File)
                        allDiscoveredFolders.forEach { folderName ->
                            val filesInFolder = folderFileMap[folderName] ?: emptyList()
                            val isExpanded = expandedFolders.contains(folderName)

                            item(key = "folder_$folderName") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.surfaceVariant.copy(alpha = 0.45f))
                                        .clickable {
                                            expandedFolders = if (isExpanded) {
                                                expandedFolders - folderName
                                            } else {
                                                expandedFolders + folderName
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 5.dp)
                                        .testTag("folder_row_$folderName"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = theme.lineNumberColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = folderName,
                                        color = theme.onSurfaceColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${filesInFolder.size}",
                                        color = theme.lineNumberColor,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .background(theme.editorBackground, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // Quick button to add file directly inside this folder
                                    IconButton(
                                        onClick = {
                                            targetFolderForNewFile = folderName
                                            newFileNameInput = ""
                                            showNewFileDialog = true
                                        },
                                        modifier = Modifier.size(24.dp).testTag("add_file_to_$folderName")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add file in $folderName",
                                            tint = theme.primaryColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Quick button to delete empty/custom folder
                                    IconButton(
                                        onClick = { onDeleteFolder(folderName) },
                                        modifier = Modifier.size(24.dp).testTag("delete_folder_$folderName")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete $folderName",
                                            tint = theme.lineNumberColor.copy(alpha = 0.6f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            if (isExpanded) {
                                if (filesInFolder.isEmpty()) {
                                    item(key = "empty_folder_$folderName") {
                                        Text(
                                            text = "Empty folder • Tap [+] to add files",
                                            color = theme.lineNumberColor.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            fontStyle = FontStyle.Italic,
                                            modifier = Modifier.padding(start = 28.dp, top = 2.dp, bottom = 4.dp)
                                        )
                                    }
                                } else {
                                    items(filesInFolder, key = { it.path }) { file ->
                                    val simpleName = file.name.substringAfterLast('/')
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 20.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onOpenFile(file) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .testTag("file_item_${file.name}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = getFileIconColor(simpleName, theme.primaryColor),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = simpleName,
                                            color = theme.onSurfaceColor,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (file.isModified) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .background(Color(0xFFF59E0B), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                        // Root Files Section
                        if (rootFiles.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp, start = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "WORKSPACE ROOT",
                                        color = theme.lineNumberColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            items(rootFiles, key = { it.path }) { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onOpenFile(file) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("file_item_${file.name}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = getFileIconColor(file.name, theme.primaryColor),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = file.name,
                                        color = theme.onSurfaceColor,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (file.isModified) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(Color(0xFFF59E0B), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ACCURATE CREATE FOLDER OR PROJECT DIALOG
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
                        text = "Select project template or blank folder:",
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
                                modifier = Modifier
                                    .clickable {
                                        selectedProjectTemplate = template
                                        newFolderNameInput = when (template) {
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
                        value = newFolderNameInput,
                        onValueChange = { newFolderNameInput = it },
                        placeholder = { Text("e.g. components, python_app, web_site", color = theme.lineNumberColor, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp).testTag("create_folder_name_input"),
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
                            onNewFolderWithTemplate(trimmed, selectedProjectTemplate)
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

    // ACCURATE CREATE NEW FILE DIALOG (With folder destination selector)
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = theme.primaryColor)
                    Text("Create New File", color = theme.onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Folder destination chips
                    Text(
                        text = "Destination Folder:",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isRootSelected = targetFolderForNewFile == null
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isRootSelected) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceVariant,
                            border = if (isRootSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor) else null,
                            modifier = Modifier.clickable { targetFolderForNewFile = null }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isRootSelected) theme.primaryColor else theme.onSurfaceColor
                                )
                                Text(
                                    text = "Root (/)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isRootSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isRootSelected) theme.primaryColor else theme.onSurfaceColor
                                )
                            }
                        }

                        allDiscoveredFolders.forEach { folder ->
                            val isSelected = targetFolderForNewFile == folder
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor) else null,
                                modifier = Modifier.clickable { targetFolderForNewFile = folder }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isSelected) theme.primaryColor else theme.onSurfaceColor
                                    )
                                    Text(
                                        text = folder,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) theme.primaryColor else theme.onSurfaceColor
                                    )
                                }
                            }
                        }
                    }

                    // File Name Input
                    Text(
                        text = "File Name (e.g. script.py, Component.java, style.css):",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it },
                        placeholder = { Text("Example.java", color = theme.lineNumberColor, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp).testTag("create_file_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.onSurfaceColor,
                            unfocusedTextColor = theme.onSurfaceColor,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Quick Extensions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(".py", ".java", ".js", ".html", ".css", ".kt", ".json", ".md").forEach { ext ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = theme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    val base = if (newFileNameInput.contains('.')) newFileNameInput.substringBeforeLast('.') else newFileNameInput.ifBlank { "script" }
                                    newFileNameInput = "$base$ext"
                                }
                            ) {
                                Text(
                                    text = ext,
                                    color = theme.primaryColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newFileNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onNewFileInFolder(targetFolderForNewFile, trimmed)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_file_button")
                ) {
                    Text("Create File", fontWeight = FontWeight.Bold)
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
}

private fun getFileIconColor(name: String, defaultPrimary: Color): Color {
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "java" -> Color(0xFFEF4444)
        "kt" -> Color(0xFFA78BFA)
        "py" -> Color(0xFF3B82F6)
        "js", "ts" -> Color(0xFFFBBF24)
        "html", "htm" -> Color(0xFFF97316)
        "css" -> Color(0xFF38BDF8)
        "json" -> Color(0xFF34D399)
        "md" -> Color(0xFF94A3B8)
        else -> defaultPrimary
    }
}
