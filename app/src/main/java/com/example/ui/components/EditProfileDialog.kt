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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount

@Composable
fun EditProfileDialog(
    theme: EditorTheme,
    userAccount: UserAccount,
    onSave: (username: String, title: String, bio: String, avatarUrl: String?, colorHex: Long, stack: String, avatarId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf(userAccount.username) }
    var title by remember { mutableStateOf(userAccount.title) }
    var bio by remember { mutableStateOf(userAccount.bio) }
    var primaryStack by remember { mutableStateOf(userAccount.primaryStack) }
    var selectedAvatarId by remember { mutableStateOf(userAccount.avatarId) }
    var selectedColorHex by remember { mutableStateOf(userAccount.avatarColorHex) }

    val colorOptions = listOf(
        0xFF00E5FF, // Cyan
        0xFFFF007F, // Pink / Magenta
        0xFFA855F7, // Violet
        0xFF10B981, // Emerald
        0xFFF59E0B, // Amber
        0xFF3B82F6, // Royal Blue
        0xFFEF4444  // Ruby Red
    )

    fun getIconForPreset(key: String): ImageVector {
        return when (key) {
            "terminal" -> Icons.Default.Terminal
            "security" -> Icons.Default.Security
            "cloud" -> Icons.Default.Cloud
            "code" -> Icons.Default.Code
            "phone_android" -> Icons.Default.PhoneAndroid
            "palette" -> Icons.Default.Palette
            "bug_report" -> Icons.Default.BugReport
            "storage" -> Icons.Default.Storage
            else -> Icons.Default.Terminal
        }
    }

    val selectedPreset = remember(selectedAvatarId) {
        ProfileAvatarPresets.getById(selectedAvatarId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                Text(
                    text = "Customize Profile",
                    color = theme.onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Profile Preview Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(selectedColorHex)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val avatarColor = Color(selectedColorHex)
                            Surface(
                                shape = CircleShape,
                                color = avatarColor.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, avatarColor),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getIconForPreset(selectedPreset.iconKey),
                                        contentDescription = null,
                                        tint = avatarColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (username.isNotBlank()) username else "developer",
                                    color = theme.onSurfaceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (title.isNotBlank()) title else "Software Engineer",
                                    color = avatarColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (userAccount.isSignedIn) "Authenticated (${userAccount.authProvider})" else "Guest Mode",
                                    color = if (userAccount.isSignedIn) Color(0xFF10B981) else theme.lineNumberColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Tech stack chip
                        if (primaryStack.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.surfaceColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = primaryStack,
                                    color = theme.onSurfaceColor.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Quick Archetype / Template Chooser
                Text(
                    text = "Choose Profile Archetype:",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileAvatarPresets.templates.forEach { t ->
                        val isMatched = title == t.title
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMatched) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceColor,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isMatched) theme.primaryColor else theme.borderColor
                            ),
                            modifier = Modifier.clickable {
                                title = t.title
                                primaryStack = t.primaryStack
                                bio = t.bio
                                selectedColorHex = t.colorHex
                                selectedAvatarId = t.avatarId
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = t.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMatched) theme.primaryColor else theme.onSurfaceColor
                                )
                                Text(
                                    text = t.avatarId.removePrefix("avatar_"),
                                    fontSize = 9.sp,
                                    color = theme.lineNumberColor
                                )
                            }
                        }
                    }
                }

                // Avatar Icon Selector
                Text(
                    text = "Choose Avatar Icon:",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileAvatarPresets.presets.forEach { preset ->
                        val isSelected = selectedAvatarId == preset.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(selectedColorHex).copy(alpha = 0.25f) else theme.surfaceColor,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) Color(selectedColorHex) else theme.borderColor
                            ),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { selectedAvatarId = preset.id }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getIconForPreset(preset.iconKey),
                                    contentDescription = preset.name,
                                    tint = if (isSelected) Color(selectedColorHex) else theme.lineNumberColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Avatar Theme Color Picker
                Text(
                    text = "Avatar Accent Color:",
                    color = theme.lineNumberColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColorHex == hex
                        val c = Color(hex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Display Name / Handle", color = theme.lineNumberColor, fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 14.sp, color = theme.onSurfaceColor),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("edit_profile_username"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Developer Title / Role", color = theme.lineNumberColor, fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 14.sp, color = theme.onSurfaceColor),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("edit_profile_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Primary Tech Stack field
                OutlinedTextField(
                    value = primaryStack,
                    onValueChange = { primaryStack = it },
                    label = { Text("Primary Languages & Tech Stack", color = theme.lineNumberColor, fontSize = 12.sp) },
                    placeholder = { Text("Kotlin • Python • TypeScript", color = theme.lineNumberColor) },
                    textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("edit_profile_stack"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick stack tag pills for one-tap selection
                val quickTags = listOf("Python", "JavaScript", "TypeScript", "CSS", "Markdown", "Kotlin", "HTML", "Rust", "Go", "SQL")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTags.forEach { tag ->
                        val isContained = primaryStack.contains(tag, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isContained) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceColor,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isContained) theme.primaryColor else theme.borderColor),
                            modifier = Modifier.clickable {
                                primaryStack = if (isContained) {
                                    primaryStack.split("•").map { it.trim() }.filter { !it.equals(tag, ignoreCase = true) }.joinToString(" • ")
                                } else {
                                    if (primaryStack.isBlank()) tag else "$primaryStack • $tag"
                                }
                            }
                        ) {
                            Text(
                                text = if (isContained) "✓ $tag" else "+ $tag",
                                color = if (isContained) theme.primaryColor else theme.lineNumberColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Bio field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Status", color = theme.lineNumberColor, fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 13.sp, color = theme.onSurfaceColor),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("edit_profile_bio"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        username.trim(),
                        title.trim(),
                        bio.trim(),
                        userAccount.avatarUrl,
                        selectedColorHex,
                        primaryStack.trim(),
                        selectedAvatarId
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = theme.onSurfaceColor)
            }
        },
        containerColor = theme.surfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}
