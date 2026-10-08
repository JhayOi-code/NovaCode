package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import com.example.terminal.ShellSession
import java.io.File

@Composable
fun TerminalScreen(
    theme: EditorTheme,
    sessions: List<ShellSession>,
    activeSessionId: String,
    onSelectSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onCloseSession: (String) -> Unit,
    onSendCommand: (String) -> Unit,
    onCtrlC: () -> Unit,
    onClearTerminal: () -> Unit,
    installedPackages: List<String>,
    onUninstallPackage: (String) -> Unit,
    onOpenFileInEditor: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFileManagerOpen by remember { mutableStateOf(false) }
    var inputCommand by remember { mutableStateOf("") }
    val currentSession = sessions.firstOrNull { it.id == activeSessionId } ?: sessions.firstOrNull()

    val listState = rememberLazyListState()

    LaunchedEffect(currentSession?.lines?.size) {
        if (!currentSession?.lines.isNullOrEmpty()) {
            listState.animateScrollToItem(currentSession!!.lines.lastIndex)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("terminal_screen"),
        color = theme.editorBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab row for multiple terminal sessions + Session control buttons
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                color = theme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Session Tabs
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sessions.forEach { sess ->
                            val isSelected = sess.id == activeSessionId
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(if (isSelected) theme.primaryColor.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { onSelectSession(sess.id) }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = sess.name,
                                    color = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (sessions.size > 1) {
                                    IconButton(
                                        onClick = { onCloseSession(sess.id) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close session",
                                            tint = theme.lineNumberColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add new session tab
                        IconButton(
                            onClick = onNewSession,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_new_terminal_session")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Session", tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Side controls: Toggle File Manager drawer, Ctrl+C, Clear
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        // Ctrl+C kill button
                        Button(
                            onClick = onCtrlC,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("btn_ctrl_c")
                        ) {
                            Text("Ctrl+C", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        // Toggle File Manager Drawer
                        IconButton(
                            onClick = { isFileManagerOpen = !isFileManagerOpen },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_toggle_terminal_files")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Terminal Files",
                                tint = if (isFileManagerOpen) theme.primaryColor else theme.onSurfaceColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Main Content: Terminal Screen + Optional Split File Manager Panel
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Terminal Console Area
                Column(
                    modifier = Modifier
                        .weight(if (isFileManagerOpen) 0.55f else 1f)
                        .fillMaxHeight()
                        .background(Color(0xFF070B12))
                ) {
                    // Terminal Output Lines
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp)
                            .testTag("terminal_output_list"),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        item {
                            Text(
                                text = "Linq Studio Shell (Android POSIX sandbox)\nType 'pkg list', 'help', or shell commands. Packages install into app /usr.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        items(currentSession?.lines ?: emptyList()) { line ->
                            TerminalOutputRow(line = line, theme = theme)
                        }
                    }

                    // Terminal Quick Command Bar (Copy, Paste, History, Quick Chips)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column {
                            // Quick action toolbar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Paste clipboard
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                        if (!clip.isNullOrEmpty()) {
                                            inputCommand += clip
                                        }
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                                }

                                // Quick commands
                                listOf("ls -la", "pwd", "pkg list", "pkg update", "clear", "git status").forEach { qCmd ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            if (qCmd == "clear") onClearTerminal()
                                            else onSendCommand(qCmd)
                                        },
                                        label = { Text(qCmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color(0xFF1E293B),
                                            labelColor = Color(0xFFE2E8F0)
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            // Command input line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${currentSession?.workingDir?.name ?: "sh"}$",
                                    color = theme.primaryColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                BasicTextField(
                                    value = inputCommand,
                                    onValueChange = { inputCommand = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("terminal_command_input"),
                                    textStyle = TextStyle(
                                        color = Color(0xFFF8FAFC),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(theme.primaryColor),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (inputCommand.isNotBlank()) {
                                                val cmd = inputCommand
                                                inputCommand = ""
                                                if (cmd.trim() == "clear") {
                                                    onClearTerminal()
                                                } else {
                                                    onSendCommand(cmd)
                                                }
                                            }
                                        }
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (inputCommand.isNotBlank()) {
                                            val cmd = inputCommand
                                            inputCommand = ""
                                            if (cmd.trim() == "clear") {
                                                onClearTerminal()
                                            } else {
                                                onSendCommand(cmd)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("btn_send_terminal")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Run",
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Split File Manager Panel inside Terminal
                if (isFileManagerOpen && currentSession != null) {
                    Surface(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .border(1.dp, theme.borderColor),
                        color = theme.surfaceColor
                    ) {
                        TerminalFileManagerPanel(
                            theme = theme,
                            workingDir = currentSession.workingDir,
                            installedPackages = installedPackages,
                            onUninstallPackage = onUninstallPackage,
                            onOpenFileInEditor = onOpenFileInEditor,
                            onSendCommand = onSendCommand,
                            onClose = { isFileManagerOpen = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalOutputRow(line: TerminalLine, theme: EditorTheme) {
    val context = LocalContext.current
    val color = when (line.type) {
        TerminalLineType.COMMAND -> theme.primaryColor
        TerminalLineType.STDOUT -> Color(0xFFE2E8F0)
        TerminalLineType.STDERR -> Color(0xFFF87171)
        TerminalLineType.INFO -> Color(0xFF38BDF8)
        TerminalLineType.SUCCESS -> Color(0xFF4ADE80)
        TerminalLineType.SYSTEM -> Color(0xFFA78BFA)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Line", line.text))
                Toast.makeText(context, "Copied line", Toast.LENGTH_SHORT).show()
            }
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = line.text,
            color = color,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun TerminalFileManagerPanel(
    theme: EditorTheme,
    workingDir: File,
    installedPackages: List<String>,
    onUninstallPackage: (String) -> Unit,
    onOpenFileInEditor: (FileItem) -> Unit,
    onSendCommand: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Files, 1: Installed Packages
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("name") } // name, size, date
    var refreshKey by remember { mutableStateOf(0) }

    // Dialog state
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<File?>(null) }
    var itemToDelete by remember { mutableStateOf<File?>(null) }
    var inputName by remember { mutableStateOf("") }

    val rawFiles = remember(workingDir, refreshKey) {
        workingDir.listFiles()?.toList() ?: emptyList()
    }

    val sortedFiles = remember(rawFiles, sortBy, searchQuery) {
        var list = rawFiles
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        when (sortBy) {
            "size" -> list.sortedWith(compareBy({ !it.isDirectory }, { it.length() }))
            "date" -> list.sortedWith(compareBy({ !it.isDirectory }, { it.lastModified() }))
            else -> list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab switcher: Files vs Installed
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = theme.gutterBackground,
            contentColor = theme.primaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = theme.primaryColor
                )
            },
            modifier = Modifier.height(36.dp)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Files (${sortedFiles.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Installed (${installedPackages.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        if (activeTab == 0) {
            // Files Navigation Header synced with cd
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(
                            onClick = {
                                workingDir.parentFile?.let { parent ->
                                    onSendCommand("cd \"${parent.absolutePath}\"")
                                }
                            },
                            enabled = workingDir.parentFile != null,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up", modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(workingDir.name.ifEmpty { "/" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.onSurfaceColor)
                            Text(workingDir.absolutePath, fontSize = 9.sp, color = theme.lineNumberColor, maxLines = 1)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { refreshKey++ }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(15.dp))
                        }
                        IconButton(onClick = { inputName = ""; showNewFileDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                        }
                        IconButton(onClick = { inputName = ""; showNewFolderDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                // Search & Sort bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(42.dp),
                        placeholder = { Text("Search...", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 11.sp, color = theme.onSurfaceColor)
                    )

                    var showSortMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Sort by Name") }, onClick = { sortBy = "name"; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Sort by Size") }, onClick = { sortBy = "size"; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Sort by Date") }, onClick = { sortBy = "date"; showSortMenu = false })
                        }
                    }
                }
            }

            // File items
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (sortedFiles.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No files found", color = theme.lineNumberColor, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(sortedFiles) { file ->
                        TerminalFileItemRow(
                            file = file,
                            theme = theme,
                            onOpen = {
                                if (file.isDirectory) {
                                    onSendCommand("cd \"${file.absolutePath}\"")
                                } else {
                                    val item = FileItem(
                                        name = file.name,
                                        path = file.absolutePath,
                                        isDirectory = false,
                                        size = file.length(),
                                        lastModified = file.lastModified()
                                    )
                                    onOpenFileInEditor(item)
                                }
                            },
                            onRename = { itemToRename = file; inputName = file.name },
                            onDelete = { itemToDelete = file },
                            onChmod = {
                                onSendCommand("chmod +x \"${file.absolutePath}\"")
                                Toast.makeText(context, "Added executable permission (+x)", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, file.readText())
                                }
                                context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
                            }
                        )
                    }
                }
            }
        } else {
            // Installed Packages Section
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Text(
                    text = "Packages installed in app's /usr directory:",
                    fontSize = 11.sp,
                    color = theme.lineNumberColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(installedPackages) { pkg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = theme.cardBackgroundColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                    Column {
                                        Text(pkg, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.onSurfaceColor)
                                        Text("usr/bin/$pkg", fontSize = 10.sp, color = theme.lineNumberColor, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Button(
                                    onClick = { onUninstallPackage(pkg) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                                        contentColor = Color(0xFFEF4444)
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Uninstall", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: New File
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File") },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("File Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (inputName.isNotBlank()) {
                        File(workingDir, inputName.trim()).createNewFile()
                        refreshKey++
                        showNewFileDialog = false
                    }
                }) { Text("Create") }
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
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (inputName.isNotBlank()) {
                        File(workingDir, inputName.trim()).mkdirs()
                        refreshKey++
                        showNewFolderDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Rename
    itemToRename?.let { file ->
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = { Text("Rename ${file.name}") },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (inputName.isNotBlank()) {
                        file.renameTo(File(file.parentFile, inputName.trim()))
                        refreshKey++
                        itemToRename = null
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Delete
    itemToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete ${file.name}?") },
            text = { Text("Are you sure you want to delete this file?") },
            confirmButton = {
                Button(
                    onClick = {
                        file.deleteRecursively()
                        refreshKey++
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TerminalFileItemRow(
    file: File,
    theme: EditorTheme,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onChmod: () -> Unit,
    onShare: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Code,
                contentDescription = null,
                tint = if (file.isDirectory) Color(0xFFFBBF24) else theme.primaryColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(file.name, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = theme.onSurfaceColor, maxLines = 1)
                Text(
                    text = if (file.isDirectory) "dir" else "${file.length()} B",
                    fontSize = 9.5.sp,
                    color = theme.lineNumberColor
                )
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions", modifier = Modifier.size(14.dp))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Edit / Open") }, onClick = { showMenu = false; onOpen() })
                DropdownMenuItem(text = { Text("Rename") }, onClick = { showMenu = false; onRename() })
                DropdownMenuItem(text = { Text("Make Executable (chmod +x)") }, onClick = { showMenu = false; onChmod() })
                DropdownMenuItem(text = { Text("Share") }, onClick = { showMenu = false; onShare() })
                DropdownMenuItem(text = { Text("Delete", color = Color(0xFFEF4444)) }, onClick = { showMenu = false; onDelete() })
            }
        }
    }
}
