package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.runner.ExecutionResult

@Composable
fun CodeOutputConsole(
    theme: EditorTheme,
    result: ExecutionResult,
    onRerun: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val isSuccess = result.isSuccess

    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSuccess) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("code_output_console")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Bar - Designed with flexible weights and spacing to prevent overlapping on any device size
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444))
                    )
                    Text(
                        text = "${result.language} Output",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSuccess) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                    ) {
                        Text(
                            text = if (isSuccess) "EXIT 0" else "EXIT ${result.exitCode}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${result.executionTimeMs}ms",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onRerun,
                        modifier = Modifier.size(28.dp).testTag("btn_console_rerun")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rerun", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = {
                            val copyText = if (result.stdout.isNotEmpty()) result.stdout else result.stderr
                            clipboardManager.setText(AnnotatedString(copyText))
                        },
                        modifier = Modifier.size(28.dp).testTag("btn_console_copy")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("btn_console_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

            // Output Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 220.dp)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    if (result.stdout.isNotEmpty()) {
                        Text(
                            text = result.stdout,
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    if (result.stderr.isNotEmpty()) {
                        if (result.stdout.isNotEmpty()) Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.stderr,
                            color = Color(0xFFF87171),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
