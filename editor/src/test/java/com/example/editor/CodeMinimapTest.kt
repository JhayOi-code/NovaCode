package com.example.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeMinimapTest {

    @Test
    fun testEmptyLineParsing() {
        val scheme = EditorColorScheme.DefaultDark
        val bars = parseLineToMinimapBars("", scheme)
        assertTrue(bars.isEmpty())

        val blankBars = parseLineToMinimapBars("    ", scheme)
        assertTrue(blankBars.isEmpty())
    }

    @Test
    fun testCommentLineParsing() {
        val scheme = EditorColorScheme.DefaultDark
        val bars = parseLineToMinimapBars("    // This is a test comment", scheme)
        assertEquals(1, bars.size)
        assertEquals(scheme.comment.copy(alpha = 0.8f), bars[0].color)
        assertTrue(bars[0].indentX > 0f)
    }

    @Test
    fun testCodeLineParsingWithKeywords() {
        val scheme = EditorColorScheme.DefaultDark
        val bars = parseLineToMinimapBars("fun calculateSum(val a: Int): Int {", scheme)
        assertFalse(bars.isEmpty())
        // Should detect keyword
        assertTrue(bars.any { it.color == scheme.keyword })
    }

    @Test
    fun testFastKeywordsCompleteness() {
        assertTrue(FAST_KEYWORDS.contains("fun"))
        assertTrue(FAST_KEYWORDS.contains("class"))
        assertTrue(FAST_KEYWORDS.contains("val"))
        assertTrue(FAST_KEYWORDS.contains("var"))
        assertTrue(FAST_KEYWORDS.contains("return"))
    }
}
