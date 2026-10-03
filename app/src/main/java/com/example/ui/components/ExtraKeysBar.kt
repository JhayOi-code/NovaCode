package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun ExtraKeysBar(
    theme: EditorTheme,
    onInsertSymbol: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onToggleFind: () -> Unit,
    onToggleWrap: () -> Unit,
    isWrapEnabled: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val quickSymbols = listOf(
        "    " to "Tab",
        "{" to "{",
        "}" to "}",
        "(" to "(",
        ")" to ")",
        "[" to "[",
        "]" to "]",
        ";" to ";",
        ":" to ":",
        "=" to "=",
        "\"" to "\"",
        "'" to "'",
        "`" to "`",
        "$" to "$",
        "->" to "->",
        "=>" to "=>",
        "<" to "<",
        ">" to ">",
        "/" to "/",
        "\\" to "\\",
        "|" to "|",
        "!" to "!",
        "?" to "?",
        "&" to "&",
        "#" to "#",
        "@" to "@",
        "+" to "+",
        "-" to "-",
        "*" to "*",
        "_" to "_"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("extra_keys_bar"),
        color = theme.surfaceColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Action buttons
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.testTag("action_undo")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) theme.primaryColor else theme.lineNumberColor
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.testTag("action_redo")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) theme.primaryColor else theme.lineNumberColor
                )
            }

            IconButton(
                onClick = onSave,
                modifier = Modifier.testTag("action_save")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = theme.primaryColor
                )
            }

            IconButton(
                onClick = onToggleFind,
                modifier = Modifier.testTag("action_find")
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = "Find and Replace",
                    tint = theme.primaryColor
                )
            }

            IconButton(
                onClick = onToggleWrap,
                modifier = Modifier.testTag("action_toggle_wrap")
            ) {
                Icon(
                    imageVector = Icons.Default.WrapText,
                    contentDescription = "Toggle Word Wrap",
                    tint = if (isWrapEnabled) theme.primaryColor else theme.lineNumberColor
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                color = theme.borderColor
            )

            // Symbol keys
            for ((insertText, label) in quickSymbols) {
                TextButton(
                    onClick = { onInsertSymbol(insertText) },
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("key_$label")
                ) {
                    Text(
                        text = label,
                        color = theme.onSurfaceColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
