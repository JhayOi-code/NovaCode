package com.example.editor

import androidx.compose.ui.text.AnnotatedString
import java.util.concurrent.ConcurrentHashMap

/**
 * Line-indexed document buffer engineered for large files.
 * Provides O(1) line access, incremental line-level syntax caching, and efficient mutation.
 */
class DocumentBuffer(initialText: String = "") {

    private val linesList = ArrayList<String>()
    private val lineSpanCache = ConcurrentHashMap<Int, AnnotatedString>()

    init {
        setText(initialText)
    }

    val lineCount: Int
        get() = linesList.size.coerceAtLeast(1)

    fun getLine(index: Int): String {
        return if (index in linesList.indices) linesList[index] else ""
    }

    fun getHighlightedLine(
        index: Int,
        colorScheme: EditorColorScheme,
        searchQuery: String = ""
    ): AnnotatedString {
        if (index !in linesList.indices) return AnnotatedString("")

        // Cache hit if no active search query
        if (searchQuery.isEmpty()) {
            val cached = lineSpanCache[index]
            if (cached != null) return cached
        }

        val rawLine = linesList[index]
        val highlighted = FastSyntaxTokenizer.highlightLine(rawLine, colorScheme, searchQuery)

        if (searchQuery.isEmpty()) {
            lineSpanCache[index] = highlighted
        }

        return highlighted
    }

    fun setText(newText: String) {
        linesList.clear()
        lineSpanCache.clear()

        if (newText.isEmpty()) {
            linesList.add("")
        } else {
            val split = newText.split("\n")
            linesList.addAll(split)
        }
    }

    fun updateLine(index: Int, newContent: String) {
        if (index in linesList.indices) {
            linesList[index] = newContent
            lineSpanCache.remove(index)
        }
    }

    fun insertLine(index: Int, lineContent: String) {
        val safeIndex = index.coerceIn(0, linesList.size)
        linesList.add(safeIndex, lineContent)
        lineSpanCache.clear() // Offset shifted
    }

    fun removeLine(index: Int) {
        if (index in linesList.indices && linesList.size > 1) {
            linesList.removeAt(index)
            lineSpanCache.clear()
        } else if (linesList.size <= 1) {
            linesList[0] = ""
            lineSpanCache.clear()
        }
    }

    fun getAllText(): String {
        return linesList.joinToString("\n")
    }

    fun clearCache() {
        lineSpanCache.clear()
    }

    val characterCount: Int
        get() = linesList.sumOf { it.length } + (lineCount - 1)
}
