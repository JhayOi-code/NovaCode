package com.example.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartEditorFeaturesTest {

    // 1. Create Main.java → class name is Main, cursor is inside main().
    @Test
    fun testJavaTemplate() {
        val res = FileTemplates.getTemplateForFile("Main.java")
        assertNotNull(res)
        assertTrue(res!!.content.contains("public class Main {"))
        assertTrue(res.content.contains("public static void main(String[] args) {"))
        assertTrue(!res.content.contains("|"))
        // Cursor position must be inside main()
        val mainLine = res.content.indexOf("public static void main")
        assertTrue("Cursor should be after main definition", res.cursorPosition > mainLine)
    }

    // 2. Create a .py, .kt, .js, .html, .css, .c, .cpp, .cs, .go, .rs, .php and .swift file → each gets its template.
    @Test
    fun testAllLanguagesTemplates() {
        val languages = listOf("py", "kt", "js", "html", "css", "c", "cpp", "cs", "go", "rs", "php", "swift")
        for (ext in languages) {
            val template = FileTemplates.getTemplateForFile("sample.$ext")
            assertNotNull("Template should exist for .$ext", template)
            assertTrue("Template for .$ext should not be empty", template!!.content.isNotEmpty())
            assertTrue("Template for .$ext should not have literal |", !template.content.contains("|"))
        }
    }

    // 3. Create a .txt file → stays empty.
    @Test
    fun testUnknownExtensionStaysEmpty() {
        val template = FileTemplates.getTemplateForFile("notes.txt")
        assertNull("Unknown extension should have null template", template)

        val docTemplate = FileTemplates.getTemplateForFile("doc.markdown")
        assertNull(docTemplate)
    }

    // 4. Type each pair → closing partner appears, cursor in between.
    @Test
    fun testAutoClosingPairs() {
        val pairs = listOf(
            '(' to ')',
            '[' to ']',
            '{' to '}',
            '"' to '"',
            '\'' to '\''
        )

        for ((open, close) in pairs) {
            val oldVal = TextFieldValue("", TextRange(0))
            val newVal = TextFieldValue(open.toString(), TextRange(1))
            val result = SmartEditProcessor.processChange(oldVal, newVal)

            assertEquals("Typing $open must insert $open$close", "$open$close", result.text)
            assertEquals("Cursor must be between pair", TextRange(1), result.selection)
        }
    }

    // 5. Type a closing character over an existing one → it skips over it.
    @Test
    fun testSkipOverClosingCharacter() {
        val closingChars = listOf(')', ']', '}', '"', '\'')
        for (ch in closingChars) {
            val oldVal = TextFieldValue("abc$ch", TextRange(3)) // cursor right before ch
            val newVal = TextFieldValue("abc$ch$ch", TextRange(4)) // soft keyboard typed ch
            val result = SmartEditProcessor.processChange(oldVal, newVal)

            assertEquals("Must not duplicate closing character $ch", "abc$ch", result.text)
            assertEquals("Cursor must skip over $ch", TextRange(4), result.selection)
        }
    }

    // 6. Backspace on an empty pair → both removed.
    @Test
    fun testBackspaceEmptyPair() {
        val pairs = listOf(
            "()" to "",
            "[]" to "",
            "{}" to "",
            "\"\"" to "",
            "''" to ""
        )

        for ((pair, expected) in pairs) {
            val oldVal = TextFieldValue("x${pair}y", TextRange(2)) // cursor between pair
            val newVal = TextFieldValue("x${pair[1]}y", TextRange(1)) // backspace deleted opening char
            val result = SmartEditProcessor.processChange(oldVal, newVal)

            assertEquals("Backspace on $pair must delete both", "xy", result.text)
            assertEquals("Cursor should be at position 1", TextRange(1), result.selection)
        }
    }

    // 7. Type { then Enter → indented blank line with } below.
    @Test
    fun testSmartEnterBetweenBraces() {
        // Line has 4 spaces indent, then {}
        val oldVal = TextFieldValue("    {}", TextRange(5)) // cursor between { and }
        val newVal = TextFieldValue("    {\n}", TextRange(6)) // soft keyboard pressed Enter
        val result = SmartEditProcessor.processChange(oldVal, newVal, isGoFile = false, tabSize = 4)

        val expected = "    {\n        \n    }"
        assertEquals(expected, result.text)
        // Cursor should sit on the blank line at indentation (14)
        val expectedCursor = "    {\n        ".length
        assertEquals(TextRange(expectedCursor), result.selection)
    }

    // 8. Smart de-indent when typing '}' on whitespace-only line
    @Test
    fun testSmartDeindentOnClosingBrace() {
        val oldVal = TextFieldValue("        ", TextRange(8)) // 8 spaces
        val newVal = TextFieldValue("        }", TextRange(9)) // user typed }
        val result = SmartEditProcessor.processChange(oldVal, newVal, tabSize = 4)

        assertEquals("    }", result.text)
        assertEquals(TextRange(5), result.selection)
    }

    // 9. Create a .go file → the indentation uses a tab character.
    @Test
    fun testGoTemplateUsesTab() {
        val res = FileTemplates.getTemplateForFile("server.go")
        assertNotNull(res)
        assertTrue("Go template must contain tab indentation", res!!.content.contains("\t"))
        assertTrue("func main() must be indented with tab", res.content.contains("func main() {\n\t\n}"))
    }

    // 10. Create index.html → the head contains a link to index.css.
    @Test
    fun testHtmlTemplateLinksToCss() {
        val res = FileTemplates.getTemplateForFile("index.html")
        assertNotNull(res)
        assertTrue(res!!.content.contains("<title>index</title>"))
        assertTrue(res.content.contains("<link rel=\"stylesheet\" href=\"index.css\">"))
    }

    // Matching bracket highlight test
    @Test
    fun testMatchingBracketHighlight() {
        val text = "fun foo(x: Int) { return [1, 2] }"
        val openParen = text.indexOf('(')
        val closeParen = text.indexOf(')')

        val matches = SmartEditProcessor.findMatchingBrackets(text, openParen + 1)
        assertEquals(setOf(openParen, closeParen), matches)
    }
}
