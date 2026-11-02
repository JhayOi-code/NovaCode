package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun SpecialCharactersToolbar(
    theme: EditorTheme,
    onInsertSymbol: (String) -> Unit,
    onSave: (() -> Unit)? = null,
    onToggleSearch: (() -> Unit)? = null,
    onRunCode: (() -> Unit)? = null,
    onUndo: (() -> Unit)? = null,
    onRedo: (() -> Unit)? = null,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onShowShortcutsHelp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val symbols = listOf("{}", "()", "[]", "\"\"", "''", "if", "=", ":", "&&", ";", "Tab", "->", "=>", "//")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .background(theme.surfaceColor)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .testTag("special_characters_toolbar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Developer Shortcut Buttons
        if (onSave != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.primaryColor.copy(alpha = 0.18f))
                    .clickable { onSave() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("shortcut_save_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Save",
                        color = theme.primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "^S",
                        color = theme.primaryColor.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (onToggleSearch != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.surfaceVariant)
                    .clickable { onToggleSearch() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("shortcut_find_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Find",
                        tint = theme.onSurfaceColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Find",
                        color = theme.onSurfaceColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "^F",
                        color = theme.lineNumberColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (onRunCode != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF059669))
                    .clickable { onRunCode() }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
                    .testTag("shortcut_run_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Run",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "^↵",
                        color = Color(0xFFD1FAE5),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (onUndo != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canUndo) theme.surfaceVariant else theme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable(enabled = canUndo) { onUndo() }
                    .padding(horizontal = 7.dp, vertical = 6.dp)
                    .testTag("shortcut_undo_chip"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) theme.onSurfaceColor else theme.lineNumberColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (onRedo != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canRedo) theme.surfaceVariant else theme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable(enabled = canRedo) { onRedo() }
                    .padding(horizontal = 7.dp, vertical = 6.dp)
                    .testTag("shortcut_redo_chip"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) theme.onSurfaceColor else theme.lineNumberColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (onShowShortcutsHelp != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF8B5CF6))
                    .clickable { onShowShortcutsHelp() }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
                    .testTag("shortcut_help_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keyboard Shortcuts",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Keys",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Divider between shortcut actions and character symbols
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(22.dp)
                .background(theme.borderColor)
        )

        // Character Symbols
        symbols.forEach { symbol ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.surfaceVariant)
                    .clickable { onInsertSymbol(symbol) }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
                    .testTag("special_char_$symbol"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
