package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

/**
 * Terminal Screen that dynamically recolors all surfaces, output lines, prompts,
 * accessory keys, and inputs to match the active EditorTheme (all 10 themes).
 */
@Composable
fun TerminalScreen(
    theme: EditorTheme,
    terminalLines: List<String>,
    onSendCommand: (String) -> Unit,
    onClearTerminal: () -> Unit,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {}
) {
    var commandInput by remember { mutableStateOf("") }
    val commandHistory = remember { mutableListOf<String>() }
    var historyIndex by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    val pal = theme.terminal

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
            .background(pal.background)
            .padding(8.dp)
            .testTag("terminal_screen")
    ) {
        // Terminal Header with Fullscreen Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor, RoundedCornerShape(10.dp))
                .heightIn(min = 56.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("terminal-drawer-header"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = pal.prompt,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "linq@android:~/workspace",
                    color = pal.prompt,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Fullscreen Toggle (48x48 touch target)
                IconButton(
                    onClick = onToggleFullScreen,
                    modifier = Modifier.size(48.dp).testTag("terminal_fullscreen_button")
                ) {
                    Icon(
                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                        tint = if (isFullScreen) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clear Terminal (48x48 touch target)
                IconButton(
                    onClick = onClearTerminal,
                    modifier = Modifier.size(48.dp).testTag("clear_terminal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(20.dp)
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
                    color = pal.chipBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, pal.chipBorder),
                    modifier = Modifier.clickable {
                        onSendCommand(cmd)
                        commandHistory.add(cmd)
                        historyIndex = commandHistory.size
                    }
                ) {
                    Text(
                        text = cmd,
                        color = pal.chipText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                .background(pal.outputBackground, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            items(terminalLines) { line ->
                val lineColor = when {
                    line.startsWith("$") -> pal.prompt
                    line.contains("Error") || line.contains("Exception") || line.contains("Failed") -> pal.error
                    line.contains("warning") || line.contains("Warn") -> pal.warning
                    line.contains("Success") || line.contains("Compiled") -> pal.success
                    line.startsWith("total") -> theme.lineNumberColor
                    else -> pal.foreground
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
                .padding(horizontal = 6.dp, vertical = 4.dp)
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
                color = pal.prompt,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("type command (ls, python, java, node, curl)...", color = theme.lineNumberColor, fontSize = 12.sp) },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = pal.foreground),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .testTag("terminal_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = pal.foreground,
                    unfocusedTextColor = pal.foreground,
                    focusedBorderColor = pal.prompt,
                    unfocusedBorderColor = theme.borderColor,
                    focusedContainerColor = pal.inputBackground,
                    unfocusedContainerColor = pal.inputBackground
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
                    tint = if (theme.isLight) Color.White else Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
