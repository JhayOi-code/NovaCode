package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EditorTheme
import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount

@Composable
fun EditProfileDialog(
    theme: EditorTheme,
    currentAccount: UserAccount?,
    onDismiss: () -> Unit,
    onSaveProfile: (username: String, customTitle: String, avatarId: String, customPhotoUri: String?) -> Unit
) {
    var username by remember { mutableStateOf(currentAccount?.name ?: "Developer") }
    var customTitle by remember { mutableStateOf(currentAccount?.customTitle ?: "Mobile Developer") }
    var selectedAvatarId by remember { mutableStateOf(currentAccount?.avatarId ?: "avatar_terminal") }
    var customPhotoUri by remember { mutableStateOf(currentAccount?.avatarCustomUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customPhotoUri = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("dialog_edit_profile")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Customize Profile",
                            color = theme.onSurfaceColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personalize your hero card name & avatar",
                            color = theme.lineNumberColor,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Preview Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.editorBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val previewAccount = UserAccount(
                            name = username.ifBlank { "Developer" },
                            customTitle = customTitle.ifBlank { "Mobile Developer" },
                            avatarId = selectedAvatarId,
                            avatarCustomUri = customPhotoUri
                        )
                        UserProfileAvatarView(account = previewAccount, size = 52.dp, borderColor = theme.primaryColor)
                        Column {
                            Text(
                                text = "Welcome, ${previewAccount.name}",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = previewAccount.customTitle,
                                color = theme.primaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Display Username") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_username")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Title Input
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Developer Role / Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_title")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Photo Option
                Text(
                    text = "Profile Photo (Optional)",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        modifier = Modifier.testTag("btn_pick_photo")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (customPhotoUri != null) "Change Photo" else "Upload Photo", fontSize = 12.sp)
                    }

                    if (customPhotoUri != null) {
                        OutlinedButton(
                            onClick = { customPhotoUri = null },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier.testTag("btn_remove_photo")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Presets Grid
                Text(
                    text = "Avatar Presets",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileAvatarPresets.presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = selectedAvatarId == preset.id && customPhotoUri == null
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) theme.primaryColor.copy(alpha = 0.2f) else theme.editorBackground,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) theme.primaryColor else theme.borderColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedAvatarId = preset.id
                                            customPhotoUri = null
                                        }
                                        .testTag("preset_${preset.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(preset.primaryColorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Text(
                                            text = preset.name,
                                            color = theme.onSurfaceColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Action
                Button(
                    onClick = {
                        val cleanName = username.trim().ifBlank { "Developer" }
                        val cleanTitle = customTitle.trim().ifBlank { "Mobile Developer" }
                        onSaveProfile(cleanName, cleanTitle, selectedAvatarId, customPhotoUri)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_profile")
                ) {
                    Text("Save & Apply to Hero Card", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
