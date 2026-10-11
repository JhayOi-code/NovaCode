package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.git.model.GitHubAccount
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.SidebarTab
import com.example.model.TreeNode
import com.example.model.TreeNodeType
import kotlinx.coroutines.launch
import java.io.File

/**
 * Mobile Sidebar Drawer (File Explorer Panel) for code editor apps.
 *
 * LAYOUT:
 * - Slides over the main content (approx 70-75% screen width).
 * - Ultra-clean dark theme with near-black surface, subtle borders, high contrast.
 * - Top tab bar with 5 icons: Run, Files (default active with underline indicator), Code Search, Source Control (Git), Account.
 * - Header row under tabs: "FILES" section title with Search, Locate/Reveal current file, New File, New Folder, Overflow.
 *
 * FILE TREE:
 * - Collapsible folders with rotating chevrons and item action menu.
 * - Files indented under folder levels with dedicated language badges (.kt, .py, .js, .html, .css, etc.).
 * - Active file highlighted with prominent selection indicator and border.
 * - Tap file -> opens in editor & closes drawer.
 * - Tap folder -> smooth expands/collapses.
 *
 * FEATURES:
 * - Filter tree live by filename with inline search input.
 * - Locate/Reveal icon auto-expands parents and scrolls directly to the currently open file.
 * - Create file / folder, Rename, Delete, Duplicate, Share, and Move via "..." menus.
 * - Minimum 44-48dp touch targets for comfortable mobile interaction.
 */
@Composable
fun SidebarFileExplorerDrawer(
    theme: EditorTheme,
    currentDirectory: File,
    activeFile: CodeFile?,
    onOpenFile: (FileItem) -> Unit,
    onCloseDrawer: () -> Unit,
    // File modification actions
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onMove: (String, String) -> Unit,
    onShare: (FileItem) -> Unit,
    onRefresh: () -> Unit,
    // Top tab actions
    onRunActive: (() -> Unit)? = null,
    onOpenGitPanel: (() -> Unit)? = null,
    githubAccount: GitHubAccount? = null,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SidebarTab.FILES) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Persistent expansion state for directories: path -> isExpanded
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

    // Dialog state management
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<TreeNode?>(null) }
    var itemToDelete by remember { mutableStateOf<TreeNode?>(null) }
    var itemToMove by remember { mutableStateOf<TreeNode?>(null) }

    var inputName by remember { mutableStateOf("") }
    var destinationInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Near-black dark surface palette consistent with VS Code / IntelliJ mobile sidebar
    val drawerBg = Color(0xFF0D1117)
    val drawerSurface = Color(0xFF161B22)
    val borderCol = Color(0xFF30363D)
    val textPrimary = Color(0xFFE6EDF3)
    val textMuted = Color(0xFF8B949E)
    val accentCol = theme.primaryColor

    // Construct recursive tree structure from directory
    val rootNodes = remember(currentDirectory, searchQuery, expandedFolders.toMap()) {
        buildTreeNodeHierarchy(
            directory = currentDirectory,
            expandedFolders = expandedFolders,
            searchQuery = searchQuery.trim()
        )
    }

    // Flatten tree to a fast lazy list while preserving parent/child nesting levels
    val flattenedList = remember(rootNodes) {
        flattenTree(rootNodes)
    }

    // Auto-expand and scroll to current file when "Locate" is triggered
    fun locateCurrentFile() {
        val path = activeFile?.path ?: return
        var parent = File(path).parentFile
        while (parent != null && parent.absolutePath.startsWith(currentDirectory.absolutePath)) {
            expandedFolders[parent.absolutePath] = true
            parent = parent.parentFile
        }
        coroutineScope.launch {
            val idx = flattenedList.indexOfFirst { it.node.path == path }
            if (idx >= 0) {
                listState.animateScrollToItem(idx)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .testTag("sidebar_file_explorer_drawer"),
        color = drawerBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // 1. TOP TAB BAR (5 Icon Tabs with underline)
            // ==========================================
            SidebarTopTabBar(
                activeTab = activeTab,
                onTabSelect = { tab ->
                    activeTab = tab
                    if (tab == SidebarTab.GIT) {
                        onOpenGitPanel?.invoke()
                    }
                },
                accentColor = accentCol,
                surfaceColor = drawerSurface,
                borderColor = borderCol,
                textMuted = textMuted
            )

            // ==========================================
            // 2. TAB CONTENT
            // ==========================================
            when (activeTab) {
                SidebarTab.FILES -> {
                    // Header Row: "FILES" + quick action buttons
                    SidebarFilesHeader(
                        isSearchExpanded = isSearchExpanded,
                        onToggleSearch = { isSearchExpanded = !isSearchExpanded },
                        onLocateFile = { locateCurrentFile() },
                        onNewFileClick = {
                            inputName = ""
                            showNewFileDialog = true
                        },
                        onNewFolderClick = {
                            inputName = ""
                            showNewFolderDialog = true
                        },
                        onRefreshClick = onRefresh,
                        onCloseDrawer = onCloseDrawer,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentCol
                    )

                    // Optional Inline Search Bar
                    AnimatedVisibility(visible = isSearchExpanded) {
                        SidebarInlineSearch(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClear = { searchQuery = "" },
                            surfaceBg = drawerSurface,
                            borderColor = borderCol,
                            textColor = textPrimary,
                            placeholderColor = textMuted
                        )
                    }

                    HorizontalDivider(color = borderCol, thickness = 1.dp)

                    // Lazy Tree List
                    if (flattenedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = textMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\"" else "Workspace folder is empty",
                                    color = textMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("sidebar_tree_lazy_column")
                        ) {
                            items(
                                items = flattenedList,
                                key = { it.node.path }
                            ) { flatItem ->
                                val node = flatItem.node
                                val isSelected = activeFile?.path == node.path

                                RecursiveTreeNodeRow(
                                    node = node,
                                    indent = flatItem.depth,
                                    isSelected = isSelected,
                                    theme = theme,
                                    textColor = textPrimary,
                                    textMuted = textMuted,
                                    accentColor = accentCol,
                                    borderCol = borderCol,
                                    onClick = {
                                        if (node.isDirectory) {
                                            val current = expandedFolders[node.path] ?: false
                                            expandedFolders[node.path] = !current
                                        } else {
                                            val fileItem = FileItem(
                                                name = node.name,
                                                path = node.path,
                                                isDirectory = false,
                                                size = node.size,
                                                lastModified = node.lastModified
                                            )
                                            onOpenFile(fileItem)
                                            onCloseDrawer()
                                        }
                                    },
                                    onRename = {
                                        itemToRename = node
                                        inputName = node.name
                                    },
                                    onDelete = {
                                        itemToDelete = node
                                    },
                                    onDuplicate = {
                                        onDuplicate(node.path)
                                    },
                                    onMove = {
                                        itemToMove = node
                                        destinationInput = currentDirectory.absolutePath
                                    },
                                    onShare = {
                                        val fi = FileItem(
                                            name = node.name,
                                            path = node.path,
                                            isDirectory = node.isDirectory,
                                            size = node.size,
                                            lastModified = node.lastModified
                                        )
                                        onShare(fi)
                                    }
                                )
                            }
                        }
                    }
                }

                SidebarTab.RUN -> {
                    SidebarRunPanel(
                        activeFile = activeFile,
                        onRun = {
                            onRunActive?.invoke()
                            onCloseDrawer()
                        },
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentCol,
                        surfaceColor = drawerSurface
                    )
                }

                SidebarTab.SEARCH -> {
                    SidebarSearchPanel(
                        currentDirectory = currentDirectory,
                        onOpenFile = {
                            onOpenFile(it)
                            onCloseDrawer()
                        },
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentCol,
                        surfaceColor = drawerSurface,
                        borderCol = borderCol
                    )
                }

                SidebarTab.GIT -> {
                    SidebarGitPanel(
                        githubAccount = githubAccount,
                        onOpenFullGit = {
                            onOpenGitPanel?.invoke()
                            onCloseDrawer()
                        },
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentCol,
                        surfaceColor = drawerSurface
                    )
                }

                SidebarTab.ACCOUNT -> {
                    SidebarAccountPanel(
                        githubAccount = githubAccount,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        accentColor = accentCol,
                        surfaceColor = drawerSurface
                    )
                }
            }
        }
    }

    // ==========================================
    // 3. DIALOGS (New File, New Folder, Rename, Delete, Move)
    // ==========================================
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("File Name (e.g. Main.java, app.py)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sidebar_new_file")
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
                    colors = ButtonDefaults.buttonColors(containerColor = accentCol)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel", color = textMuted) }
            }
        )
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Folder", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sidebar_new_folder")
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
                    colors = ButtonDefaults.buttonColors(containerColor = accentCol)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel", color = textMuted) }
            }
        )
    }

    itemToRename?.let { node ->
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = { Text("Rename ${if (node.isDirectory) "Folder" else "File"}", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sidebar_rename")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onRename(node.path, inputName.trim())
                            itemToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentCol)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) { Text("Cancel", color = textMuted) }
            }
        )
    }

    itemToDelete?.let { node ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Confirmation", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${node.name}\"?\n${if (node.isDirectory) "All files inside will be permanently deleted." else ""}",
                    color = textPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(node.path)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel", color = textMuted) }
            }
        )
    }

    itemToMove?.let { node ->
        AlertDialog(
            onDismissRequest = { itemToMove = null },
            title = { Text("Move \"${node.name}\"", color = textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Target Destination Path:", color = textMuted, fontSize = 12.sp)
                    OutlinedTextField(
                        value = destinationInput,
                        onValueChange = { destinationInput = it },
                        label = { Text("Destination Directory") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sidebar_move")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (destinationInput.isNotBlank()) {
                            onMove(node.path, destinationInput.trim())
                            itemToMove = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentCol)
                ) {
                    Text("Move")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToMove = null }) { Text("Cancel", color = textMuted) }
            }
        )
    }
}

// ==========================================
// TOP TAB BAR
// ==========================================
@Composable
private fun SidebarTopTabBar(
    activeTab: SidebarTab,
    onTabSelect: (SidebarTab) -> Unit,
    accentColor: Color,
    surfaceColor: Color,
    borderColor: Color,
    textMuted: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Pair(SidebarTab.RUN, Icons.Default.PlayArrow),
                Pair(SidebarTab.FILES, Icons.Default.Folder),
                Pair(SidebarTab.SEARCH, Icons.Default.Search),
                Pair(SidebarTab.GIT, Icons.AutoMirrored.Filled.CallSplit),
                Pair(SidebarTab.ACCOUNT, Icons.Default.AccountCircle)
            )

            tabs.forEach { (tab, icon) ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelect(tab) }
                        .testTag("sidebar_tab_${tab.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) Color.White else textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // Active tab underline indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.dp)
                                .background(if (isSelected) Color.White else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// FILES HEADER ROW
// ==========================================
@Composable
private fun SidebarFilesHeader(
    isSearchExpanded: Boolean,
    onToggleSearch: () -> Unit,
    onLocateFile: () -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    textPrimary: Color,
    textMuted: Color,
    accentColor: Color
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "FILES",
            color = textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Search toggle
            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("sidebar_btn_search")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Files",
                    tint = if (isSearchExpanded) accentColor else textMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Locate / Reveal current file
            IconButton(
                onClick = onLocateFile,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("sidebar_btn_locate")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Locate Current File",
                    tint = textMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // New File
            IconButton(
                onClick = onNewFileClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("sidebar_btn_new_file")
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAdd,
                    contentDescription = "New File",
                    tint = textMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // New Folder
            IconButton(
                onClick = onNewFolderClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("sidebar_btn_new_folder")
            ) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = "New Folder",
                    tint = textMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Overflow "..." menu
            Box {
                IconButton(
                    onClick = { showOverflowMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sidebar_btn_overflow")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Actions",
                        tint = textMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Refresh Explorer") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            showOverflowMenu = false
                            onRefreshClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Close Sidebar") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            showOverflowMenu = false
                            onCloseDrawer()
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// INLINE SEARCH BAR
// ==========================================
@Composable
private fun SidebarInlineSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    surfaceBg: Color,
    borderColor: Color,
    textColor: Color,
    placeholderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .background(surfaceBg, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = placeholderColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(textColor),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text("Filter files by name...", color = placeholderColor, fontSize = 12.sp)
                }
                innerTextField()
            },
            modifier = Modifier
                .weight(1f)
                .testTag("sidebar_filter_input")
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = placeholderColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ==========================================
// RECURSIVE TREE NODE ROW
// ==========================================
@Composable
private fun RecursiveTreeNodeRow(
    node: TreeNode,
    indent: Int,
    isSelected: Boolean,
    theme: EditorTheme,
    textColor: Color,
    textMuted: Color,
    accentColor: Color,
    borderCol: Color,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (node.isExpanded) 90f else 0f,
        label = "chevron_anim"
    )

    // Indentation width based on depth level
    val indentPadding = (indent * 16 + 8).dp

    val rowBg = if (isSelected) accentColor.copy(alpha = 0.18f) else Color.Transparent
    val rowBorder = if (isSelected) accentColor else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp) // Touch target minimum
            .background(rowBg)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = rowBorder
            )
            .clickable(onClick = onClick)
            .padding(start = indentPadding, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.isDirectory) {
                // Animated chevron indicator
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (node.isExpanded) "Collapse" else "Expand",
                    tint = textMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }

            // Language / Folder Icon
            LanguageFileIcon(
                filename = node.name,
                isDirectory = node.isDirectory,
                isExpanded = node.isExpanded,
                size = 18.dp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // File or Folder Name
            Text(
                text = node.name,
                color = if (isSelected) Color.White else textColor,
                fontSize = 13.sp,
                fontWeight = if (node.isDirectory || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action menu "..." button on right
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("sidebar_node_menu_${node.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        menuExpanded = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        menuExpanded = false
                        onDuplicate()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Move") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        menuExpanded = false
                        onMove()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Share") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        menuExpanded = false
                        onShare()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF4444)) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

// ==========================================
// SUB-PANELS (Run, Search, Git, Account)
// ==========================================
@Composable
private fun SidebarRunPanel(
    activeFile: CodeFile?,
    onRun: () -> Unit,
    textPrimary: Color,
    textMuted: Color,
    accentColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("RUN & EXECUTE", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        if (activeFile != null) {
            Surface(
                color = surfaceColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Target Active File:", color = textMuted, fontSize = 11.sp)
                    Text(activeFile.name, color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRun,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run in Terminal")
                    }
                }
            }
        } else {
            Text("No file currently open in editor.", color = textMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SidebarSearchPanel(
    currentDirectory: File,
    onOpenFile: (FileItem) -> Unit,
    textPrimary: Color,
    textMuted: Color,
    accentColor: Color,
    surfaceColor: Color,
    borderCol: Color
) {
    var query by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf<List<FileItem>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CODE & FILE SEARCH", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = query,
            onValueChange = { q ->
                query = q
                matches = if (q.isBlank()) {
                    emptyList()
                } else {
                    currentDirectory.walkTopDown()
                        .filter { it.isFile && it.name.contains(q, ignoreCase = true) }
                        .take(30)
                        .map { FileItem(name = it.name, path = it.absolutePath, isDirectory = false, size = it.length()) }
                        .toList()
                }
            },
            label = { Text("Search files in workspace...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(matches) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFile(item) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageFileIcon(filename = item.name, isDirectory = false, size = 16.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.name, color = textPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun SidebarGitPanel(
    githubAccount: GitHubAccount?,
    onOpenFullGit: () -> Unit,
    textPrimary: Color,
    textMuted: Color,
    accentColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("SOURCE CONTROL", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (githubAccount?.isLinked == true) "Connected: ${githubAccount.username}" else "Local Git Workspace",
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Manage branches, commits, staging, push, pull and clone remote repositories.", color = textMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onOpenFullGit,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Git Panel")
                }
            }
        }
    }
}

@Composable
private fun SidebarAccountPanel(
    githubAccount: GitHubAccount?,
    textPrimary: Color,
    textMuted: Color,
    accentColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("ACCOUNT & PROFILE", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(36.dp))
                Text(
                    text = githubAccount?.username?.takeIf { it.isNotBlank() } ?: "Linq Studio User",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = githubAccount?.email?.takeIf { it.isNotBlank() } ?: "Standalone Mobile IDE",
                    color = textMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==========================================
// TREE DATA HELPERS
// ==========================================
private data class FlatTreeItem(
    val node: TreeNode,
    val depth: Int
)

/**
 * Builds the hierarchical TreeNode model recursively from local directory,
 * pruning or filtering based on the search query.
 */
private fun buildTreeNodeHierarchy(
    directory: File,
    expandedFolders: Map<String, Boolean>,
    searchQuery: String,
    level: Int = 0
): List<TreeNode> {
    val cacheKey = "${directory.absolutePath}_${expandedFolders.hashCode()}_${searchQuery}_$level"
    val cached = com.example.model.TreeCacheManager.getCachedNodes(cacheKey)
    if (cached != null) return cached

    val files = directory.listFiles() ?: return emptyList()

    // Directories first, then alphabetical
    val sorted = files.sortedWith(
        compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
    )

    val results = mutableListOf<TreeNode>()

    for (file in sorted) {
        if (file.name.startsWith(".") && file.name != ".env") continue // skip hidden files

        val isDir = file.isDirectory
        val isExpanded = expandedFolders[file.absolutePath] ?: false

        if (isDir) {
            val children = if (isExpanded || searchQuery.isNotBlank()) {
                buildTreeNodeHierarchy(file, expandedFolders, searchQuery, level + 1)
            } else {
                emptyList()
            }

            val matchesSearch = searchQuery.isBlank() ||
                    file.name.contains(searchQuery, ignoreCase = true) ||
                    children.isNotEmpty()

            if (matchesSearch) {
                results.add(
                    TreeNode(
                        id = file.absolutePath,
                        name = file.name,
                        type = TreeNodeType.FOLDER,
                        path = file.absolutePath,
                        isExpanded = isExpanded || (searchQuery.isNotBlank() && children.isNotEmpty()),
                        children = children,
                        level = level
                    )
                )
            }
        } else {
            val matchesSearch = searchQuery.isBlank() || file.name.contains(searchQuery, ignoreCase = true)
            if (matchesSearch) {
                results.add(
                    TreeNode(
                        id = file.absolutePath,
                        name = file.name,
                        type = TreeNodeType.FILE,
                        path = file.absolutePath,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        level = level
                    )
                )
            }
        }
    }

    com.example.model.TreeCacheManager.putCachedNodes(cacheKey, results)
    return results
}

/**
 * Flattens recursive tree nodes into an indexed linear list for smooth 60fps LazyColumn rendering.
 */
private fun flattenTree(nodes: List<TreeNode>, depth: Int = 0): List<FlatTreeItem> {
    val list = mutableListOf<FlatTreeItem>()
    for (node in nodes) {
        list.add(FlatTreeItem(node, depth))
        if (node.isDirectory && node.isExpanded && node.children.isNotEmpty()) {
            list.addAll(flattenTree(node.children, depth + 1))
        }
    }
    return list
}
