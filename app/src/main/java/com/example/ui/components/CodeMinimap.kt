package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.linter.LintIssue
import com.example.linter.LintSeverity
import com.example.model.EditorTheme

@Composable
fun CodeMinimap(
    theme: EditorTheme,
    codeContent: String,
    lintIssues: List<LintIssue> = emptyList(),
    scrollFraction: Float = 0f,
    onScrollToLineFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = remember(codeContent) { codeContent.lines() }
    val errorLines = remember(lintIssues) {
        lintIssues.filter { it.severity == LintSeverity.ERROR }.map { it.line }.toSet()
    }
    val warningLines = remember(lintIssues) {
        lintIssues.filter { it.severity == LintSeverity.WARNING }.map { it.line }.toSet()
    }

    Surface(
        color = theme.surfaceColor.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
        modifier = modifier
            .width(42.dp)
            .fillMaxHeight()
            .testTag("code_minimap")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 2.dp)
                .pointerInput(lines.size) {
                    detectTapGestures { offset ->
                        val tappedFraction = (offset.y / size.height).coerceIn(0f, 1f)
                        onScrollToLineFraction(tappedFraction)
                    }
                }
        ) {
            val totalLines = maxOf(1, lines.size)
            val canvasHeight = size.height
            val canvasWidth = size.width

            // Calculate height per line in minimap (min 1.5px, max 4px)
            val lineHeight = (canvasHeight / totalLines).coerceIn(1.5f, 5f)
            val displayedLines = minOf(totalLines, (canvasHeight / lineHeight).toInt())

            for (i in 0 until displayedLines) {
                val lineIndex = (i * totalLines / displayedLines).coerceIn(0, totalLines - 1)
                val line = lines.getOrNull(lineIndex) ?: ""
                val lineNum = lineIndex + 1
                val y = i * lineHeight

                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                val indentCount = line.takeWhile { it.isWhitespace() }.length
                val startX = (indentCount * 2f).coerceAtMost(canvasWidth * 0.4f)
                val barWidth = ((trimmed.length * 1.2f).coerceIn(4f, canvasWidth - startX - 4f))

                // Determine color based on line content and linter errors
                val barColor = when {
                    errorLines.contains(lineNum) -> Color(0xFFEF4444)
                    warningLines.contains(lineNum) -> Color(0xFFF59E0B)
                    trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("/*") ->
                        Color(0xFF6B7280) // comment
                    trimmed.startsWith("fun ") || trimmed.startsWith("def ") || trimmed.startsWith("function ") || trimmed.startsWith("class ") ->
                        Color(0xFFA855F7) // keyword
                    trimmed.contains("\"") || trimmed.contains("'") ->
                        Color(0xFF10B981) // string
                    trimmed.startsWith("<") ->
                        Color(0xFF38BDF8) // HTML tag
                    else ->
                        Color(0xFF94A3B8).copy(alpha = 0.7f)
                }

                // Draw line bar
                drawRect(
                    color = barColor,
                    topLeft = Offset(startX, y),
                    size = Size(barWidth, maxOf(1.2f, lineHeight - 0.5f))
                )

                // Error tick on right edge
                if (errorLines.contains(lineNum)) {
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = 2.5f,
                        center = Offset(canvasWidth - 3f, y + lineHeight / 2)
                    )
                }
            }

            // Draw Viewport Window Indicator
            val viewportHeight = (canvasHeight * 0.22f).coerceIn(24f, canvasHeight * 0.6f)
            val viewportTop = (scrollFraction * (canvasHeight - viewportHeight)).coerceIn(0f, canvasHeight - viewportHeight)

            drawRoundRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = Offset(0f, viewportTop),
                size = Size(canvasWidth, viewportHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Viewport borders
            drawRoundRect(
                color = theme.primaryColor.copy(alpha = 0.55f),
                topLeft = Offset(0f, viewportTop),
                size = Size(canvasWidth, viewportHeight),
                cornerRadius = CornerRadius(4f, 4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
        }
    }
}
