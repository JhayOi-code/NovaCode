package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.editor.SmartEditProcessor
import com.example.model.BreadcrumbHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BreadcrumbAndCursorTest {

    @Test
    fun testBreadcrumbNestedWorkspacePath() {
        val wsDir = File("/data/user/0/com.aistudio.test/files/workspace")
        val filePath = "/data/user/0/com.aistudio.test/files/workspace/src/components/Header.kt"
        val segments = BreadcrumbHelper.getSegments(filePath, "Header.kt", wsDir)

        assertEquals(4, segments.size)
        assertEquals("workspace", segments[0].label)
        assertTrue(segments[0].isDirectory)

        assertEquals("src", segments[1].label)
        assertTrue(segments[1].isDirectory)

        assertEquals("components", segments[2].label)
        assertTrue(segments[2].isDirectory)

        assertEquals("Header.kt", segments[3].label)
        assertFalse(segments[3].isDirectory)
    }

    @Test
    fun testBreadcrumbRootFile() {
        val wsDir = File("/data/user/0/com.aistudio.test/files/workspace")
        val filePath = "/data/user/0/com.aistudio.test/files/workspace/main.py"
        val segments = BreadcrumbHelper.getSegments(filePath, "main.py", wsDir)

        assertEquals(2, segments.size)
        assertEquals("workspace", segments[0].label)
        assertEquals("main.py", segments[1].label)
        assertFalse(segments[1].isDirectory)
    }

    @Test
    fun testBreadcrumbRelativePath() {
        val segments = BreadcrumbHelper.getSegments("WebStarter/index.html", "index.html")

        assertTrue(segments.isNotEmpty())
        assertEquals("index.html", segments.last().label)
        assertFalse(segments.last().isDirectory)
    }

    @Test
    fun testSpaceBarTypingDoesNotResetToLineOne() {
        val originalText = "val count = 0\nval message = \"hello\""
        // Cursor at position 13 (after count = 0)
        val oldVal = TextFieldValue(originalText, TextRange(13))
        // User presses space bar
        val spaceInserted = "val count = 0 \nval message = \"hello\""
        val newVal = TextFieldValue(spaceInserted, TextRange(14))

        val result = SmartEditProcessor.processChange(oldVal, newVal)

        // Result must NOT reset to 0 (line 1)
        assertEquals(14, result.selection.start)
        assertEquals(14, result.selection.end)
        assertEquals(spaceInserted, result.text)
    }

    @Test
    fun testEnterOnIndentedLineDoesNotResetToLineOne() {
        val originalText = "fun main() {\n    val x = 1\n}"
        val lineTwoEnd = "fun main() {\n    val x = 1".length
        val oldVal = TextFieldValue(originalText, TextRange(lineTwoEnd))

        // User presses Enter at the end of "val x = 1"
        val afterEnterText = "fun main() {\n    val x = 1\n\n}"
        val newVal = TextFieldValue(afterEnterText, TextRange(lineTwoEnd + 1))

        val result = SmartEditProcessor.processChange(oldVal, newVal)

        // Cursor must be on the new line with indentation, NOT at line 1 (0)
        val expectedCursor = "fun main() {\n    val x = 1\n    ".length
        assertEquals(expectedCursor, result.selection.start)
        assertTrue("Cursor must be well past position 0", result.selection.start > 20)
    }

    @Test
    fun testEnterBetweenBracesMaintainsIndentation() {
        val text = "fun test() {}"
        val cursorBetween = text.indexOf('{') + 1
        val oldVal = TextFieldValue(text, TextRange(cursorBetween))
        val textWithNewline = "fun test() {\n}"
        val newVal = TextFieldValue(textWithNewline, TextRange(cursorBetween + 1))

        val result = SmartEditProcessor.processChange(oldVal, newVal, isGoFile = false, tabSize = 4)

        // Must expand braces and put cursor in the middle
        assertTrue(result.text.contains("{\n    \n}"))
        val expectedPos = "fun test() {\n    ".length
        assertEquals(expectedPos, result.selection.start)
    }
}
