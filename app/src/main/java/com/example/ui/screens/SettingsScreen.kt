package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiSettings
import com.example.model.EditorTheme
import com.example.model.UserAccount
import com.example.ui.components.DebugInspectorDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.GoogleAuthDialog

@Composable
fun SettingsScreen(
    currentTheme: EditorTheme,
    userAccount: UserAccount,
    aiSettings: AiSettings,
    hasApiKey: Boolean,
    isSuggestionsEnabled: Boolean = true,
    isMinimapVisible: Boolean = false,
    openFilesCount: Int = 0,
    activeFileName: String = "",
    serverStatus: String = "Stopped",
    onSelectTheme: (EditorTheme) -> Unit,
    onUpdateProfile: (String, String, String, String?, Long, String, String) -> Unit,
    onSignInWithGoogle: (String, String) -> Unit = { _, _ -> },
    onSignOut: () -> Unit = {},
    onResetWorkspace: () -> Unit = {},
    onToggleSuggestionsEnabled: () -> Unit = {},
    onToggleMinimap: () -> Unit = {},
    onOpenAiSettings: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var showDebugInspector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.editorBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preferences & Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = currentTheme.onSurfaceColor
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = currentTheme.primaryColor.copy(alpha = 0.15f),
                modifier = Modifier.clickable { showDebugInspector = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Debug",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 1: Developer Account & Google Profile
        Text(
            text = "Developer Profile",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.lineNumberColor
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier.fillMaxWidth().testTag("settings_profile_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val avatarColor = Color(userAccount.avatarColorHex)
                        Surface(
                            shape = CircleShape,
                            color = avatarColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, avatarColor),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userAccount.username.take(1).uppercase(),
                                    color = avatarColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userAccount.username,
                                    color = currentTheme.onSurfaceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (userAccount.isSignedIn) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = userAccount.email,
                                color = currentTheme.lineNumberColor,
                                fontSize = 12.sp
                            )
                            Text(
                                text = userAccount.title,
                                color = currentTheme.lineNumberColor.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("settings_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Permanent Status & Sign In with Google Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (userAccount.isSignedIn) Color(0x2210B981) else currentTheme.surfaceVariant
                    ) {
                        Text(
                            text = if (userAccount.isSignedIn) "PERMANENTLY SIGNED IN (${userAccount.authProvider})" else "GUEST SESSION",
                            color = if (userAccount.isSignedIn) Color(0xFF10B981) else currentTheme.lineNumberColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { showGoogleAuthDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("settings_auth_trigger_button")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userAccount.isSignedIn) "Account" else "Google Sign In",
                            fontSize = 12.sp,
                            color = currentTheme.onSurfaceColor
                        )
                    }
                }
            }
        }

        // Section 2: Editor Themes (All 9 Themes!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Color Themes (${EditorTheme.allThemes.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = currentTheme.lineNumberColor
            )
            Text(
                text = "Saved Permanently",
                fontSize = 11.sp,
                color = currentTheme.primaryColor
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            EditorTheme.allThemes.forEach { theme ->
                val isSelected = currentTheme.id == theme.id
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) currentTheme.primaryColor else currentTheme.borderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTheme(theme) }
                        .testTag("theme_item_${theme.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Theme Color Palette Swatches Preview
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.editorBackground)
                                    .border(1.dp, currentTheme.borderColor, RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(theme.primaryColor))
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(theme.keywordColor))
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(theme.stringColor))
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(theme.numberColor))
                            }

                            Column {
                                Text(
                                    text = theme.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = currentTheme.onSurfaceColor,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${theme.category} theme",
                                    color = currentTheme.lineNumberColor,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = currentTheme.primaryColor,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: AI Coding Assistant BYOK Card
        Text(
            text = "AI Coding Assistant",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.lineNumberColor
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAiSettings() }
                .testTag("settings_ai_assistant_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = currentTheme.primaryColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Provider & Model",
                                color = currentTheme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (hasApiKey) Color(0x3310B981) else Color(0x33EF4444)
                            ) {
                                Text(
                                    text = if (hasApiKey) "KEY CONFIGURED" else "BYOK KEY REQUIRED",
                                    color = if (hasApiKey) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${aiSettings.provider.displayName} • ${aiSettings.selectedModel}",
                            color = currentTheme.lineNumberColor,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Configure AI",
                    tint = currentTheme.lineNumberColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Section 4: Editor Preferences
        Text(
            text = "Editor Preferences",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.lineNumberColor
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Code Suggestions Bar",
                            color = currentTheme.onSurfaceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Show keyword & syntax completions above keyboard",
                            color = currentTheme.lineNumberColor,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isSuggestionsEnabled,
                        onCheckedChange = { onToggleSuggestionsEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = currentTheme.primaryColor,
                            checkedTrackColor = currentTheme.primaryColor.copy(alpha = 0.3f)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Code Minimap Preview",
                            color = currentTheme.onSurfaceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Toggle visual overview of current file structure",
                            color = currentTheme.lineNumberColor,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isMinimapVisible,
                        onCheckedChange = { onToggleMinimap() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = currentTheme.primaryColor,
                            checkedTrackColor = currentTheme.primaryColor.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        // Section 5: Full Debug & Maintenance
        Text(
            text = "Diagnostics & Maintenance",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.lineNumberColor
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Text(
                            text = "Full Debug Inspector",
                            color = currentTheme.onSurfaceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { showDebugInspector = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("open_debug_panel_button")
                    ) {
                        Text("Open Inspector", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            theme = currentTheme,
            userAccount = userAccount,
            onSave = onUpdateProfile,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showGoogleAuthDialog) {
        GoogleAuthDialog(
            theme = currentTheme,
            userAccount = userAccount,
            onSignInSuccess = onSignInWithGoogle,
            onSignOut = onSignOut,
            onDismiss = { showGoogleAuthDialog = false }
        )
    }

    if (showDebugInspector) {
        DebugInspectorDialog(
            theme = currentTheme,
            openFilesCount = openFilesCount,
            activeFileName = activeFileName,
            lintErrorsCount = 0,
            lintWarningsCount = 0,
            serverStatus = serverStatus,
            onTriggerGc = { System.gc() },
            onResetWorkspace = onResetWorkspace,
            onDismiss = { showDebugInspector = false }
        )
    }
}
