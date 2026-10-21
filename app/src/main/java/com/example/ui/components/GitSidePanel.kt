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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.GitFileChange
import com.example.model.GitRepositoryStatus

@Composable
fun GitSidePanel(
    theme: EditorTheme,
    gitStatus: GitRepositoryStatus,
    onStageFile: (String) -> Unit,
    onUnstageFile: (String) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onCommit: (String) -> Unit,
    onPush: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    val stagedChanges = remember(gitStatus.changes) { gitStatus.changes.filter { it.isStaged } }
    val unstagedChanges = remember(gitStatus.changes) { gitStatus.changes.filter { !it.isStaged } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.surfaceColor)
            .padding(12.dp)
            .testTag("git_side_panel")
    ) {
        // Header with branch and push
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ForkRight,
                    contentDescription = "Branch",
                    tint = theme.primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = gitStatus.currentBranch,
                    color = theme.onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Push Button
            IconButton(
                onClick = onPush,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.surfaceVariant)
                    .testTag("git_push_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Push",
                    tint = if (gitStatus.aheadCount > 0) theme.primaryColor else theme.lineNumberColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Commit message input
        OutlinedTextField(
            value = commitMessage,
            onValueChange = { commitMessage = it },
            placeholder = { Text("Commit message...", color = theme.lineNumberColor, fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("git_commit_message_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.onSurfaceColor,
                unfocusedTextColor = theme.onSurfaceColor,
                focusedBorderColor = theme.primaryColor,
                unfocusedBorderColor = theme.borderColor,
                focusedContainerColor = theme.editorBackground,
                unfocusedContainerColor = theme.editorBackground
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Commit Button
        Button(
            onClick = {
                if (commitMessage.isNotBlank()) {
                    onCommit(commitMessage)
                    commitMessage = ""
                }
            },
            enabled = commitMessage.isNotBlank() && stagedChanges.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("git_commit_button")
        ) {
            Icon(Icons.Default.Commit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Commit (${stagedChanges.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lists of Staged & Unstaged
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Staged section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STAGED CHANGES (${stagedChanges.size})",
                        color = theme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    if (stagedChanges.isNotEmpty()) {
                        Text(
                            text = "Unstage All",
                            color = theme.lineNumberColor,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { onUnstageAll() }
                                .padding(4.dp)
                                .testTag("git_unstage_all_button")
                        )
                    }
                }
            }

            items(stagedChanges) { change ->
                GitFileItem(
                    theme = theme,
                    change = change,
                    isStaged = true,
                    onToggleStage = { onUnstageFile(change.filePath) }
                )
            }

            // Unstaged section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHANGES (${unstagedChanges.size})",
                        color = theme.lineNumberColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    if (unstagedChanges.isNotEmpty()) {
                        Text(
                            text = "Stage All",
                            color = theme.primaryColor,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { onStageAll() }
                                .padding(4.dp)
                                .testTag("git_stage_all_button")
                        )
                    }
                }
            }

            if (unstagedChanges.isEmpty() && stagedChanges.isEmpty()) {
                item {
                    Text(
                        text = "Working tree clean",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            items(unstagedChanges) { change ->
                GitFileItem(
                    theme = theme,
                    change = change,
                    isStaged = false,
                    onToggleStage = { onStageFile(change.filePath) }
                )
            }
        }
    }
}

@Composable
private fun GitFileItem(
    theme: EditorTheme,
    change: GitFileChange,
    isStaged: Boolean,
    onToggleStage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(theme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onToggleStage() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("git_file_${change.fileName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "M",
                color = Color(0xFFF59E0B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = change.fileName,
                color = theme.onSurfaceColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        IconButton(
            onClick = onToggleStage,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (isStaged) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = if (isStaged) "Unstage" else "Stage",
                tint = theme.lineNumberColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
