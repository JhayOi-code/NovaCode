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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.GitCommit
import com.example.model.GitFileItem
import com.example.model.GitFileStatus
import com.example.model.GitStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitSidePanel(
    theme: EditorTheme,
    gitStatus: GitStatus,
    onStageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageFile: (String) -> Unit,
    onUnstageAll: () -> Unit,
    onCommit: (String) -> Unit,
    onPush: () -> Unit,
    onCreateBranch: (String) -> Unit,
    onSwitchBranch: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var showBranchDialog by remember { mutableStateOf(false) }
    var newBranchName by remember { mutableStateOf("") }
    var showBranchesMenu by remember { mutableStateOf(false) }
    var showCommitHistory by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.surfaceColor)
            .testTag("git_side_panel")
    ) {
        // TOP HEADER: Source Control & Current Branch
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.editorBackground.copy(alpha = 0.5f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Source Control",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SOURCE CONTROL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(22.dp).testTag("btn_close_git_panel")
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Close Git Panel",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Branch Selector / Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.primaryColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showBranchesMenu = true }
                        .testTag("btn_git_branch_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = gitStatus.branch,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.primaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showBranchesMenu,
                        onDismissRequest = { showBranchesMenu = false }
                    ) {
                        gitStatus.branches.forEach { b ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (b == gitStatus.branch) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = theme.primaryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(b, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    onSwitchBranch(b)
                                    showBranchesMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                                    Text("New Branch...", fontSize = 12.sp, color = theme.primaryColor)
                                }
                            },
                            onClick = {
                                showBranchesMenu = false
                                newBranchName = ""
                                showBranchDialog = true
                            }
                        )
                    }
                }

                // Push Button
                IconButton(
                    onClick = onPush,
                    modifier = Modifier
                        .size(32.dp)
                        .background(theme.primaryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .testTag("btn_git_push")
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = "Push",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (!gitStatus.lastPushStatus.isNullOrEmpty()) {
                Text(
                    text = gitStatus.lastPushStatus,
                    fontSize = 10.sp,
                    color = Color(0xFF10B981),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(color = theme.borderColor)

        // COMMIT MESSAGE BOX & COMMIT BUTTON
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = commitMessage,
                onValueChange = { commitMessage = it },
                placeholder = { Text("Commit message (e.g. Update UI)", fontSize = 11.sp, color = theme.lineNumberColor) },
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = theme.onSurfaceColor),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primaryColor,
                    unfocusedBorderColor = theme.borderColor,
                    focusedContainerColor = theme.editorBackground,
                    unfocusedContainerColor = theme.editorBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_git_commit_message")
            )

            Button(
                onClick = {
                    if (commitMessage.isNotBlank()) {
                        onCommit(commitMessage)
                        commitMessage = ""
                    }
                },
                enabled = commitMessage.isNotBlank() && gitStatus.stagedFiles.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .testTag("btn_git_commit")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Commit to ${gitStatus.branch} (${gitStatus.stagedFiles.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(color = theme.borderColor)

        // SCROLLABLE FILE LIST & COMMIT HISTORY
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // STAGED CHANGES SECTION
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "STAGED CHANGES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "(${gitStatus.stagedFiles.size})",
                            fontSize = 10.sp,
                            color = theme.lineNumberColor
                        )
                    }

                    if (gitStatus.stagedFiles.isNotEmpty()) {
                        Text(
                            text = "Unstage All",
                            fontSize = 10.sp,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { onUnstageAll() }
                                .padding(2.dp)
                                .testTag("btn_git_unstage_all")
                        )
                    }
                }
            }

            if (gitStatus.stagedFiles.isEmpty()) {
                item {
                    Text(
                        text = "No staged changes",
                        fontSize = 10.sp,
                        color = theme.lineNumberColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            } else {
                items(gitStatus.stagedFiles) { item ->
                    GitFileRow(
                        theme = theme,
                        item = item,
                        isStaged = true,
                        onAction = { onUnstageFile(item.relativePath) }
                    )
                }
            }

            // UNSTAGED CHANGES SECTION
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CHANGES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                        Text(
                            text = "(${gitStatus.unstagedFiles.size})",
                            fontSize = 10.sp,
                            color = theme.lineNumberColor
                        )
                    }

                    if (gitStatus.unstagedFiles.isNotEmpty()) {
                        Text(
                            text = "Stage All",
                            fontSize = 10.sp,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { onStageAll() }
                                .padding(2.dp)
                                .testTag("btn_git_stage_all")
                        )
                    }
                }
            }

            if (gitStatus.unstagedFiles.isEmpty()) {
                item {
                    Text(
                        text = "Working tree clean",
                        fontSize = 10.sp,
                        color = theme.lineNumberColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            } else {
                items(gitStatus.unstagedFiles) { item ->
                    GitFileRow(
                        theme = theme,
                        item = item,
                        isStaged = false,
                        onAction = { onStageFile(item.relativePath) }
                    )
                }
            }

            // COMMIT HISTORY SECTION (COLLAPSIBLE)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = theme.borderColor.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCommitHistory = !showCommitHistory }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "COMMIT LOG (${gitStatus.commits.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor
                        )
                    }
                    Icon(
                        imageVector = if (showCommitHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (showCommitHistory) {
                items(gitStatus.commits) { commit ->
                    CommitHistoryItem(theme = theme, commit = commit, dateFormat = dateFormat)
                }
            }
        }
    }

    // CREATE BRANCH DIALOG
    if (showBranchDialog) {
        AlertDialog(
            onDismissRequest = { showBranchDialog = false },
            title = { Text("Create New Branch", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a branch name:", fontSize = 12.sp, color = theme.onSurfaceColor)
                    OutlinedTextField(
                        value = newBranchName,
                        onValueChange = { newBranchName = it },
                        placeholder = { Text("e.g. feature-awesome-ui", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_branch_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBranchName.isNotBlank()) {
                            onCreateBranch(newBranchName)
                            showBranchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                ) {
                    Text("Create & Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBranchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GitFileRow(
    theme: EditorTheme,
    item: GitFileItem,
    isStaged: Boolean,
    onAction: () -> Unit
) {
    val (statusLabel, statusColor) = when (item.status) {
        GitFileStatus.STAGED -> Pair("S", Color(0xFF10B981))
        GitFileStatus.MODIFIED -> Pair("M", Color(0xFFF59E0B))
        GitFileStatus.UNTRACKED -> Pair("U", Color(0xFF38BDF8))
        GitFileStatus.DELETED -> Pair("D", Color(0xFFEF4444))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Text(
                text = item.relativePath,
                fontSize = 11.sp,
                color = theme.onSurfaceColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onAction,
            modifier = Modifier
                .size(22.dp)
                .testTag(if (isStaged) "btn_unstage_${item.relativePath}" else "btn_stage_${item.relativePath}")
        ) {
            Icon(
                imageVector = if (isStaged) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = if (isStaged) "Unstage" else "Stage",
                tint = if (isStaged) Color(0xFFEF4444) else Color(0xFF10B981),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CommitHistoryItem(
    theme: EditorTheme,
    commit: GitCommit,
    dateFormat: SimpleDateFormat
) {
    Surface(
        color = theme.editorBackground.copy(alpha = 0.4f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = commit.id,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryColor
                )
                Text(
                    text = dateFormat.format(Date(commit.timestamp)),
                    fontSize = 9.sp,
                    color = theme.lineNumberColor
                )
            }
            Text(
                text = commit.message,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = theme.onSurfaceColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(10.dp))
                Text(
                    text = "${commit.author} • ${commit.branch} (${commit.fileCount} files)",
                    fontSize = 9.sp,
                    color = theme.lineNumberColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
