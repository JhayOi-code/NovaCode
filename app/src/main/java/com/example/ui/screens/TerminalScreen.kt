package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun TerminalScreen(
    theme: EditorTheme,
    terminalLines: List<String>,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    onSendCommand: (String) -> Unit,
    onClearTerminal: () -> Unit
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Command History for Termux-style Up/Down navigation
    val commandHistory = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    val quickCommands = listOf("help", "ls -la", "pwd", "neofetch", "run", "python main.py", "java Example.java", "git status", "date", "whoami")
    val accessoryKeys = listOf("ESC", "TAB", "CTRL", "ALT", "-", "/", "|", "~", "↑", "↓", "CLEAR")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(8.dp)
            .testTag("terminal_screen")
    ) {
        // Termux Title Bar with Fullscreen Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor, RoundedCornerShape(10.dp))
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
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "linq@android:~/workspace",
                    color = Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Fullscreen Toggle
                IconButton(
                    onClick = onToggleFullScreen,
                    modifier = Modifier.size(30.dp).testTag("terminal_fullscreen_button")
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                        tint = if (isFullScreen) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onClearTerminal,
                    modifier = Modifier.size(30.dp).testTag("clear_terminal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickCommands.forEach { cmd ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = theme.surfaceColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier.clickable {
                        onSendCommand(cmd)
                        commandHistory.add(cmd)
                        historyIndex = commandHistory.size
                    }
                ) {
                    Text(
                        text = cmd,
                        color = theme.primaryColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Terminal Output Window
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF02040A), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            items(terminalLines) { line ->
                val lineColor = when {
                    line.startsWith("$") -> Color(0xFF00E5FF)
                    line.contains("Error") || line.contains("Exception") -> Color(0xFFEF4444)
                    line.contains("warning") -> Color(0xFFF59E0B)
                    line.contains("Success") || line.contains("Compiled") -> Color(0xFF10B981)
                    line.startsWith("total") -> Color(0xFF94A3B8)
                    else -> Color(0xFFE2E8F0)
                }
                Text(
                    text = line,
                    color = lineColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Termux Accessory Key Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            accessoryKeys.forEach { key ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surfaceVariant)
                        .clickable {
                            when (key) {
                                "TAB" -> commandInput += "    "
                                "CLEAR" -> onClearTerminal()
                                "↑" -> {
                                    if (commandHistory.isNotEmpty()) {
                                        historyIndex = (historyIndex - 1).coerceAtLeast(0)
                                        commandInput = commandHistory[historyIndex]
                                    }
                                }
                                "↓" -> {
                                    if (commandHistory.isNotEmpty()) {
                                        if (historyIndex < commandHistory.size - 1) {
                                            historyIndex++
                                            commandInput = commandHistory[historyIndex]
                                        } else {
                                            historyIndex = commandHistory.size
                                            commandInput = ""
                                        }
                                    }
                                }
                                else -> commandInput += key
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        color = theme.onSurfaceColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Command Prompt Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = Color(0xFF00E5FF),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("type command (ls, python, java, node, curl)...", color = theme.lineNumberColor, fontSize = 12.sp) },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFF00E5FF)),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .testTag("terminal_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF00E5FF),
                    unfocusedTextColor = Color(0xFF00E5FF),
                    focusedBorderColor = theme.primaryColor,
                    unfocusedBorderColor = theme.borderColor,
                    focusedContainerColor = Color(0xFF0A0F1D),
                    unfocusedContainerColor = Color(0xFF0A0F1D)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    val cmd = commandInput.trim()
                    if (cmd.isNotEmpty()) {
                        commandHistory.add(cmd)
                        historyIndex = commandHistory.size
                        onSendCommand(cmd)
                        commandInput = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(theme.primaryColor, RoundedCornerShape(8.dp))
                    .testTag("terminal_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
