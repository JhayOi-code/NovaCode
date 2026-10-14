package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.UserAccount
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.UserProfileAvatarView

@Composable
fun SettingsScreen(
    currentTheme: EditorTheme,
    userAccount: UserAccount?,
    onSelectTheme: (EditorTheme) -> Unit,
    onUpdateProfile: (String, String, String, String?) -> Unit,
    onOpenPlugins: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.editorBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.onSurfaceColor
        )

        // User Profile Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditProfileDialog = true }
                .testTag("settings_profile_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserProfileAvatarView(account = userAccount, size = 52.dp, borderColor = currentTheme.primaryColor)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userAccount?.name ?: "Developer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.onSurfaceColor
                    )
                    Text(
                        text = userAccount?.customTitle ?: "Mobile Developer",
                        fontSize = 12.sp,
                        color = currentTheme.primaryColor
                    )
                    Text(
                        text = "Tap to customize username & profile photo",
                        fontSize = 11.sp,
                        color = currentTheme.lineNumberColor
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = currentTheme.lineNumberColor)
            }
        }

        // Themes Section
        Text(
            text = "Editor Theme",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.onSurfaceColor
        )

        val themes = listOf(EditorTheme.LinqDark, EditorTheme.OneDark, EditorTheme.Dracula)
        themes.forEach { theme ->
            val isSelected = currentTheme.id == theme.id
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = currentTheme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(theme.editorBackground, RoundedCornerShape(4.dp))
                                .border(1.dp, theme.primaryColor, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = theme.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = currentTheme.onSurfaceColor
                        )
                    }
                    if (isSelected) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primaryColor
                        )
                    }
                }
            }
        }

        // Extensions Section
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = currentTheme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenPlugins() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Extension, contentDescription = null, tint = Color(0xFFA855F7))
                    Text(text = "Manage Extensions", color = currentTheme.onSurfaceColor)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = currentTheme.lineNumberColor)
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            theme = currentTheme,
            currentAccount = userAccount,
            onDismiss = { showEditProfileDialog = false },
            onSaveProfile = { u, t, a, p ->
                onUpdateProfile(u, t, a, p)
            }
        )
    }
}
