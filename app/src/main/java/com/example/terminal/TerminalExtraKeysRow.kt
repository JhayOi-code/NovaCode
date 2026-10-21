package com.example.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
fun TerminalExtraKeysRow(
    theme: EditorTheme,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCtrlActive by remember { mutableStateOf(false) }
    var isAltActive by remember { mutableStateOf(false) }

    val keys = listOf(
        "ESC", "TAB", "CTRL", "ALT",
        "↑", "↓", "←", "→",
        "HOME", "END", "PGUP", "PGDN",
        "-", "|", "/", "~", "_", ":", "\"", "$"
    )

    Surface(
        color = theme.surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .testTag("terminal_extra_keys_row")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            keys.forEach { key ->
                val isStickyActive = (key == "CTRL" && isCtrlActive) || (key == "ALT" && isAltActive)
                val bgColor = when {
                    isStickyActive -> Color(0xFF10B981)
                    key in listOf("CTRL", "ALT", "ESC", "TAB") -> theme.editorBackground
                    else -> theme.surfaceColor.copy(alpha = 0.8f)
                }
                val textColor = if (isStickyActive) Color.White else theme.onSurfaceColor

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = bgColor,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isStickyActive) Color(0xFF10B981) else theme.borderColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clickable {
                            when (key) {
                                "CTRL" -> isCtrlActive = !isCtrlActive
                                "ALT" -> isAltActive = !isAltActive
                                else -> {
                                    val prefix = if (isCtrlActive) "CTRL+" else if (isAltActive) "ALT+" else ""
                                    onKeyPress(prefix + key)
                                    isCtrlActive = false
                                    isAltActive = false
                                }
                            }
                        }
                        .testTag("btn_term_key_$key")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
