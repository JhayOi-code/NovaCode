package com.example.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DocumentBufferTest {

    @Test
    fun testLargeFileBufferLines() {
        val linesCount = 1000
        val sb = StringBuilder()
        for (i in 1..linesCount) {
            sb.append("val line$i = $i\n")
        }

        val buffer = DocumentBuffer(sb.toString())
        assertEquals(linesCount + 1, buffer.lineCount)
        assertEquals("val line1 = 1", buffer.getLine(0))
        assertEquals("val line500 = 500", buffer.getLine(499))
    }

    @Test
    fun testFastSyntaxHighlighting() {
        val line = "fun calculate(x: Int): String { return \"value: \$x\" }"
        val highlighted = FastSyntaxTokenizer.highlightLine(
            lineText = line,
            colorScheme = EditorColorScheme.DefaultDark
        )

        assertNotNull(highlighted)
        assertEquals(line, highlighted.text)
    }

    @Test
    fun testBufferIncrementalUpdate() {
        val buffer = DocumentBuffer("line 1\nline 2\nline 3")
        assertEquals(3, buffer.lineCount)

        buffer.updateLine(1, "updated line 2")
        assertEquals("updated line 2", buffer.getLine(1))
        assertEquals("line 1\nupdated line 2\nline 3", buffer.getAllText())
    }
}
