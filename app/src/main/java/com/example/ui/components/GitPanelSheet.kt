package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.git.model.GitCommit
import com.example.git.model.GitHubAccount
import com.example.git.model.GitStatus
import com.example.model.EditorTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitPanelSheet(
    theme: EditorTheme,
    isOpen: Boolean,
    onClose: () -> Unit,
    githubAccount: GitHubAccount,
    gitStatus: GitStatus?,
    gitCurrentBranch: String,
    gitRemoteUrl: String,
    gitLogs: List<GitCommit>,
    isLoading: Boolean,
    currentDirectory: File,
    onLinkGitHub: (username: String, token: String, email: String) -> Unit,
    onUnlinkGitHub: () -> Unit,
    onInitRepo: () -> Unit,
    onSetRemote: (url: String) -> Unit,
    onPull: () -> Unit,
    onPush: () -> Unit,
    onCommit: (message: String) -> Unit,
    onClone: (repoUrl: String) -> Unit,
    onRefreshStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showLinkDialog by remember { mutableStateOf(false) }
    var showEditRemoteDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }

    var commitMessage by remember { mutableStateOf("") }
    var newRemoteUrl by remember(gitRemoteUrl) { mutableStateOf(gitRemoteUrl) }
    var cloneRepoUrl by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = theme.surfaceColor,
        contentColor = theme.onSurfaceColor,
        sheetState = sheetState,
        modifier = modifier.testTag("git_panel_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallSplit,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Git & GitHub VCS",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (gitStatus != null) "Branch: $gitCurrentBranch • ${currentDirectory.name}" else "No active repository",
                            color = theme.lineNumberColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshStatus,
                        enabled = !isLoading,
                        modifier = Modifier.testTag("btn_git_refresh")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = theme.primaryColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Status",
                                tint = theme.onSurfaceColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_git_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = theme.borderColor.copy(alpha = 0.6f))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- 1. GitHub Account Section ---
                item {
                    GitHubAccountCard(
                        account = githubAccount,
                        theme = theme,
                        onLinkClick = { showLinkDialog = true },
                        onUnlinkClick = onUnlinkGitHub
                    )
                }

                // --- 2. Repository & Remote Origin Section ---
                item {
                    RepositoryStatusCard(
                        directory = currentDirectory,
                        gitStatus = gitStatus,
                        currentBranch = gitCurrentBranch,
                        remoteUrl = gitRemoteUrl,
                        theme = theme,
                        onInitClick = onInitRepo,
                        onEditRemoteClick = {
                            if (newRemoteUrl.isBlank() && githubAccount.isLinked) {
                                val folderName = currentDirectory.name.lowercase().replace(' ', '-')
                                newRemoteUrl = "https://github.com/${githubAccount.username}/$folderName.git"
                            }
                            showEditRemoteDialog = true
                        },
                        onCloneClick = { showCloneDialog = true }
                    )
                }

                // --- 3. Push & Pull Actions (Enabled when Git repo exists) ---
                if (gitStatus != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = theme.editorBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Remote Sync Actions",
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor,
                                    fontSize = 14.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // PULL BUTTON
                                    Button(
                                        onClick = onPull,
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = theme.surfaceColor,
                                            contentColor = theme.onSurfaceColor
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("btn_git_pull")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                tint = theme.primaryColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Pull", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }

                                    // PUSH BUTTON
                                    Button(
                                        onClick = onPush,
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = theme.primaryColor,
                                            contentColor = theme.onPrimaryColor
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("btn_git_push")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudUpload,
                                                contentDescription = null,
                                                tint = theme.onPrimaryColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Push", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                }

                                if (gitRemoteUrl.isNotBlank()) {
                                    Text(
                                        text = "Remote: $gitRemoteUrl ($gitCurrentBranch)",
                                        color = theme.lineNumberColor,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "⚠️ No remote URL set. Click 'Set Remote' above before pushing.",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // --- 4. Staging & Commit Section ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = theme.editorBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Commit Changes",
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor,
                                        fontSize = 14.sp
                                    )

                                    val changeCount = gitStatus.totalChanges
                                    Surface(
                                        color = if (changeCount > 0) theme.primaryColor.copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (changeCount > 0) "$changeCount changed" else "Working tree clean",
                                            color = if (changeCount > 0) theme.primaryColor else Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = commitMessage,
                                    onValueChange = { commitMessage = it },
                                    placeholder = { Text("Commit message (e.g. Add responsive layout)", fontSize = 12.sp, color = theme.lineNumberColor) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_git_commit_msg"),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = theme.primaryColor,
                                        unfocusedBorderColor = theme.borderColor
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        if (commitMessage.isNotBlank()) {
                                            onCommit(commitMessage)
                                            commitMessage = ""
                                        }
                                    },
                                    enabled = !isLoading && commitMessage.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = theme.surfaceColor,
                                        contentColor = theme.primaryColor
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("btn_git_commit")
                                ) {
                                    Text("Stage All & Commit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // --- 5. Commit History (Logs) ---
                    if (gitLogs.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Commits (${gitLogs.size})",
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(gitLogs) { commit ->
                            CommitItemRow(commit = commit, theme = theme)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: Link GitHub Account ---
    if (showLinkDialog) {
        var usernameInput by remember { mutableStateOf(githubAccount.username) }
        var tokenInput by remember { mutableStateOf(githubAccount.token) }
        var emailInput by remember { mutableStateOf(githubAccount.email) }
        var isTokenVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            containerColor = theme.surfaceColor,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryColor)
                    Text("Link GitHub Account", fontWeight = FontWeight.Bold, color = theme.onSurfaceColor)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Connect with a GitHub Personal Access Token (classic or fine-grained with 'repo' scope) to authenticate push and pull requests.",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("GitHub Username") },
                        placeholder = { Text("e.g. octocat") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_github_username"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        )
                    )

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Personal Access Token (PAT)") },
                        placeholder = { Text("ghp_••••••••••••••••") },
                        singleLine = true,
                        visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                Icon(
                                    imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle token visibility",
                                    tint = theme.lineNumberColor
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_github_token"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        )
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Commit Email (Optional)") },
                        placeholder = { Text("user@users.noreply.github.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_github_email"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (usernameInput.isNotBlank()) {
                            onLinkGitHub(usernameInput, tokenInput, emailInput)
                            showLinkDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.testTag("btn_confirm_link_github")
                ) {
                    Text("Connect Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Cancel", color = theme.lineNumberColor)
                }
            }
        )
    }

    // --- DIALOG: Edit Remote URL ---
    if (showEditRemoteDialog) {
        AlertDialog(
            onDismissRequest = { showEditRemoteDialog = false },
            containerColor = theme.surfaceColor,
            title = { Text("Configure Remote Repository", fontWeight = FontWeight.Bold, color = theme.onSurfaceColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter the GitHub repository URL to push/pull to (origin):",
                        fontSize = 12.sp,
                        color = theme.lineNumberColor
                    )
                    OutlinedTextField(
                        value = newRemoteUrl,
                        onValueChange = { newRemoteUrl = it },
                        placeholder = { Text("https://github.com/user/repo.git") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_git_remote_url"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetRemote(newRemoteUrl)
                        showEditRemoteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.testTag("btn_confirm_set_remote")
                ) {
                    Text("Save Remote")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRemoteDialog = false }) {
                    Text("Cancel", color = theme.lineNumberColor)
                }
            }
        )
    }

    // --- DIALOG: Clone Repository ---
    if (showCloneDialog) {
        var selectedProvider by remember { mutableStateOf("GitHub") }
        AlertDialog(
            onDismissRequest = { showCloneDialog = false },
            containerColor = theme.surfaceColor,
            title = { Text("Clone Git Repository", fontWeight = FontWeight.Bold, color = theme.onSurfaceColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Clone any public or private repository from GitHub, GitLab, Bitbucket, Codeberg, or any Git URL:",
                        fontSize = 12.sp,
                        color = theme.lineNumberColor
                    )

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
                            androidx.compose.material3.FilterChip(
                                selected = selectedProvider == name,
                                onClick = {
                                    selectedProvider = name
                                    if (sampleUrl.isNotEmpty()) {
                                        cloneRepoUrl = sampleUrl
                                    }
                                },
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cloneRepoUrl,
                        onValueChange = { cloneRepoUrl = it },
                        placeholder = { Text("https://github.com/user/repo.git") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_git_clone_url"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = theme.borderColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cloneRepoUrl.isNotBlank()) {
                            onClone(cloneRepoUrl)
                            showCloneDialog = false
                            cloneRepoUrl = ""
                        }
                    },
                    enabled = cloneRepoUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.testTag("btn_confirm_clone_repo")
                ) {
                    Text("Clone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloneDialog = false }) {
                    Text("Cancel", color = theme.lineNumberColor)
                }
            }
        )
    }
}

@Composable
private fun GitHubAccountCard(
    account: GitHubAccount,
    theme: EditorTheme,
    onLinkClick: () -> Unit,
    onUnlinkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_github_account"),
        colors = CardDefaults.cardColors(containerColor = theme.editorBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (account.isLinked)
                                Brush.linearGradient(listOf(theme.primaryColor, Color(0xFF6366F1)))
                            else
                                Brush.linearGradient(listOf(Color(0xFF374151), Color(0xFF1F2937)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (account.isLinked) Icons.Default.Link else Icons.Default.LinkOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (account.isLinked) "@${account.username}" else "GitHub Account",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 14.sp
                        )

                        Surface(
                            color = if (account.isLinked) Color(0xFF10B981).copy(alpha = 0.15f) else theme.lineNumberColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (account.isLinked) "Linked" else "Not Linked",
                                color = if (account.isLinked) Color(0xFF10B981) else theme.lineNumberColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (account.isLinked && account.displayName.isNotBlank() && account.displayName != account.username) {
                        Text(
                            text = account.displayName,
                            color = theme.onSurfaceColor.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (account.isLinked) "Token: ${account.maskedToken} • ${account.email}" else "Link GitHub to push and pull repos",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontFamily = if (account.isLinked) FontFamily.Monospace else FontFamily.Default,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (account.isLinked) {
                OutlinedButton(
                    onClick = onUnlinkClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("btn_unlink_github")
                ) {
                    Text("Unlink", fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = onLinkClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_link_github")
                ) {
                    Text("Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RepositoryStatusCard(
    directory: File,
    gitStatus: GitStatus?,
    currentBranch: String,
    remoteUrl: String,
    theme: EditorTheme,
    onInitClick: () -> Unit,
    onEditRemoteClick: () -> Unit,
    onCloneClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.editorBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local Workspace Repository",
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor,
                    fontSize = 14.sp
                )

                TextButton(onClick = onCloneClick, modifier = Modifier.testTag("btn_git_clone")) {
                    Text("+ Clone Repo", color = theme.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                Text(
                    text = directory.name,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp
                )
                Text(
                    text = "(${directory.path})",
                    color = theme.lineNumberColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (gitStatus == null) {
                Surface(
                    color = theme.surfaceColor,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Folder is not a Git repository yet.",
                            color = theme.lineNumberColor,
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = onInitClick,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("btn_init_repo")
                        ) {
                            Text("Initialize Git", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = theme.surfaceColor,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                            Text(currentBranch, color = theme.onSurfaceColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surfaceColor)
                            .clickable(onClick = onEditRemoteClick)
                            .border(1.dp, theme.borderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (remoteUrl.isNotBlank()) remoteUrl else "Set Remote Origin...",
                            color = if (remoteUrl.isNotBlank()) theme.onSurfaceColor else theme.lineNumberColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.Edit, contentDescription = "Edit remote", tint = theme.lineNumberColor, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitItemRow(commit: GitCommit, theme: EditorTheme) {
    val dateStr = remember(commit.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.format(Date(commit.timestamp))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.editorBackground,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = theme.primaryColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = commit.shortId,
                    color = theme.primaryColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = commit.message,
                    fontWeight = FontWeight.Medium,
                    color = theme.onSurfaceColor,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${commit.author} • $dateStr",
                    color = theme.lineNumberColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}
