package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.plugin.PluginItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginQuickSheet(
    theme: EditorTheme,
    installedPlugins: List<PluginItem>,
    onDismiss: () -> Unit,
    onFormatDocument: () -> Unit,
    onRunDiagnostics: () -> Unit,
    onShowStats: () -> Unit,
    onShowTodos: () -> Unit,
    onOpenMarketplace: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceColor,
        modifier = Modifier.testTag("plugin_quick_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Extension, contentDescription = null, tint = theme.primaryColor)
                    Text(
                        text = "VS Code Extensions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )
                }
                TextButton(onClick = {
                    onDismiss()
                    onOpenMarketplace()
                }) {
                    Text("Browse Store", color = theme.primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onFormatDocument()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.weight(1f).testTag("btn_quick_format")
                ) {
                    Icon(Icons.Default.FormatAlignLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Format", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onRunDiagnostics()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.weight(1f).testTag("btn_quick_lint")
                ) {
                    Icon(Icons.Default.Rule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ESLint", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        onShowStats()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                    modifier = Modifier.weight(1f).testTag("btn_quick_stats")
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stats", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Active Plugins (${installedPlugins.count { it.isEnabled }})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = theme.lineNumberColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(installedPlugins) { plugin ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.editorBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plugin.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.onSurfaceColor
                                )
                                Text(
                                    text = plugin.description,
                                    fontSize = 11.sp,
                                    color = theme.lineNumberColor,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (plugin.isEnabled) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (plugin.isEnabled) "ACTIVE" else "OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (plugin.isEnabled) Color(0xFF10B981) else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
