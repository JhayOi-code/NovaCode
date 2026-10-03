package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EditorTheme
import com.example.model.PermissionStatus
import com.example.model.PermissionTier

@Composable
fun PermissionWizardDialog(
    theme: EditorTheme,
    status: PermissionStatus,
    onRequestSafFolder: () -> Unit,
    onRequestAllFilesAccess: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("permission_wizard_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = theme.surfaceColor,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Device Storage Access",
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose your file access tier",
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.lineNumberColor
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.onSurfaceColor
                        )
                    }
                }

                // Tier 1: App Sandbox
                TierCard(
                    tier = PermissionTier.TIER_1_APP_STORAGE,
                    isGranted = true,
                    theme = theme,
                    action = null
                )

                // Tier 2: SAF Folder Picker
                TierCard(
                    tier = PermissionTier.TIER_2_SAF,
                    isGranted = status.tier2Granted,
                    theme = theme,
                    action = {
                        OutlinedButton(
                            onClick = onRequestSafFolder,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primaryColor),
                            modifier = Modifier.testTag("btn_grant_saf")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (status.tier2Granted) "Change SAF Folder" else "Grant Folder Access")
                        }
                    }
                )

                // Tier 3: All Files Access
                TierCard(
                    tier = PermissionTier.TIER_3_ALL_FILES,
                    isGranted = status.tier3Granted,
                    theme = theme,
                    action = {
                        Button(
                            onClick = onRequestAllFilesAccess,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status.tier3Granted) Color(0xFF22C55E) else theme.primaryColor,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.testTag("btn_grant_all_files")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (status.tier3Granted) "Configured in Settings" else "Open All Files Settings")
                        }
                    }
                )

                // Tier 4: Restricted paths info
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.cardBackgroundColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Android 11+ System Restrictions",
                                fontWeight = FontWeight.SemiBold,
                                color = theme.onSurfaceColor,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Google OS blocks direct access to Android/data and Android/obb even with All Files Access. For project coding, use your Documents, Downloads, or Internal Storage folders.",
                                color = theme.lineNumberColor,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_done_permission_wizard"),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = theme.onPrimaryColor)
                ) {
                    Text("Continue to NovaCode", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TierCard(
    tier: PermissionTier,
    isGranted: Boolean,
    theme: EditorTheme,
    action: (@Composable () -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) Color(0xFF22C55E).copy(alpha = 0.6f) else theme.borderColor
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF22C55E) else Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tier.title,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isGranted) Color(0xFF22C55E).copy(alpha = 0.15f)
                            else theme.surfaceColor
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isGranted) "Active" else tier.badge,
                        color = if (isGranted) Color(0xFF22C55E) else theme.lineNumberColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = tier.description,
                color = theme.lineNumberColor,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (action != null) {
                Spacer(modifier = Modifier.height(2.dp))
                action()
            }
        }
    }
}
