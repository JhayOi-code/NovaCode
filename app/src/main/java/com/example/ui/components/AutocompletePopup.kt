package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.autocomplete.AutocompleteSuggestion
import com.example.model.EditorTheme

@Composable
fun AutocompleteBar(
    theme: EditorTheme,
    suggestions: List<AutocompleteSuggestion>,
    isEnabled: Boolean,
    onToggleEnabled: () -> Unit,
    onSelectSuggestion: (AutocompleteSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isEnabled) {
        // Bar is fully hidden when off, editor uses freed space!
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(theme.surfaceColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("autocomplete_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle button on the bar's edge
        IconButton(
            onClick = onToggleEnabled,
            modifier = Modifier
                .size(36.dp)
                .testTag("toggle_suggestions_button")
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb,
                contentDescription = "Toggle Suggestions",
                tint = if (isEnabled) theme.primaryColor else theme.lineNumberColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Suggestions horizontal list (takes full available width)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (suggestions.isEmpty()) {
                Text(
                    text = "Type for live code suggestions...",
                    color = theme.lineNumberColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceVariant)
                            .clickable { onSelectSuggestion(suggestion) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("suggestion_${suggestion.displayText}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = suggestion.type.take(1).uppercase(),
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = suggestion.displayText,
                                color = theme.onSurfaceColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Quick dismiss
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(32.dp)
                .testTag("dismiss_suggestions_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close suggestions",
                tint = theme.lineNumberColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
