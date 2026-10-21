package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.ai.ProposedDiff
import com.example.model.EditorTheme

@Composable
fun DiffPreviewDialog(
    theme: EditorTheme,
    diff: ProposedDiff,
    onApply: (String) -> Unit,
    onReject: () -> Unit
) {
    var isSideBySide by remember { mutableStateOf(false) }
    val originalLines = remember(diff.originalCode) { diff.originalCode.lines() }
    val proposedLines = remember(diff.proposedCode) { diff.proposedCode.lines() }

    Dialog(
        onDismissRequest = onReject,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("diff_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = theme.editorBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Diff Preview",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "AI Code Proposal",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = diff.description,
                                color = theme.lineNumberColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Mode switch: Inline vs Side-by-Side
                    Row(
                        modifier = Modifier
                            .background(theme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (!isSideBySide) theme.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("diff_view_inline")
                        ) {
                            Text(
                                text = "Inline",
                                color = if (!isSideBySide) theme.primaryColor else theme.lineNumberColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("tab_inline")
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSideBySide) theme.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("diff_view_side_by_side")
                        ) {
                            Text(
                                text = "Side-by-Side",
                                color = if (isSideBySide) theme.primaryColor else theme.lineNumberColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("tab_side_by_side")
                            )
                        }
                    }
                }

                // Diff Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (isSideBySide) {
                        // Side-by-side view
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left: Original
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(theme.surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Current Code",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(originalLines) { idx, line ->
                                        Text(
                                            text = "${idx + 1}  $line",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = theme.onSurfaceColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Right: Proposed
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(theme.surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Proposed Change",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(proposedLines) { idx, line ->
                                        Text(
                                            text = "${idx + 1}  $line",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = Color(0xFF34D399)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Inline Diff
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(theme.surfaceColor, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val maxLines = maxOf(originalLines.size, proposedLines.size)
                            items(maxLines) { index ->
                                val origLine = originalLines.getOrNull(index)
                                val propLine = proposedLines.getOrNull(index)

                                if (origLine != null && propLine != null && origLine != propLine) {
                                    // Deletion line
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x33EF4444))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("- ", color = Color(0xFFEF4444), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                        Text(origLine, color = Color(0xFFFCA5A5), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                    // Addition line
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x3310B981))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("+ ", color = Color(0xFF10B981), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                        Text(propLine, color = Color(0xFF6EE7B7), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                } else {
                                    // Unchanged line
                                    val line = propLine ?: origLine ?: ""
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("  ", color = theme.lineNumberColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                        Text(line, color = theme.onSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Bar (Apply & Reject buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.surfaceColor)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("diff_reject_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reject")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onApply(diff.proposedCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("diff_apply_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply (Undoable)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
