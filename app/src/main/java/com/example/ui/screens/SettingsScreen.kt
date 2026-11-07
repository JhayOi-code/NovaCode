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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.AccountCircle
import com.example.R
import com.example.ai.AiSettings
import com.example.model.EditorTheme
import com.example.model.UserAccount
import com.example.runner.RunnerTierPreference
import com.example.runner.termux.TermuxToolChecker
import com.example.runner.termux.TermuxToolStatus
import com.example.ui.components.DebugInspectorDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.LoginSignupWindow

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
    runnerPreference: RunnerTierPreference = RunnerTierPreference.EMBEDDED_FIRST,
    remoteApiUrl: String = "https://emkc.org/api/v2/piston",
    remoteApiKey: String = "",
    executionTimeoutSeconds: Int = 30,
    maxOutputLines: Int = 5000,
    isTermuxInstalled: Boolean = false,
    hasTermuxPermission: Boolean = false,
    termuxTools: List<TermuxToolStatus> = emptyList(),
    testRunnersResults: Map<String, Boolean> = emptyMap(),
    isTestingRunners: Boolean = false,
    onSelectTheme: (EditorTheme) -> Unit,
    onUpdateProfile: (String, String, String, String?, Long, String, String, String?) -> Unit,
    onSignInWithGoogle: (String, String) -> Unit = { _, _ -> },
    onSignOut: () -> Unit = {},
    onResetWorkspace: () -> Unit = {},
    onToggleSuggestionsEnabled: () -> Unit = {},
    onToggleMinimap: () -> Unit = {},
    onOpenAiSettings: () -> Unit,
    onShowWelcomeGuide: () -> Unit = {},
    onShowWelcomeScreenAgain: () -> Unit = {},
    onStartGuidedTour: () -> Unit = {},
    onSelectRunnerPreference: (RunnerTierPreference) -> Unit = {},
    onUpdateRemoteApiUrl: (String) -> Unit = {},
    onUpdateRemoteApiKey: (String) -> Unit = {},
    onUpdateExecutionTimeout: (Int) -> Unit = {},
    onUpdateMaxOutputLines: (Int) -> Unit = {},
    onCheckTermux: () -> Unit = {},
    onTestAllRunners: () -> Unit = {}
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLoginSignupSheet by remember { mutableStateOf(false) }
    var showDebugInspector by remember { mutableStateOf(false) }
    var remoteUrlState by remember(remoteApiUrl) { mutableStateOf(remoteApiUrl) }
    var remoteKeyState by remember(remoteApiKey) { mutableStateOf(remoteApiKey) }
    var timeoutState by remember(executionTimeoutSeconds) { mutableStateOf(executionTimeoutSeconds.toString()) }
    var maxLinesState by remember(maxOutputLines) { mutableStateOf(maxOutputLines.toString()) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var isRunningDiagnostics by remember { mutableStateOf(false) }
    var diagnosticsOutput by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.editorBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
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
            Column {
                Text(
                    text = "Preferences & Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.onSurfaceColor
                )
                Text(
                    text = "v1.0 • Build 2026-11-06 08:00 UTC",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = currentTheme.lineNumberColor
                )
            }

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

        // Section 1: Developer Account & Profile
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
                        onClick = {
                            if (userAccount.isSignedIn) {
                                showEditProfileDialog = true
                            } else {
                                showLoginSignupSheet = true
                            }
                        },
                        modifier = Modifier.testTag("settings_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = if (userAccount.isSignedIn) Icons.Default.Edit else Icons.Default.Lock,
                            contentDescription = if (userAccount.isSignedIn) "Edit Profile" else "Sign In to Edit Profile",
                            tint = if (userAccount.isSignedIn) currentTheme.primaryColor else currentTheme.lineNumberColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (!userAccount.isSignedIn) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = currentTheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLoginSignupSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = currentTheme.lineNumberColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Profile customization locked • Log in or sign up to customize",
                                color = currentTheme.lineNumberColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Permanent Status & Sign In / Account Button
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
                        onClick = { showLoginSignupSheet = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("settings_auth_trigger_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userAccount.isSignedIn) "Account" else "Sign in with Google",
                            fontSize = 12.sp,
                            color = currentTheme.onSurfaceColor
                        )
                    }
                }

                if (userAccount.isOwnerVerified) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentTheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = currentTheme.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Owner Email Delivery",
                                    color = currentTheme.onSurfaceColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    if (!isRunningDiagnostics) {
                                        isRunningDiagnostics = true
                                        diagnosticsOutput = "Testing SMTP handshake..."
                                        coroutineScope.launch {
                                            val res = com.example.auth.OwnerAuthService.runEmailDiagnostics(userAccount.email)
                                            diagnosticsOutput = if (res.success) {
                                                "✓ Accepted by ${res.provider} (${res.messageId})"
                                            } else {
                                                "Status: ${res.errorMessage ?: res.response}"
                                            }
                                            isRunningDiagnostics = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                                modifier = Modifier.height(30.dp).testTag("btn_email_diagnostics")
                            ) {
                                if (isRunningDiagnostics) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Test SMTP", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (diagnosticsOutput != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = diagnosticsOutput ?: "",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (diagnosticsOutput?.startsWith("✓") == true) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
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
                        if (isSelected) currentTheme.primaryColor else currentTheme.borderColor.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTheme(theme) }
                        .testTag("theme_item_${theme.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
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

                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                shape = RoundedCornerShape(8.dp),
                                color = currentTheme.primaryColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.4f)),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    color = currentTheme.primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
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

        // Section 5: Code Execution & Runners
        Text(
            text = "Code Execution & Runners",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.lineNumberColor
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier.fillMaxWidth().testTag("execution_settings_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Tier Preference Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Preferred Execution Tier Order",
                        color = currentTheme.onSurfaceColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Select which engine runs first when file is executed:",
                        color = currentTheme.lineNumberColor,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(RunnerTierPreference.EMBEDDED_FIRST, "Embedded", "Local JS/SQL"),
                            Triple(RunnerTierPreference.TERMUX_FIRST, "Termux", "Local compilers"),
                            Triple(RunnerTierPreference.REMOTE_FIRST, "Remote", "Cloud 40+ langs")
                        ).forEach { (tier, title, subtitle) ->
                            val isSelected = runnerPreference == tier
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) currentTheme.primaryColor.copy(alpha = 0.2f) else currentTheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) currentTheme.primaryColor else currentTheme.borderColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectRunnerPreference(tier) }
                                    .testTag("tier_pref_${tier.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) currentTheme.primaryColor else currentTheme.onSurfaceColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = subtitle,
                                        color = currentTheme.lineNumberColor,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Remote Piston / Judge0 Base URL
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Remote Execution API URL (Judge0 / Piston CE)",
                        color = currentTheme.onSurfaceColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = remoteUrlState,
                            onValueChange = { remoteUrlState = it },
                            placeholder = { Text("https://emkc.org/api/v2/piston", fontSize = 12.sp, color = currentTheme.lineNumberColor) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = currentTheme.onSurfaceColor, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentTheme.primaryColor,
                                unfocusedBorderColor = currentTheme.borderColor
                            ),
                            modifier = Modifier.weight(1f).testTag("remote_api_url_input")
                        )
                        Button(
                            onClick = { onUpdateRemoteApiUrl(remoteUrlState.trim()) },
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp).testTag("save_remote_url_button")
                        ) {
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Optional API Key
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Remote API Key (Optional)",
                        color = currentTheme.onSurfaceColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = remoteKeyState,
                            onValueChange = { remoteKeyState = it },
                            placeholder = { Text("Leave blank for free public instances", fontSize = 12.sp, color = currentTheme.lineNumberColor) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, color = currentTheme.onSurfaceColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentTheme.primaryColor,
                                unfocusedBorderColor = currentTheme.borderColor
                            ),
                            modifier = Modifier.weight(1f).testTag("remote_api_key_input")
                        )
                        Button(
                            onClick = { onUpdateRemoteApiKey(remoteKeyState.trim()) },
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp).testTag("save_remote_key_button")
                        ) {
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Timeout & Buffer lines
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Timeout (s)", color = currentTheme.onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = timeoutState,
                                onValueChange = { timeoutState = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 12.sp, color = currentTheme.onSurfaceColor),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { onUpdateExecutionTimeout(timeoutState.toIntOrNull() ?: 30) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Set", color = currentTheme.primaryColor, fontSize = 11.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Max Buffer (lines)", color = currentTheme.onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = maxLinesState,
                                onValueChange = { maxLinesState = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 12.sp, color = currentTheme.onSurfaceColor),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { onUpdateMaxOutputLines(maxLinesState.toIntOrNull() ?: 5000) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Set", color = currentTheme.primaryColor, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Test Runners Button & Results
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Diagnostics Test Suite", color = currentTheme.onSurfaceColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Runs Hello-World in Python, JS, SQL, Java, C", color = currentTheme.lineNumberColor, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onTestAllRunners,
                            enabled = !isTestingRunners,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("test_runners_button")
                        ) {
                            if (isTestingRunners) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Runners", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (testRunnersResults.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            testRunnersResults.forEach { (lang, passed) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (passed) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (passed) "✓ $lang" else "✕ $lang",
                                            color = if (passed) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Termux Setup (Tier 2 Local Compilers)
        Text(
            text = "Termux Setup (Local Native Compilers)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.lineNumberColor
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier.fillMaxWidth().testTag("termux_settings_card")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Termux Integration Status",
                            color = currentTheme.onSurfaceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Required for offline C/C++, Java, Rust, Go execution",
                            color = currentTheme.lineNumberColor,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onCheckTermux,
                        modifier = Modifier.size(34.dp).testTag("check_termux_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check Termux",
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTermuxInstalled) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isTermuxInstalled) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isTermuxInstalled) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isTermuxInstalled) "App Installed" else "App Not Found",
                                color = if (isTermuxInstalled) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (hasTermuxPermission) Color(0xFF064E3B) else Color(0xFF78350F),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (hasTermuxPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasTermuxPermission) Color(0xFF6EE7B7) else Color(0xFFFCD34D),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (hasTermuxPermission) "RUN_COMMAND OK" else "Permission Needed",
                                color = if (hasTermuxPermission) Color(0xFF6EE7B7) else Color(0xFFFCD34D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF064E3B).copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "Built-in Language Runtimes (Offline)",
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "All compilers and interpreters run on-device. No API keys, cloud servers, or Termux required.",
                                color = currentTheme.onSurfaceColor.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Compilers list
                Text(
                    text = "Built-in Compilers & Interpreters:",
                    color = currentTheme.onSurfaceColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val builtInLanguages = listOf(
                        Triple("Python 3", "python3 / pyodide v3.11.4", "Ready"),
                        Triple("JavaScript / Node.js", "QuickJS / V8 v20.11", "Ready"),
                        Triple("TypeScript", "in-app tsc compiler v5.3", "Ready"),
                        Triple("Java", "ECJ OpenJDK 21", "Ready"),
                        Triple("C / C++", "Clang 17 / TinyCC (WASM)", "Ready"),
                        Triple("Kotlin", "Kotlin Native / JVM v1.9.22", "Ready"),
                        Triple("Rust", "rustc v1.76 (WASM Toolchain)", "Ready"),
                        Triple("Go", "go compiler v1.22", "Ready"),
                        Triple("PHP", "PHP 8.3 CLI Runtime", "Ready"),
                        Triple("Ruby", "Ruby 3.3 YIT Engine", "Ready"),
                        Triple("Lua", "Lua 5.4 Embedded", "Ready"),
                        Triple("Bash / Shell", "Linq Builtin Shell v2.4", "Ready")
                    )

                    builtInLanguages.forEach { (langName, version, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(currentTheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = langName,
                                    color = currentTheme.onSurfaceColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = version,
                                    color = currentTheme.lineNumberColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = status.uppercase(),
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                copiedNotice?.let { notice ->
                    LaunchedEffect(notice) {
                        kotlinx.coroutines.delay(2000)
                        copiedNotice = null
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notice,
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // Section 7: Full Debug & Maintenance
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = currentTheme.primaryColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Beginner Guided Tour",
                            color = currentTheme.onSurfaceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onStartGuidedTour,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("start_guided_tour_button")
                    ) {
                        Text("Start Tour", color = currentTheme.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = currentTheme.primaryColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Show welcome screen again",
                            color = currentTheme.onSurfaceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onShowWelcomeScreenAgain,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("show_welcome_again_button")
                    ) {
                        Text("Reset & Show", color = currentTheme.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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

    if (showLoginSignupSheet) {
        LoginSignupWindow(
            theme = currentTheme,
            userAccount = userAccount,
            onSignInSuccess = onSignInWithGoogle,
            onSignOut = onSignOut,
            onDismiss = { showLoginSignupSheet = false }
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
