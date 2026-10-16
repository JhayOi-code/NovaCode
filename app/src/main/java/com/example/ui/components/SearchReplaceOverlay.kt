package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun SearchReplaceOverlay(
    theme: EditorTheme,
    activeFileContent: String,
    onContentChange: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var wholeWord by remember { mutableStateOf(false) }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    // Find all matches
    val matchRanges = remember(searchQuery, activeFileContent, matchCase, wholeWord) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            val list = mutableListOf<IntRange>()
            val pattern = if (wholeWord) "\\b${Regex.escape(searchQuery)}\\b" else Regex.escape(searchQuery)
            val regex = if (matchCase) Regex(pattern) else Regex(pattern, RegexOption.IGNORE_CASE)
            regex.findAll(activeFileContent).forEach { match ->
                list.add(match.range)
            }
            list
        }
    }

    val matchCount = matchRanges.size
    val currentPositionDisplay = if (matchCount == 0) {
        if (searchQuery.isEmpty()) "" else "No results"
    } else {
        "${(currentMatchIndex % matchCount) + 1} of $matchCount"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("search_replace_modal")
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            // ROW 1: SEARCH BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        currentMatchIndex = 0
                    },
                    placeholder = { Text("Find...", fontSize = 12.sp, color = theme.lineNumberColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = theme.editorBackground,
                        unfocusedContainerColor = theme.editorBackground,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("input_search_query")
                )

                // Match Count Display
                if (currentPositionDisplay.isNotEmpty()) {
                    Text(
                        text = currentPositionDisplay,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (matchCount > 0) theme.primaryColor else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Match Case Toggle (Aa)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (matchCase) theme.primaryColor.copy(alpha = 0.25f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (matchCase) theme.primaryColor else theme.borderColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clickable { matchCase = !matchCase }
                ) {
                    Text(
                        text = "Aa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (matchCase) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                    )
                }

                // Whole Word Toggle (\b)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (wholeWord) theme.primaryColor.copy(alpha = 0.25f) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (wholeWord) theme.primaryColor else theme.borderColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clickable { wholeWord = !wholeWord }
                ) {
                    Text(
                        text = "\\b",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (wholeWord) theme.primaryColor else theme.lineNumberColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                    )
                }

                // Previous Match Button
                IconButton(
                    onClick = {
                        if (matchCount > 0) {
                            currentMatchIndex = (currentMatchIndex - 1 + matchCount) % matchCount
                        }
                    },
                    enabled = matchCount > 0,
                    modifier = Modifier.size(28.dp).testTag("btn_find_prev")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Previous Match", tint = if (matchCount > 0) theme.onSurfaceColor else theme.lineNumberColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                }

                // Next Match Button
                IconButton(
                    onClick = {
                        if (matchCount > 0) {
                            currentMatchIndex = (currentMatchIndex + 1) % matchCount
                        }
                    },
                    enabled = matchCount > 0,
                    modifier = Modifier.size(28.dp).testTag("btn_find_next")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Next Match", tint = if (matchCount > 0) theme.onSurfaceColor else theme.lineNumberColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("btn_close_search")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ROW 2: REPLACE BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = replaceQuery,
                    onValueChange = { replaceQuery = it },
                    placeholder = { Text("Replace with...", fontSize = 12.sp, color = theme.lineNumberColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = theme.editorBackground,
                        unfocusedContainerColor = theme.editorBackground,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("input_replace_query")
                )

                // Replace One
                Button(
                    onClick = {
                        if (matchRanges.isNotEmpty()) {
                            val activeIdx = currentMatchIndex.coerceIn(0, matchRanges.lastIndex)
                            val targetRange = matchRanges[activeIdx]
                            val newText = activeFileContent.substring(0, targetRange.first) +
                                    replaceQuery +
                                    activeFileContent.substring(targetRange.last + 1)
                            onContentChange(newText)
                        }
                    },
                    enabled = matchRanges.isNotEmpty(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.height(34.dp).testTag("btn_replace_one")
                ) {
                    Text("Replace", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Replace All
                OutlinedButton(
                    onClick = {
                        if (searchQuery.isNotEmpty()) {
                            val pattern = if (wholeWord) "\\b${Regex.escape(searchQuery)}\\b" else Regex.escape(searchQuery)
                            val regex = if (matchCase) Regex(pattern) else Regex(pattern, RegexOption.IGNORE_CASE)
                            val newText = regex.replace(activeFileContent, replaceQuery)
                            onContentChange(newText)
                        }
                    },
                    enabled = matchCount > 0,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_replace_all")
                ) {
                    Text("Replace All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
