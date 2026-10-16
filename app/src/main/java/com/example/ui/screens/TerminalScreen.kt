package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.TerminalLine

@Composable
fun TerminalScreen(
    theme: EditorTheme,
    terminalLines: List<TerminalLine>,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    onSendCommand: (String) -> Unit,
    onClearTerminal: () -> Unit
) {
    var commandInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D14))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Terminal Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                Text(
                    text = "Linq Studio Terminal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onToggleFullScreen,
                    modifier = Modifier.size(32.dp).testTag("btn_terminal_fullscreen")
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                        tint = if (isFullScreen) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onClearTerminal, modifier = Modifier.size(32.dp).testTag("btn_clear_terminal")) {
                    Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }
        }

        HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

        // Terminal Output Screen
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false
        ) {
            items(terminalLines) { line ->
                val textColor = when {
                    line.isCommand -> Color(0xFF38BDF8)
                    line.isError -> Color(0xFFEF4444)
                    else -> Color(0xFFCBD5E1)
                }
                Text(
                    text = line.text,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Command Row (Adapts cleanly to narrow screens)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF14532D)
            ) {
                Text(
                    text = "$",
                    color = Color(0xFF86EFAC),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                )
            }

            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = {
                    Text(
                        "type command (help, run, python, ls)...",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color.White
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_terminal_command")
            )

            IconButton(
                onClick = {
                    val cmd = commandInput.trim()
                    if (cmd.isNotEmpty()) {
                        onSendCommand(cmd)
                        commandInput = ""
                    }
                },
                modifier = Modifier.size(36.dp).testTag("btn_send_terminal")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
            }
        }
    }
}
