package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun SearchReplaceOverlay(
    theme: EditorTheme,
    onFindNext: (String) -> Unit,
    onFindPrevious: (String) -> Unit,
    onReplace: (String, String) -> Unit,
    onReplaceAll: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("search_replace_overlay"),
        shape = RoundedCornerShape(12.dp),
        color = theme.surfaceColor,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Find Row - with proper minHeight and line-height so descenders aren't clipped!
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Find in file...",
                            color = theme.lineNumberColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = theme.onSurfaceColor
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("search_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor,
                        focusedContainerColor = theme.editorBackground,
                        unfocusedContainerColor = theme.editorBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                IconButton(
                    onClick = { onFindPrevious(searchQuery) },
                    modifier = Modifier.size(36.dp).testTag("find_prev_button")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Find Previous", tint = theme.onSurfaceColor, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { onFindNext(searchQuery) },
                    modifier = Modifier.size(36.dp).testTag("find_next_button")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Find Next", tint = theme.onSurfaceColor, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp).testTag("close_search_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                }
            }

            // Replace Row - unclipped placeholder and text!
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = replaceQuery,
                    onValueChange = { replaceQuery = it },
                    placeholder = {
                        Text(
                            text = "Replace with...",
                            color = theme.lineNumberColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = theme.onSurfaceColor
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("replace_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor,
                        focusedContainerColor = theme.editorBackground,
                        unfocusedContainerColor = theme.editorBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = { onReplace(searchQuery, replaceQuery) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant, contentColor = theme.onSurfaceColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp).testTag("replace_one_button")
                ) {
                    Text("Replace", fontSize = 12.sp)
                }

                Button(
                    onClick = { onReplaceAll(searchQuery, replaceQuery) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp).testTag("replace_all_button")
                ) {
                    Text("All", fontSize = 12.sp)
                }
            }
        }
    }
}
