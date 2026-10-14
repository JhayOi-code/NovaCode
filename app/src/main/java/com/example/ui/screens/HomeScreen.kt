package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.UserAccount
import com.example.server.ServerStatus
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.UserProfileAvatarView

@Composable
fun HomeScreen(
    theme: EditorTheme,
    recentFiles: List<CodeFile>,
    serverStatus: ServerStatus,
    userAccount: UserAccount?,
    onOpenRecent: (CodeFile) -> Unit,
    onNewFile: () -> Unit,
    onOpenWorkspace: () -> Unit,
    onOpenWebDemo: () -> Unit,
    onStartDevServer: () -> Unit,
    onStopDevServer: () -> Unit,
    onUpdateProfile: (String, String, String, String?) -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HERO CARD WITH CUSTOM USERNAME AND PROFILE
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth().testTag("hero_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEditProfileDialog = true }
                            .testTag("hero_profile_clickable")
                    ) {
                        UserProfileAvatarView(
                            account = userAccount,
                            size = 54.dp,
                            borderColor = theme.primaryColor,
                            modifier = Modifier.testTag("hero_avatar_button")
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val heroDisplayName = userAccount?.name ?: "Developer"
                                Text(
                                    text = "Welcome, $heroDisplayName",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor,
                                    modifier = Modifier.testTag("hero_welcome_text")
                                )
                                IconButton(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier.size(24.dp).testTag("btn_edit_hero_username")
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = userAccount?.customTitle ?: "Mobile Developer",
                                fontSize = 12.sp,
                                color = theme.primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Linq Studio Brand Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E1B4B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor)
                    ) {
                        Text(
                            text = "LINQ STUDIO",
                            color = Color(0xFF818CF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "High-performance mobile IDE with fast code execution, VS Code extension ecosystem, and live web dev server.",
                    fontSize = 13.sp,
                    color = theme.lineNumberColor,
                    lineHeight = 18.sp
                )

                // Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNewFile,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        modifier = Modifier.weight(1f).testTag("btn_hero_new_file")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New File", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenWebDemo,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.weight(1f).testTag("btn_hero_web_demo")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Web Preview", fontSize = 12.sp)
                    }
                }
            }
        }

        // LOCAL DEV SERVER CONTROLS
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = theme.primaryColor)
                        Text(
                            text = "Local Web Dev Server",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor
                        )
                    }

                    val isRunning = serverStatus.isRunning
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRunning) Color(0xFF14532D) else Color(0xFF334155)
                    ) {
                        Text(
                            text = if (isRunning) "ACTIVE :${serverStatus.portOrNull ?: 8080}" else "STOPPED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Runs a live HTTP server hosting WebStarter with HTML, CSS, JavaScript, and dynamic assets.",
                    fontSize = 12.sp,
                    color = theme.lineNumberColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!serverStatus.isRunning) {
                        Button(
                            onClick = onStartDevServer,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.testTag("btn_start_server")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Server", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onStopDevServer,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.testTag("btn_stop_server")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Server", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onOpenWebDemo,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        modifier = Modifier.testTag("btn_launch_preview")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open Preview", fontSize = 12.sp)
                    }
                }
            }
        }

        // STARTER PROJECTS
        Text(
            text = "Starter Templates",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.onSurfaceColor
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenWebDemo() }
                .testTag("item_starter_web")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F766E).copy(alpha = 0.3f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Html, contentDescription = null, tint = Color(0xFF14B8A6))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Web Application Starter", fontWeight = FontWeight.Bold, color = theme.onSurfaceColor, fontSize = 14.sp)
                    Text(text = "HTML5, modern CSS, dynamic JavaScript with Linq Studio logo", fontSize = 12.sp, color = theme.lineNumberColor)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.lineNumberColor)
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            theme = theme,
            currentAccount = userAccount,
            onDismiss = { showEditProfileDialog = false },
            onSaveProfile = { u, t, a, p ->
                onUpdateProfile(u, t, a, p)
            }
        )
    }
}
