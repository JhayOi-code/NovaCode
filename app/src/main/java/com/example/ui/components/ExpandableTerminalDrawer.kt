package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CapturedLog
import com.example.model.EditorTheme

@Composable
fun ExpandableTerminalDrawer(
    theme: EditorTheme,
    logs: List<CapturedLog>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onSendCommand: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOGS, ERRORS
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new logs
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.lastIndex)
        }
    }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "LOGS" -> logs.filter { !it.isError && !it.isWarning }
            "ERRORS" -> logs.filter { it.isError || it.isWarning }
            else -> logs
        }
    }

    val errorCount = remember(logs) { logs.count { it.isError } }
    val warnCount = remember(logs) { logs.count { it.isWarning } }

    Card(
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D14)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(200))
            .testTag("expandable_terminal_drawer")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // DRAWER HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFF111827))
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = "Console",
                        tint = if (errorCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sandbox Console",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )

                    // Logs and Errors count badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (errorCount > 0) Color(0xFF7F1D1D) else Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${logs.size} logs${if (errorCount > 0) " • $errorCount err" else ""}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (errorCount > 0) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Filter buttons & Action icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Filter Chips
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (selectedFilter == "ALL") Color(0xFF6366F1).copy(alpha = 0.25f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedFilter == "ALL") Color(0xFF6366F1) else Color(0xFF334155)),
                        modifier = Modifier.clickable { selectedFilter = "ALL" }
                    ) {
                        Text("All", fontSize = 10.sp, color = if (selectedFilter == "ALL") Color(0xFF818CF8) else Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (selectedFilter == "ERRORS") Color(0xFFEF4444).copy(alpha = 0.25f) else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedFilter == "ERRORS") Color(0xFFEF4444) else Color(0xFF334155)),
                        modifier = Modifier.clickable { selectedFilter = "ERRORS" }
                    ) {
                        Text("Errors", fontSize = 10.sp, color = if (selectedFilter == "ERRORS") Color(0xFFF87171) else Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Clear button
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(26.dp).testTag("btn_clear_drawer")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                    }

                    // Expand / Collapse button
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(26.dp).testTag("btn_toggle_drawer_expand")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close / Dismiss button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(26.dp).testTag("btn_close_drawer")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    }
                }
            }

            // EXPANDED BODY (Console Output + Interactive Eval Bar)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 220.dp)
                        .background(Color(0xFF090D14))
                ) {
                    // Logs List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (filteredLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "No console logs captured yet. Run code in userRenderedContent sandbox to capture output.",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        } else {
                            items(filteredLogs, key = { it.id }) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = log.formattedTime,
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )

                                    val (badgeBg, badgeText) = when {
                                        log.isError -> Pair(Color(0xFF7F1D1D), Color(0xFFFCA5A5))
                                        log.isWarning -> Pair(Color(0xFF78350F), Color(0xFFFDE68A))
                                        log.level == "INFO" -> Pair(Color(0xFF1E1B4B), Color(0xFFC7D2FE))
                                        else -> Pair(Color(0xFF064E3B), Color(0xFF6EE7B7))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = badgeBg,
                                        modifier = Modifier.padding(top = 1.dp)
                                    ) {
                                        Text(
                                            text = log.level,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeText,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    val textColor = when {
                                        log.isError -> Color(0xFFF87171)
                                        log.isWarning -> Color(0xFFFBBF24)
                                        log.level == "INFO" -> Color(0xFF38BDF8)
                                        else -> Color(0xFFE2E8F0)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.message,
                                            color = textColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                        if (log.source != null) {
                                            Text(
                                                text = "${log.source}${if (log.lineNumber != null) ":${log.lineNumber}" else ""}",
                                                color = Color(0xFF64748B),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B))

                    // Quick Command / Eval Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111827))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = ">",
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            placeholder = { Text("eval expression or command...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFF111827),
                                unfocusedContainerColor = Color(0xFF111827)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("terminal_drawer_input")
                        )

                        IconButton(
                            onClick = {
                                val cmd = commandInput.trim()
                                if (cmd.isNotEmpty()) {
                                    onSendCommand(cmd)
                                    commandInput = ""
                                }
                            },
                            modifier = Modifier.size(28.dp).testTag("btn_terminal_drawer_send")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}
