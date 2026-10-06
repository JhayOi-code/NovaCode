package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.TerminalLine
import com.example.model.TerminalLineType

@Composable
fun TerminalSheet(
    theme: EditorTheme,
    lines: List<TerminalLine>,
    isRunning: Boolean,
    onSendCommand: (String) -> Unit,
    onClear: () -> Unit,
    onRerun: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom whenever new lines arrive
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("terminal_sheet"),
        color = Color(0xFF080C14),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Terminal Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Terminal",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = "Linq Studio Terminal",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Running / Idle indicator badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isRunning) Color(0x3322C55E) else Color(0x3364748B))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8))
                        )
                        Text(
                            text = if (isRunning) "RUNNING" else "IDLE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onRerun,
                        enabled = !isRunning,
                        modifier = Modifier.size(28.dp).testTag("btn_terminal_rerun")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Rerun",
                            tint = if (!isRunning) Color(0xFF22C55E) else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(28.dp).testTag("btn_terminal_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("btn_terminal_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quick Command Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B111E))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val chips = listOf("git status", "git commit -m \"update\"", "git push", "git log", "ls", "pwd", "date", "clear", "help", "python3")
                chips.forEach { chipCmd ->
                    FilterChip(
                        selected = false,
                        onClick = { onSendCommand(chipCmd) },
                        label = {
                            Text(
                                text = chipCmd,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF131D31)
                        ),
                        border = null,
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            // Console Log View
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF080C14))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("terminal_output_list")
            ) {
                if (lines.isEmpty()) {
                    item {
                        Text(
                            text = "Terminal ready. Run code or type a command below.",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(lines) { line ->
                        val (textColor, fontWeight) = when (line.type) {
                            TerminalLineType.COMMAND -> Color(0xFF38BDF8) to FontWeight.SemiBold
                            TerminalLineType.STDOUT -> Color(0xFFE2E8F0) to FontWeight.Normal
                            TerminalLineType.STDERR -> Color(0xFFEF4444) to FontWeight.Medium
                            TerminalLineType.INFO -> Color(0xFFA78BFA) to FontWeight.Normal
                            TerminalLineType.SUCCESS -> Color(0xFF22C55E) to FontWeight.SemiBold
                            TerminalLineType.SYSTEM -> Color(0xFF64748B) to FontWeight.Normal
                        }

                        Text(
                            text = line.text,
                            color = textColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = fontWeight,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }

            // Interactive Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "➜ ~ $",
                        color = Color(0xFF22C55E),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    BasicTextField(
                        value = inputCommand,
                        onValueChange = { inputCommand = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_input_field"),
                        textStyle = TextStyle(
                            color = Color(0xFFF8FAFC),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(theme.primaryColor),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputCommand.isNotBlank()) {
                                    val cmd = inputCommand
                                    inputCommand = ""
                                    onSendCommand(cmd)
                                }
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (inputCommand.isEmpty()) {
                                Text(
                                    text = "Type command...",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = {
                            if (inputCommand.isNotBlank()) {
                                val cmd = inputCommand
                                inputCommand = ""
                                onSendCommand(cmd)
                            }
                        },
                        modifier = Modifier.size(28.dp).testTag("btn_terminal_send")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Command",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
