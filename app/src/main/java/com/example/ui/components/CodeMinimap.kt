package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

/**
 * High-definition Code Minimap that renders actual readable, crisp code text
 * rather than pixelated blocks, with interactive viewport indicator and smooth touch scrolling.
 */
@Composable
fun CodeMinimap(
    content: String,
    theme: EditorTheme,
    scrollProgress: Float,
    onScrollToFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val lines = content.lines()
    val totalLines = maxOf(1, lines.size)

    Box(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(theme.surfaceColor.copy(alpha = 0.85f))
            .pointerInput(totalLines) {
                detectTapGestures { offset ->
                    val fraction = (offset.y / size.height).coerceIn(0f, 1f)
                    onScrollToFraction(fraction)
                }
            }
            .pointerInput(totalLines) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.y / size.height).coerceIn(0f, 1f)
                    onScrollToFraction(fraction)
                }
            }
            .testTag("code_minimap")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate precise vertical step for lines
            val charHeight = 5.2f
            val maxVisibleLines = (canvasHeight / charHeight).toInt().coerceAtLeast(1)
            val lineStep = if (totalLines > maxVisibleLines) {
                (totalLines.toFloat() / maxVisibleLines).toInt().coerceAtLeast(1)
            } else {
                1
            }

            var currentY = 2f
            var lineIdx = 0

            while (lineIdx < totalLines && currentY < canvasHeight) {
                val rawLine = lines[lineIdx]
                val trimmed = rawLine.trimStart()
                val indentSpaces = (rawLine.length - trimmed.length).coerceAtMost(16)
                val indentX = indentSpaces * 2.2f + 3f

                if (trimmed.isNotEmpty()) {
                    // Syntax coloring matching theme tokens
                    val textColor = when {
                        trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("/*") ->
                            theme.commentColor
                        trimmed.startsWith("import ") || trimmed.startsWith("package ") || trimmed.startsWith("from ") ->
                            theme.keywordColor
                        trimmed.startsWith("fun ") || trimmed.startsWith("def ") || trimmed.startsWith("function ") ||
                        trimmed.startsWith("public ") || trimmed.startsWith("class ") || trimmed.startsWith("val ") ||
                        trimmed.startsWith("var ") || trimmed.startsWith("let ") || trimmed.startsWith("const ") ->
                            theme.keywordColor
                        trimmed.contains("(") ->
                            theme.functionColor
                        trimmed.contains("\"") || trimmed.contains("'") ->
                            theme.stringColor
                        else ->
                            theme.onSurfaceColor.copy(alpha = 0.8f)
                    }

                    // Render actual sharp, non-pixelated text
                    val displayText = trimmed.take(28)
                    try {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = displayText,
                            topLeft = Offset(indentX.coerceAtMost(canvasWidth - 10f), currentY),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 3.6.sp,
                                color = textColor
                            )
                        )
                    } catch (_: Exception) {
                        // Fallback line marker if drawText fails
                        drawRect(
                            color = textColor,
                            topLeft = Offset(indentX, currentY),
                            size = Size((trimmed.length * 2f).coerceIn(4f, canvasWidth - indentX - 4f), 1.5f)
                        )
                    }
                }

                currentY += charHeight
                lineIdx += lineStep
            }

            // Left accent border separating minimap from editor text
            drawLine(
                color = theme.borderColor,
                start = Offset(0f, 0f),
                end = Offset(0f, canvasHeight),
                strokeWidth = 1f
            )

            // Viewport Highlight Rectangle (translucent overlay showing current scroll view)
            val viewportHeight = (canvasHeight * 0.18f).coerceIn(28f, canvasHeight * 0.5f)
            val maxScrollY = (canvasHeight - viewportHeight).coerceAtLeast(0f)
            val viewportY = (scrollProgress.coerceIn(0f, 1f) * maxScrollY)

            // Viewport background fill
            drawRect(
                color = theme.primaryColor.copy(alpha = 0.16f),
                topLeft = Offset(0f, viewportY),
                size = Size(canvasWidth, viewportHeight)
            )

            // Viewport top and bottom borders
            drawLine(
                color = theme.primaryColor.copy(alpha = 0.7f),
                start = Offset(0f, viewportY),
                end = Offset(canvasWidth, viewportY),
                strokeWidth = 1.5f
            )
            drawLine(
                color = theme.primaryColor.copy(alpha = 0.7f),
                start = Offset(0f, viewportY + viewportHeight),
                end = Offset(canvasWidth, viewportY + viewportHeight),
                strokeWidth = 1.5f
            )

            // Left highlight indicator
            drawRect(
                color = theme.primaryColor,
                topLeft = Offset(0f, viewportY),
                size = Size(2.5f, viewportHeight)
            )
        }
    }
}
