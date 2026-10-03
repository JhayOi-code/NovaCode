package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun QuickFindDialog(
    theme: EditorTheme,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    replaceQuery: String,
    onReplaceQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatchIndex: Int,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplaceCurrent: () -> Unit,
    onReplaceAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showReplace by remember { mutableStateOf(false) }
    var useRegex by remember { mutableStateOf(false) }
    var matchCase by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("quick_find_dialog"),
        shape = RoundedCornerShape(12.dp),
        color = theme.surfaceColor,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Find Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Find input field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(theme.editorBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, theme.borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("find_input_field"),
                        textStyle = TextStyle(
                            color = theme.onSurfaceColor,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(theme.cursorColor),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Find in file...", color = theme.lineNumberColor, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = if (matchCount > 0) "${currentMatchIndex + 1}/$matchCount" else "0 matches",
                            color = if (matchCount > 0) theme.primaryColor else theme.lineNumberColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onFindPrevious,
                    enabled = matchCount > 0,
                    modifier = Modifier.testTag("btn_find_prev")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Match",
                        tint = theme.primaryColor
                    )
                }

                IconButton(
                    onClick = onFindNext,
                    enabled = matchCount > 0,
                    modifier = Modifier.testTag("btn_find_next")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Match",
                        tint = theme.primaryColor
                    )
                }

                IconButton(
                    onClick = { showReplace = !showReplace },
                    modifier = Modifier.testTag("btn_toggle_replace")
                ) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Toggle Replace",
                        tint = if (showReplace) theme.primaryColor else theme.lineNumberColor
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_find")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Find",
                        tint = theme.onSurfaceColor
                    )
                }
            }

            // Replace Row (if expanded)
            if (showReplace) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(theme.editorBackground, RoundedCornerShape(8.dp))
                            .border(1.dp, theme.borderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = replaceQuery,
                            onValueChange = onReplaceQueryChange,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("replace_input_field"),
                            textStyle = TextStyle(
                                color = theme.onSurfaceColor,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(theme.cursorColor),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (replaceQuery.isEmpty()) {
                                    Text("Replace with...", color = theme.lineNumberColor, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    TextButton(
                        onClick = onReplaceCurrent,
                        enabled = matchCount > 0,
                        modifier = Modifier.testTag("btn_replace_one")
                    ) {
                        Text("Replace", color = theme.primaryColor, fontSize = 13.sp)
                    }

                    TextButton(
                        onClick = onReplaceAll,
                        enabled = matchCount > 0,
                        modifier = Modifier.testTag("btn_replace_all")
                    ) {
                        Text("All", color = theme.secondaryColor, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
