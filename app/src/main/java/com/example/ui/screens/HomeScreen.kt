package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.git.model.GitHubAccount
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.PermissionStatus
import com.example.server.ServerStatus

@Composable
fun HomeScreen(
    theme: EditorTheme,
    permissionStatus: PermissionStatus,
    recentFiles: List<CodeFile>,
    onOpenRecent: (CodeFile) -> Unit,
    onNewFile: () -> Unit,
    onOpenWorkspace: () -> Unit,
    onOpenWebDemo: () -> Unit,
    onOpenPythonDemo: () -> Unit,
    onOpenStorageWizard: () -> Unit,
    onNavigateToEditor: () -> Unit,
    serverStatus: ServerStatus = ServerStatus.Stopped,
    onStartDevServer: () -> Unit = {},
    onStopDevServer: () -> Unit = {},
    githubAccount: GitHubAccount = GitHubAccount(),
    onOpenGitPanel: () -> Unit = {},
    isLinqStudioCardVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val companyName = stringResource(R.string.company_name)
    val footerText = stringResource(R.string.footer_credit, companyName)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimalist Hero Card (Hideable via setting, reflows cleanly)
        if (isLinqStudioCardVisible) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("linq_studio_hero_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Linq Studio App Logo
                            Image(
                                painter = painterResource(R.drawable.ic_linq_logo),
                                contentDescription = "Linq Studio",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor
                                )
                                Text(
                                    text = "Simple, Fast Mobile IDE",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = theme.primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "Lightweight code editor with fast syntax highlighting, full device storage access, and responsive web preview.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.lineNumberColor,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToEditor,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.primaryColor,
                                    contentColor = theme.onPrimaryColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_open_editor_hero")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Open Editor", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onOpenWebDemo,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.cardBackgroundColor,
                                    contentColor = theme.onSurfaceColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_quick_web_demo")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Web Demo")
                            }
                        }
                    }
                }
            }
        }

        // Storage Access Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenStorageWizard)
                    .testTag("card_storage_tier"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Storage Access",
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (permissionStatus.tier3Granted) "• Full Access"
                                    else if (permissionStatus.tier2Granted) "• SAF Folder"
                                    else "• App Sandbox",
                                    fontSize = 11.sp,
                                    color = if (permissionStatus.tier3Granted) Color(0xFF22C55E) else theme.primaryColor
                                )
                            }
                            Text(
                                text = "Tap to configure device storage permissions",
                                color = theme.lineNumberColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Configure",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Local Development HTTP Server Card
        item {
            val isServerRunning = serverStatus.isRunning
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_dev_server"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = if (isServerRunning) Color(0xFF22C55E) else theme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Local Dev Server",
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor,
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isServerRunning) Color(0xFF14532D) else Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = if (isServerRunning) "RUNNING :${serverStatus.portOrNull}" else "STOPPED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isServerRunning) Color(0xFF86EFAC) else Color(0xFF94A3B8),
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (isServerRunning) serverStatus.urlOrNull ?: "http://127.0.0.1:8080/"
                                    else "NanoHTTPD embedded server for workspace web preview",
                                    color = if (isServerRunning) theme.primaryColor else theme.lineNumberColor,
                                    fontSize = 11.sp,
                                    fontFamily = if (isServerRunning) FontFamily.Monospace else FontFamily.Default
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isServerRunning) {
                            OutlinedButton(
                                onClick = onStopDevServer,
                                modifier = Modifier.weight(1f).height(32.dp).testTag("btn_stop_dev_server"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Stop Server", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = onStartDevServer,
                                modifier = Modifier.weight(1f).height(32.dp).testTag("btn_start_dev_server"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.primaryColor,
                                    contentColor = theme.onPrimaryColor
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Start Server", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onOpenWebDemo,
                            modifier = Modifier.weight(1f).height(32.dp).testTag("btn_launch_web_preview"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.cardBackgroundColor,
                                contentColor = theme.onSurfaceColor
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Web Preview", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Git & GitHub VCS Dashboard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenGitPanel)
                    .testTag("card_git_vcs"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
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

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Git & GitHub VCS",
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (githubAccount.isLinked) Color(0xFF10B981).copy(alpha = 0.15f) else theme.primaryColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (githubAccount.isLinked) "Linked" else "Connect",
                                        color = if (githubAccount.isLinked) Color(0xFF10B981) else theme.primaryColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (githubAccount.isLinked) "Connected as @${githubAccount.username} • Push & Pull code repos" else "Link your GitHub account to push & pull repositories",
                                color = theme.lineNumberColor,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Git Panel",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "Quick Launch",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = theme.onSurfaceColor
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimpleQuickCard(
                    title = "New File",
                    subtitle = "Scratchpad",
                    icon = Icons.Default.Code,
                    theme = theme,
                    onClick = onNewFile,
                    modifier = Modifier.weight(1f)
                )

                SimpleQuickCard(
                    title = "Workspace",
                    subtitle = "Browse files",
                    icon = Icons.Default.FolderOpen,
                    theme = theme,
                    onClick = onOpenWorkspace,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimpleQuickCard(
                    title = "Web Starter",
                    subtitle = "HTML/CSS/JS",
                    icon = Icons.Default.Language,
                    theme = theme,
                    onClick = onOpenWebDemo,
                    modifier = Modifier.weight(1f)
                )

                SimpleQuickCard(
                    title = "Python Demo",
                    subtitle = "Fibonacci",
                    icon = Icons.Default.Terminal,
                    theme = theme,
                    onClick = onOpenPythonDemo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Files
        if (recentFiles.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Buffers",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor
                )
            }

            items(recentFiles.size) { idx ->
                val file = recentFiles[idx]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRecent(file) },
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = file.name,
                                    fontWeight = FontWeight.Medium,
                                    color = theme.onSurfaceColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${file.language.displayName} • ${file.lineCount} lines",
                                    color = theme.lineNumberColor,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "Open",
                            color = theme.primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Footer Credit
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_footer_credit"),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = footerText,
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Linq Studio v1.0.0",
                        color = theme.lineNumberColor.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    theme: EditorTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("quick_card_$title"),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.primaryColor,
                modifier = Modifier.size(20.dp)
            )

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = theme.lineNumberColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}
