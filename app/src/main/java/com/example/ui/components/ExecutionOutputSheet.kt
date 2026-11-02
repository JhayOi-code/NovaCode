package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.runner.ExecutionStatus
import com.example.runner.LiveExecutionState
import com.example.runner.OutputLine
import com.example.runner.OutputLineType

@Composable
fun ExecutionOutputSheet(
    theme: EditorTheme,
    state: LiveExecutionState,
    modifier: Modifier = Modifier,
    isCollapsed: Boolean = false,
    onToggleCollapse: () -> Unit = {},
    onRerun: () -> Unit,
    onStop: () -> Unit,
    onClear: () -> Unit,
    onSendStdin: (String) -> Unit,
    onJumpToLine: (String, Int) -> Unit,
    onOpenInTerminal: () -> Unit,
    onOpenSettings: () -> Unit = {},
    floatingWindowsCount: Int = 0,
    onClearWindows: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    var stdinInput by remember { mutableStateOf("") }
    var copiedToast by remember { mutableStateOf(false) }

    // Auto-scroll to bottom as new output lines or characters arrive
    LaunchedEffect(state.lines.size, state.lines.lastOrNull()?.text) {
        if (state.lines.isNotEmpty()) {
            listState.animateScrollToItem(state.lines.size - 1)
        }
    }

    LaunchedEffect(copiedToast) {
        if (copiedToast) {
            kotlinx.coroutines.delay(2000)
            copiedToast = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("execution_output_sheet"),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = theme.surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
        tonalElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drag Handle Bar (Draggable Indicator)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCollapse() }
                    .padding(bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(theme.lineNumberColor.copy(alpha = 0.4f))
                )
            }

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Status icon or spinner
                    when (state.status) {
                        ExecutionStatus.RUNNING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = theme.primaryColor
                            )
                        }
                        ExecutionStatus.FINISHED -> {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x3310B981),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        ExecutionStatus.ERROR -> {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x33EF4444),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Error",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        ExecutionStatus.STOPPED -> {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x33F59E0B),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stopped",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        ExecutionStatus.IDLE -> {
                            Surface(
                                shape = CircleShape,
                                color = theme.surfaceVariant,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "Idle",
                                        tint = theme.lineNumberColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${state.language.ifEmpty { "Code" }} Output",
                                color = theme.onSurfaceColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            // Short, non-wrapping chip with icon
                            RunnerBadge(runnerName = state.runnerName, theme = theme)
                        }
                        Text(
                            text = when (state.status) {
                                ExecutionStatus.RUNNING -> "Executing live..."
                                ExecutionStatus.FINISHED -> "Completed • Exit code 0"
                                ExecutionStatus.ERROR -> "Failed • Exit code ${state.exitCode ?: 1}"
                                ExecutionStatus.STOPPED -> "Stopped by user"
                                ExecutionStatus.IDLE -> "Ready"
                            },
                            color = theme.lineNumberColor,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Header Action Icons with 48dp Minimum Touch Targets
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Collapse / Expand Toggle
                    IconButton(
                        onClick = onToggleCollapse,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("toggle_collapse_output_button")
                    ) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isCollapsed) "Expand Output" else "Collapse Output",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (state.isRunning) {
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("stop_execution_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onRerun,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("rerun_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Rerun",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("clear_output_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("close_output_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Console Content
            AnimatedVisibility(visible = !isCollapsed) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Console Output Body
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF030712),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                    ) {
                        SelectionContainer {
                            if (state.lines.isEmpty()) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp)
                                ) {
                                    Text(
                                        text = if (state.isRunning) "Waiting for output stream..." else "No output recorded.",
                                        color = theme.lineNumberColor,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                ) {
                                    items(state.lines, key = { it.id }) { line ->
                                        OutputLineView(
                                            line = line,
                                            theme = theme,
                                            onJumpToLine = onJumpToLine
                                        )
                                    }
                                    if (state.isTruncated) {
                                        item {
                                            Text(
                                                text = "\n[Notice: Output truncated at buffer limit]",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stdin Input Row (Visible when execution is running)
                    if (state.isRunning) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = stdinInput,
                                onValueChange = { stdinInput = it },
                                placeholder = {
                                    Text(
                                        "Send stdin (input() / Scanner)...",
                                        fontSize = 12.sp,
                                        color = theme.lineNumberColor
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("stdin_input_field"),
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = theme.onSurfaceColor,
                                    fontFamily = FontFamily.Monospace
                                ),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primaryColor,
                                    unfocusedBorderColor = theme.borderColor,
                                    focusedContainerColor = theme.surfaceColor,
                                    unfocusedContainerColor = theme.surfaceColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (stdinInput.isNotEmpty()) {
                                        onSendStdin(stdinInput)
                                        stdinInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.primaryColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(46.dp)
                                    .testTag("send_stdin_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    // Bottom Action Row (Horizontally Scrollable, Never Truncated)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Copy Output
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(state.totalRawText))
                                copiedToast = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("copy_output_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copiedToast) "Copied!" else "Copy",
                                fontSize = 12.sp,
                                color = theme.onSurfaceColor
                            )
                        }

                        // Share Output
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "${state.language} Output")
                                    putExtra(Intent.EXTRA_TEXT, state.totalRawText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Execution Output"))
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("share_output_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp, color = theme.onSurfaceColor)
                        }

                        // Terminal
                        OutlinedButton(
                            onClick = onOpenInTerminal,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("open_in_terminal_button")
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Terminal", fontSize = 12.sp, color = theme.onSurfaceColor)
                        }

                        // Windows (N) Button
                        if (floatingWindowsCount > 0) {
                            OutlinedButton(
                                onClick = onClearWindows,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("floating_windows_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF8B5CF6))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Windows ($floatingWindowsCount)", fontSize = 12.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                            }
                        }

                        // Dismiss Button (Purple Style, full label, never truncated)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .testTag("dismiss_output_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dismiss", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RunnerBadge(runnerName: String, theme: EditorTheme) {
    val (shortName, icon) = when {
        runnerName.contains("Remote", ignoreCase = true) ||
                runnerName.contains("Piston", ignoreCase = true) ||
                runnerName.contains("Judge0", ignoreCase = true) ||
                runnerName.contains("Cloud", ignoreCase = true) -> Pair("Cloud Sandbox", Icons.Default.Cloud)
        runnerName.contains("Termux", ignoreCase = true) -> Pair("Termux", Icons.Default.Terminal)
        runnerName.contains("Embedded", ignoreCase = true) -> Pair("Embedded", Icons.Default.PlayArrow)
        else -> Pair("Sandbox", Icons.Default.Cloud)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = theme.primaryColor.copy(alpha = 0.15f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.primaryColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = shortName,
                color = theme.primaryColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OutputLineView(
    line: OutputLine,
    theme: EditorTheme,
    onJumpToLine: (String, Int) -> Unit
) {
    val textColor = when (line.type) {
        OutputLineType.STDERR -> Color(0xFFEF4444)
        OutputLineType.SYSTEM -> Color(0xFF94A3B8)
        OutputLineType.STDOUT -> Color(0xFFF1F5F9)
    }

    val isClickable = line.fileTarget != null && line.lineTarget != null

    if (isClickable) {
        // Structured Layout: Jump badge in its own top row so it NEVER overlaps error text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .background(Color(0x1AEE4444), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0x33EF4444),
                    modifier = Modifier.clickable { onJumpToLine(line.fileTarget!!, line.lineTarget!!) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Jump",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Line ${line.lineTarget} • Jump in ${line.fileTarget}",
                            color = Color(0xFFF87171),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = line.text,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Text(
            text = line.text,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
        )
    }
}
