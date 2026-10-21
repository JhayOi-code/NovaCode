package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EditorTheme
import kotlinx.coroutines.delay

@Composable
fun DebugInspectorDialog(
    theme: EditorTheme,
    openFilesCount: Int,
    activeFileName: String,
    lintErrorsCount: Int,
    lintWarningsCount: Int,
    serverStatus: String,
    onTriggerGc: () -> Unit,
    onResetWorkspace: () -> Unit,
    onDismiss: () -> Unit
) {
    var totalMem by remember { mutableLongStateOf(Runtime.getRuntime().totalMemory() / (1024 * 1024)) }
    var freeMem by remember { mutableLongStateOf(Runtime.getRuntime().freeMemory() / (1024 * 1024)) }
    var maxMem by remember { mutableLongStateOf(Runtime.getRuntime().maxMemory() / (1024 * 1024)) }
    var gcTriggeredNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024)
            freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024)
            maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        }
    }

    val usedMem = (totalMem - freeMem).coerceAtLeast(1)
    val memUsagePercent = (usedMem.toFloat() / totalMem.toFloat()).coerceIn(0f, 1f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("debug_inspector_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Full Debug & Telemetry",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp).testTag("close_debug_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // JVM Heap Memory Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "JVM Heap Memory",
                                    color = theme.onSurfaceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${usedMem}MB / ${totalMem}MB (${(memUsagePercent * 100).toInt()}%)",
                                color = theme.primaryColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { memUsagePercent },
                            color = if (memUsagePercent > 0.85f) Color(0xFFEF4444) else theme.primaryColor,
                            trackColor = theme.borderColor,
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )

                        Text(
                            text = "Max Heap: ${maxMem}MB • Free: ${freeMem}MB",
                            color = theme.lineNumberColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Engine & Workspace Telemetry
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = theme.secondaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Engine Diagnostics",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        DebugMetricRow("Active Buffer", activeFileName.ifEmpty { "None" }, theme)
                        DebugMetricRow("Open Tab Buffers", "$openFilesCount files", theme)
                        DebugMetricRow("Linter Status", "$lintErrorsCount errors, $lintWarningsCount warnings", theme)
                        DebugMetricRow("Dev Server", serverStatus, theme)
                        DebugMetricRow("Compose Frame Rate", "60 FPS Target", theme)
                        DebugMetricRow("Threads Active", "${Thread.activeCount()} threads", theme)
                    }
                }

                if (gcTriggeredNotice != null) {
                    Text(
                        text = gcTriggeredNotice!!,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Debug Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onTriggerGc()
                            System.gc()
                            totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024)
                            freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024)
                            gcTriggeredNotice = "Garbage collector triggered. Memory refreshed."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("trigger_gc_button")
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger GC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onResetWorkspace()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("reset_workspace_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Files", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugMetricRow(label: String, value: String, theme: EditorTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = theme.lineNumberColor, fontSize = 12.sp)
        Text(
            text = value,
            color = theme.onSurfaceColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
