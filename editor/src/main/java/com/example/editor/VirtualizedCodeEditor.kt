package com.example.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.WrapText
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualizedCodeEditor(
    text: String,
    onTextChange: (String) -> Unit,
    colorScheme: EditorColorScheme = EditorColorScheme.DefaultDark,
    fontSizeSp: Int = 13,
    isWordWrap: Boolean = false,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(text) {
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }

    var isFindOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isMinimapVisible by remember { mutableStateOf(true) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val lines = remember(textFieldValue.text) { textFieldValue.text.split("\n") }
    val lineCount = lines.size.coerceAtLeast(1)

    // Ultra-fast line-by-line syntax highlight transformation
    val syntaxTransformation = remember(colorScheme, searchQuery) {
        VisualTransformation { rawText ->
            val builder = androidx.compose.ui.text.buildAnnotatedString {
                val lineList = rawText.text.split("\n")
                lineList.forEachIndexed { index, lineStr ->
                    val lineHighlighted = FastSyntaxTokenizer.highlightLine(
                        lineText = lineStr,
                        colorScheme = colorScheme,
                        searchQuery = searchQuery
                    )
                    append(lineHighlighted)
                    if (index < lineList.lastIndex) {
                        append("\n")
                    }
                }
            }
            TransformedText(builder, OffsetMapping.Identity)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("virtualized_code_editor")
    ) {
        // Minimalist Search Bar (if activated)
        if (isFindOpen) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = colorScheme.gutterBackground,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.gutterBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_find_input"),
                        textStyle = TextStyle(color = colorScheme.textColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        cursorBrush = SolidColor(colorScheme.cursorColor),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) Text("Find in document...", color = colorScheme.lineNumber, fontSize = 13.sp)
                            inner()
                        }
                    )

                    IconButton(
                        onClick = {
                            isFindOpen = false
                            searchQuery = ""
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colorScheme.lineNumber, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Core Text Viewport (Gutter + Text Field + Minimap)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Main Text Editing Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                ) {
                    // Line Numbers Gutter
                    Column(
                        modifier = Modifier
                            .background(colorScheme.gutterBackground)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            Text(
                                text = "$i",
                                color = colorScheme.lineNumber,
                                fontSize = fontSizeSp.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = (fontSizeSp + 6).sp
                            )
                        }
                    }

                    // Gutter Border Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(colorScheme.gutterBorder)
                    )

                    // Code Editing Area
                    val horizModifier = if (isWordWrap) Modifier else Modifier.horizontalScroll(horizontalScroll)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(horizModifier)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                onTextChange(newValue.text)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("virtualized_editor_input"),
                            textStyle = TextStyle(
                                color = colorScheme.textColor,
                                fontSize = fontSizeSp.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = (fontSizeSp + 6).sp
                            ),
                            cursorBrush = SolidColor(colorScheme.cursorColor),
                            visualTransformation = syntaxTransformation
                        )
                    }
                }
            }

            // Vertical Code Mini-Map on the right side
            if (isMinimapVisible) {
                CodeMinimap(
                    lines = lines,
                    verticalScrollState = verticalScroll,
                    colorScheme = colorScheme,
                    fontSizeSp = fontSizeSp,
                    modifier = Modifier
                        .width(54.dp)
                        .fillMaxHeight()
                )
            }
        }

        // Minimalist Extra Keys Toolbar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            color = colorScheme.gutterBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.gutterBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Undo
                IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) colorScheme.cursorColor else colorScheme.lineNumber.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo
                IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) colorScheme.cursorColor else colorScheme.lineNumber.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Search toggle
                IconButton(onClick = { isFindOpen = !isFindOpen }, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Default.FindInPage,
                        contentDescription = "Find",
                        tint = if (isFindOpen) colorScheme.cursorColor else colorScheme.textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Minimap toggle
                IconButton(
                    onClick = { isMinimapVisible = !isMinimapVisible },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("btn_toggle_minimap")
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Toggle Minimap",
                        tint = if (isMinimapVisible) colorScheme.cursorColor else colorScheme.lineNumber.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Keys
                val quickKeys = listOf("    " to "Tab", "{" to "{", "}" to "}", "(" to "(", ")" to ")", "[" to "]", "]" to "]", ";", "=", "\"", "'", ":", "<", ">", "/", "_", "+", "-", "$")
                quickKeys.forEach { item ->
                    val (insert, label) = if (item is Pair<*, *>) (item.first as String) to (item.second as String) else (item as String) to item
                    TextButton(
                        onClick = {
                            val sel = textFieldValue.selection
                            val current = textFieldValue.text
                            val updated = current.substring(0, sel.min) + insert + current.substring(sel.max)
                            val newPos = sel.min + insert.length
                            textFieldValue = TextFieldValue(updated, TextRange(newPos))
                            onTextChange(updated)
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = label,
                            color = colorScheme.textColor,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Minimal Status Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            color = colorScheme.gutterBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val pos = textFieldValue.selection.start
                val textBefore = textFieldValue.text.take(pos)
                val ln = textBefore.count { it == '\n' } + 1
                val col = pos - textBefore.lastIndexOf('\n')

                Text(
                    text = "Ln $ln, Col $col  •  $lineCount lines",
                    color = colorScheme.lineNumber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "${textFieldValue.text.length} chars  •  Minimap ${if (isMinimapVisible) "ON" else "OFF"}",
                    color = colorScheme.lineNumber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
