package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    modifier: Modifier = Modifier
) {
    val symbols = listOf("{}", "()", "[]", "\"\"", "''", "if", "=", ":", "&&", ";", "Tab")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(theme.surfaceColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState())
            .testTag("special_characters_toolbar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        symbols.forEach { symbol ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.surfaceVariant)
                    .clickable { onInsertSymbol(symbol) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
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
