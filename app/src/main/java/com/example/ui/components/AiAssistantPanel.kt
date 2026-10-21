package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiChatMessage
import com.example.ai.AiRole
import com.example.model.EditorTheme

@Composable
fun AiAssistantPanel(
    theme: EditorTheme,
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    prefilledPrompt: String = "",
    onSendMessage: (String) -> Unit,
    onStopGenerating: () -> Unit,
    onClearChat: () -> Unit,
    onClose: () -> Unit,
    onReviewDiff: (String, String) -> Unit,
    currentEditorCode: String = "",
    modifier: Modifier = Modifier
) {
    var inputText by remember(prefilledPrompt) { mutableStateOf(prefilledPrompt) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp, max = 460.dp)
            .testTag("ai_assistant_panel"),
        color = theme.editorBackground,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Linq AI",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Linq AI Assistant",
                        color = theme.onSurfaceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = theme.primaryColor,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.size(32.dp).testTag("clear_ai_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).testTag("close_ai_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = theme.primaryColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "How can I help you code today?",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask questions, fix lint errors, or request code modifications with diffs.",
                                color = theme.lineNumberColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(messages) { message ->
                    ChatMessageItem(
                        theme = theme,
                        message = message,
                        currentEditorCode = currentEditorCode,
                        onReviewDiff = onReviewDiff,
                        onCopyText = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AI Code", text))
                        }
                    )
                }
            }

            // Quick AI Action Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor.copy(alpha = 0.6f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair("⚡ Explain", "Explain how this code works step by step:\n```\n$currentEditorCode\n```"),
                    Pair("🛠 Fix Code", "Review and fix any potential bugs or logic errors in this code:\n```\n$currentEditorCode\n```"),
                    Pair("📝 Comments", "Add clear inline explanatory comments to this code:\n```\n$currentEditorCode\n```"),
                    Pair("⌨️ Typewriter", "Write a Python script that prints text using a smooth typewriter effect in terminal."),
                    Pair("🚀 Optimize", "Optimize the runtime performance and readability of this code:\n```\n$currentEditorCode\n```")
                ).forEach { (label, prompt) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .clickable { onSendMessage(prompt) }
                            .testTag("ai_quick_chip_$label")
                    ) {
                        Text(
                            text = label,
                            color = theme.primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask AI to code, refactor or fix...",
                            color = theme.lineNumberColor,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 100.dp)
                        .testTag("ai_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor,
                        focusedContainerColor = theme.editorBackground,
                        unfocusedContainerColor = theme.editorBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isLoading) {
                    IconButton(
                        onClick = onStopGenerating,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .testTag("stop_ai_generating_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val trimmed = inputText.trim()
                            if (trimmed.isNotEmpty()) {
                                onSendMessage(trimmed)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.primaryColor.copy(alpha = 0.2f))
                            .testTag("send_ai_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    theme: EditorTheme,
    message: AiChatMessage,
    currentEditorCode: String,
    onReviewDiff: (String, String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val isUser = message.role == AiRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            color = if (isUser) theme.primaryColor.copy(alpha = 0.15f) else theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) theme.primaryColor.copy(alpha = 0.3f) else theme.borderColor
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Parse code fences ```...```
                val content = message.content
                if (content.contains("```")) {
                    val segments = content.split("```")
                    segments.forEachIndexed { index, segment ->
                        if (index % 2 == 1) {
                            // Code block!
                            val lines = segment.lines()
                            val lang = lines.firstOrNull()?.trim() ?: ""
                            val codeContent = if (lines.size > 1) lines.drop(1).joinToString("\n").trimEnd() else segment

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.editorBackground)
                                    .border(1.dp, theme.borderColor, RoundedCornerShape(8.dp))
                            ) {
                                // Code block bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(theme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang.ifEmpty { "code" },
                                        color = theme.primaryColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Row {
                                        // Review Diff button
                                        IconButton(
                                            onClick = { onReviewDiff(currentEditorCode, codeContent) },
                                            modifier = Modifier.size(26.dp).testTag("code_block_diff_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Compare,
                                                contentDescription = "Review Diff",
                                                tint = theme.primaryColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        // Copy button for code blocks
                                        IconButton(
                                            onClick = { onCopyText(codeContent) },
                                            modifier = Modifier.size(26.dp).testTag("code_block_copy_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Code",
                                                tint = theme.lineNumberColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = codeContent,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = theme.onSurfaceColor,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else if (segment.isNotBlank()) {
                            Text(
                                text = segment.trim(),
                                color = theme.onSurfaceColor,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = content,
                        color = theme.onSurfaceColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
