package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autocomplete.AutocompleteSuggestion
import com.example.autocomplete.SuggestionKind
import com.example.model.EditorTheme

@Composable
fun AutocompleteBar(
    theme: EditorTheme,
    suggestions: List<AutocompleteSuggestion>,
    onSelectSuggestion: (AutocompleteSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = suggestions.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = theme.surfaceColor.copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.45f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .testTag("autocomplete_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Copilot Indicator / Sparkle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (suggestions.any { it.isAi }) Color(0xFFA855F7).copy(alpha = 0.2f) else theme.primaryColor.copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (suggestions.any { it.isAi }) Icons.Default.AutoAwesome else Icons.Default.Lightbulb,
                        contentDescription = "Suggestions",
                        tint = if (suggestions.any { it.isAi }) Color(0xFFA855F7) else theme.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Scrollable chips of suggestions
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    suggestions.forEachIndexed { index, item ->
                        val (chipBg, chipBorder, chipText) = when {
                            item.isAi -> Triple(
                                Color(0xFFA855F7).copy(alpha = 0.2f),
                                Color(0xFFA855F7).copy(alpha = 0.6f),
                                Color(0xFFC084FC)
                            )
                            item.kind == SuggestionKind.FUNCTION || item.kind == SuggestionKind.METHOD -> Triple(
                                Color(0xFF38BDF8).copy(alpha = 0.15f),
                                Color(0xFF38BDF8).copy(alpha = 0.4f),
                                Color(0xFF38BDF8)
                            )
                            item.kind == SuggestionKind.KEYWORD -> Triple(
                                Color(0xFFF43F5E).copy(alpha = 0.15f),
                                Color(0xFFF43F5E).copy(alpha = 0.4f),
                                Color(0xFFFB7185)
                            )
                            item.kind == SuggestionKind.TAG -> Triple(
                                Color(0xFF10B981).copy(alpha = 0.15f),
                                Color(0xFF10B981).copy(alpha = 0.4f),
                                Color(0xFF34D399)
                            )
                            else -> Triple(
                                theme.editorBackground,
                                theme.borderColor.copy(alpha = 0.5f),
                                theme.onSurfaceColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = chipBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
                            modifier = Modifier
                                .clickable { onSelectSuggestion(item) }
                                .testTag("autocomplete_chip_$index")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (item.isAi) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFA855F7),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    Text(
                                        text = item.label,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = chipText,
                                        maxLines = 1
                                    )
                                }

                                if (!item.signature.isNullOrEmpty()) {
                                    Text(
                                        text = item.signature,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = theme.lineNumberColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp).testTag("btn_dismiss_autocomplete")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = theme.lineNumberColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
