package com.example.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Data representation of a line in the minimap with scaled tokens.
 */
internal data class MinimapBar(
    val indentX: Float,
    val width: Float,
    val color: Color
)

/**
 * Vertical code mini-map providing a bird's-eye view of long files
 * and facilitating high-speed scrolling and scrubbing.
 */
@Composable
fun CodeMinimap(
    lines: List<String>,
    verticalScrollState: ScrollState,
    colorScheme: EditorColorScheme,
    fontSizeSp: Int = 13,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Precompute minimap bars for bird's-eye syntax structure
    val minimapLines = remember(lines, colorScheme) {
        lines.map { line ->
            parseLineToMinimapBars(line, colorScheme)
        }
    }

    val mainLineHeightPx = with(density) { (fontSizeSp + 6).sp.toPx() }
    val miniLinePitchPx = with(density) { 3.dp.toPx() }
    val miniLineHeightPx = with(density) { 1.8.dp.toPx() }
    val minViewportIndicatorHeightPx = with(density) { 24.dp.toPx() }
    val dividerWidthPx = with(density) { 1.dp.toPx() }

    Row(
        modifier = modifier
            .testTag("code_minimap")
            .background(colorScheme.gutterBackground.copy(alpha = 0.96f))
    ) {
        // Vertical divider separating minimap from editor text
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(colorScheme.gutterBorder.copy(alpha = 0.7f))
        )

        // Interactive Canvas rendering lines and viewport highlight
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(lines.size) {
                    detectTapGestures { offset ->
                        val maxScroll = verticalScrollState.maxValue
                        if (maxScroll <= 0) return@detectTapGestures
                        val canvasH = size.height.toFloat()
                        val totalMiniH = lines.size * miniLinePitchPx
                        val targetFraction = if (totalMiniH <= canvasH) {
                            (offset.y / totalMiniH.coerceAtLeast(1f)).coerceIn(0f, 1f)
                        } else {
                            (offset.y / canvasH.coerceAtLeast(1f)).coerceIn(0f, 1f)
                        }
                        val targetScroll = (targetFraction * maxScroll).toInt().coerceIn(0, maxScroll)
                        coroutineScope.launch {
                            verticalScrollState.scrollTo(targetScroll)
                        }
                    }
                }
                .pointerInput(lines.size) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            val maxScroll = verticalScrollState.maxValue
                            if (maxScroll <= 0) return@detectVerticalDragGestures
                            val canvasH = size.height.toFloat()
                            val totalMiniH = lines.size * miniLinePitchPx
                            val targetFraction = if (totalMiniH <= canvasH) {
                                (offset.y / totalMiniH.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            } else {
                                (offset.y / canvasH.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            }
                            val targetScroll = (targetFraction * maxScroll).toInt().coerceIn(0, maxScroll)
                            coroutineScope.launch {
                                verticalScrollState.scrollTo(targetScroll)
                            }
                        },
                        onVerticalDrag = { change, _ ->
                            val maxScroll = verticalScrollState.maxValue
                            if (maxScroll <= 0) return@detectVerticalDragGestures
                            change.consume()
                            val canvasH = size.height.toFloat()
                            val totalMiniH = lines.size * miniLinePitchPx
                            val targetFraction = if (totalMiniH <= canvasH) {
                                (change.position.y / totalMiniH.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            } else {
                                (change.position.y / canvasH.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            }
                            val targetScroll = (targetFraction * maxScroll).toInt().coerceIn(0, maxScroll)
                            coroutineScope.launch {
                                verticalScrollState.scrollTo(targetScroll)
                            }
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val lineCount = lines.size.coerceAtLeast(1)

            val totalContentHeightPx = lineCount * mainLineHeightPx
            val maxScroll = verticalScrollState.maxValue.coerceAtLeast(0)
            val currentScroll = verticalScrollState.value.coerceIn(0, maxScroll.coerceAtLeast(1))
            val editorViewportHeightPx = (totalContentHeightPx - maxScroll).coerceAtLeast(100f)

            val totalMiniH = lineCount * miniLinePitchPx
            val isScrollable = totalMiniH > canvasH

            val scrollRatio = if (maxScroll > 0) currentScroll.toFloat() / maxScroll else 0f
            val minimapScrollY = if (isScrollable) scrollRatio * (totalMiniH - canvasH) else 0f

            // 1. Draw miniature code lines (bird's-eye view)
            val codeAreaW = canvasW - 8.dp.toPx()

            val startIndex = if (isScrollable) {
                ((minimapScrollY / miniLinePitchPx).toInt() - 2).coerceAtLeast(0)
            } else 0

            val endIndex = if (isScrollable) {
                (((minimapScrollY + canvasH) / miniLinePitchPx).toInt() + 2).coerceAtMost(lineCount - 1)
            } else (lineCount - 1)

            for (i in startIndex..endIndex) {
                val y = i * miniLinePitchPx - minimapScrollY
                if (y < -miniLinePitchPx || y > canvasH + miniLinePitchPx) continue

                val bars = minimapLines.getOrNull(i) ?: continue
                for (bar in bars) {
                    val startX = 4.dp.toPx() + bar.indentX * codeAreaW
                    val barWidth = (bar.width * codeAreaW).coerceIn(2.dp.toPx(), codeAreaW - startX + 4.dp.toPx())

                    drawRect(
                        color = bar.color,
                        topLeft = Offset(startX, y),
                        size = Size(barWidth, miniLineHeightPx)
                    )
                }
            }

            // 2. Draw Viewport Indicator (slider highlighting currently visible section)
            val indicatorTop: Float
            val indicatorHeight: Float

            if (!isScrollable) {
                indicatorHeight = ((editorViewportHeightPx / totalContentHeightPx.coerceAtLeast(1f)) * totalMiniH)
                    .coerceIn(minViewportIndicatorHeightPx, canvasH)
                indicatorTop = (scrollRatio * (totalMiniH - indicatorHeight)).coerceIn(0f, canvasH - indicatorHeight)
            } else {
                indicatorHeight = ((editorViewportHeightPx / totalContentHeightPx.coerceAtLeast(1f)) * canvasH)
                    .coerceIn(minViewportIndicatorHeightPx, canvasH * 0.7f)
                indicatorTop = (scrollRatio * (canvasH - indicatorHeight)).coerceIn(0f, canvasH - indicatorHeight)
            }

            // Indicator background tint
            drawRoundRect(
                color = colorScheme.selectionColor.copy(alpha = 0.35f),
                topLeft = Offset(1f, indicatorTop),
                size = Size(canvasW - 2f, indicatorHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Indicator border frame
            drawRoundRect(
                color = colorScheme.cursorColor.copy(alpha = 0.55f),
                topLeft = Offset(1f, indicatorTop),
                size = Size(canvasW - 2f, indicatorHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
    }
}

/**
 * Fast helper to parse a line into mini syntax bars with normalized fractions.
 */
internal fun parseLineToMinimapBars(line: String, colorScheme: EditorColorScheme): List<MinimapBar> {
    if (line.isBlank()) return emptyList()

    val indentCount = line.takeWhile { it == ' ' || it == '\t' }.length
    val trimmed = line.trimStart()
    if (trimmed.isEmpty()) return emptyList()

    // Fraction of indentation (max 40% of minimap width)
    val indentFraction = (indentCount * 0.025f).coerceAtMost(0.4f)
    val availableFraction = 1f - indentFraction

    // Comments: whole line rendered in comment color
    if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*") || trimmed.startsWith("#")) {
        val lenFraction = (trimmed.length * 0.015f).coerceIn(0.1f, availableFraction)
        return listOf(MinimapBar(indentFraction, lenFraction, colorScheme.comment.copy(alpha = 0.8f)))
    }

    val bars = mutableListOf<MinimapBar>()
    var currentOffset = indentFraction

    // Split words to create multi-tone syntax representation
    val tokens = trimmed.split(Regex("(?<=[\\s;(),{}.])|(?=[\\s;(),{}.])")).filter { it.isNotBlank() }

    for (token in tokens.take(12)) {
        if (currentOffset >= 0.95f) break
        val tokenLen = (token.length * 0.018f).coerceIn(0.04f, 0.25f)
        val color = when {
            token in FAST_KEYWORDS -> colorScheme.keyword
            token.startsWith("\"") || token.startsWith("'") -> colorScheme.string
            token.all { it.isDigit() } -> colorScheme.number
            token.first().isUpperCase() -> colorScheme.type
            token.endsWith("(") -> colorScheme.function
            else -> colorScheme.textColor.copy(alpha = 0.65f)
        }
        bars.add(MinimapBar(currentOffset, tokenLen, color))
        currentOffset += tokenLen + 0.02f
    }

    return if (bars.isEmpty()) {
        val totalLen = (trimmed.length * 0.015f).coerceIn(0.1f, availableFraction)
        listOf(MinimapBar(indentFraction, totalLen, colorScheme.textColor.copy(alpha = 0.6f)))
    } else bars
}

internal val FAST_KEYWORDS = setOf(
    "val", "var", "fun", "class", "interface", "object", "package", "import",
    "return", "if", "else", "when", "for", "while", "data", "override",
    "def", "from", "as", "const", "let", "function", "export", "public", "private"
)
