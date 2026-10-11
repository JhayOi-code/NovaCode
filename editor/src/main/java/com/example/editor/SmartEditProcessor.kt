package com.example.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Smart Editor Assistant handling:
 * 1. Auto-closing pairs: () [] {} "" ''
 * 2. Skipping over closing characters
 * 3. Backspace on empty pairs to delete both
 * 4. Smart Enter (auto-indent, brace expansion)
 * 5. Smart de-indent on typing '}'
 * 6. Matching bracket highlighter
 */
object SmartEditProcessor {

    private val PAIRS = mapOf(
        '(' to ')',
        '[' to ']',
        '{' to '}',
        '"' to '"',
        '\'' to '\''
    )

    private val CLOSING_CHARS = setOf(')', ']', '}', '"', '\'')

    /**
     * Intercepts and processes text changes from soft keyboard input.
     */
    fun processChange(
        oldValue: TextFieldValue,
        newValue: TextFieldValue,
        isGoFile: Boolean = false,
        tabSize: Int = 4
    ): TextFieldValue {
        val oldText = oldValue.text
        val newText = newValue.text

        // If text is unchanged (only cursor moved or selection changed), return as is
        if (oldText == newText) return newValue

        val oldLen = oldText.length
        val newLen = newText.length
        val oldStart = oldValue.selection.min
        val oldEnd = oldValue.selection.max

        // ---------------------------------------------------------
        // 1. Backspace on empty pair: deletes both opening & closing
        // ---------------------------------------------------------
        if (oldValue.selection.collapsed && newLen == oldLen - 1 && oldStart > 0 && oldStart < oldLen) {
            val leftChar = oldText[oldStart - 1]
            val rightChar = oldText[oldStart]
            if (PAIRS[leftChar] == rightChar) {
                // User deleted leftChar, rightChar is now at oldStart - 1
                if (newValue.selection.start == oldStart - 1 &&
                    newValue.text.getOrNull(oldStart - 1) == rightChar
                ) {
                    val textWithoutPair = oldText.substring(0, oldStart - 1) + oldText.substring(oldStart + 1)
                    return TextFieldValue(textWithoutPair, TextRange(oldStart - 1))
                }
            }
        }

        // Only process single-character additions for smart pairs and smart enter
        val replacedLength = oldEnd - oldStart
        val addedLength = newLen - (oldLen - replacedLength)
        if (addedLength != 1) {
            return newValue
        }

        val typedChar = newText[oldStart]

        // ---------------------------------------------------------
        // 2. Smart Enter (auto-indent & brace expansion)
        // ---------------------------------------------------------
        if (typedChar == '\n') {
            // Check if Enter pressed directly between '{' and '}'
            if (oldValue.selection.collapsed && oldStart > 0 && oldStart < oldLen &&
                oldText[oldStart - 1] == '{' && oldText[oldStart] == '}'
            ) {
                val lineStart = oldText.lastIndexOf('\n', oldStart - 1) + 1
                val baseIndent = oldText.substring(lineStart, oldStart - 1).takeWhile { it == ' ' || it == '\t' }
                val indentUnit = if (isGoFile) "\t" else " ".repeat(tabSize)

                val textBefore = oldText.substring(0, oldStart)
                val textAfter = oldText.substring(oldStart)
                val middle = "\n" + baseIndent + indentUnit + "\n" + baseIndent
                val fullText = textBefore + middle + textAfter
                val cursorPos = textBefore.length + 1 + baseIndent.length + indentUnit.length

                return TextFieldValue(fullText, TextRange(cursorPos))
            }

            // Normal Smart Enter: keep line indentation + add indent if ending with '{', '(', '[', ':'
            val lineStart = oldText.lastIndexOf('\n', oldStart - 1) + 1
            val lineBeforeCursor = oldText.substring(lineStart, oldStart)
            val baseIndent = lineBeforeCursor.takeWhile { it == ' ' || it == '\t' }
            val trimmed = lineBeforeCursor.trimEnd()
            val shouldAddIndent = trimmed.endsWith('{') || trimmed.endsWith('(') || trimmed.endsWith('[') || trimmed.endsWith(':')
            val extraIndent = if (shouldAddIndent) {
                if (isGoFile) "\t" else " ".repeat(tabSize)
            } else ""

            val totalIndent = baseIndent + extraIndent
            val textBefore = oldText.substring(0, oldStart)
            val textAfter = oldText.substring(oldEnd)
            val fullText = textBefore + "\n" + totalIndent + textAfter
            val cursorPos = textBefore.length + 1 + totalIndent.length

            return TextFieldValue(fullText, TextRange(cursorPos))
        }

        // ---------------------------------------------------------
        // 3. Skip-over existing closing character
        // ---------------------------------------------------------
        if (typedChar in CLOSING_CHARS && oldValue.selection.collapsed && oldStart < oldLen) {
            val charAfterCursor = oldText[oldStart]
            if (typedChar == charAfterCursor) {
                // Simply advance cursor over existing closing character
                return TextFieldValue(oldText, TextRange(oldStart + 1))
            }
        }

        // ---------------------------------------------------------
        // 4. Typing '}' on a whitespace-only line de-indents one level
        // ---------------------------------------------------------
        if (typedChar == '}') {
            val lineStart = oldText.lastIndexOf('\n', oldStart - 1) + 1
            val lineBeforeCursor = oldText.substring(lineStart, oldStart)
            if (lineBeforeCursor.isNotEmpty() && lineBeforeCursor.all { it == ' ' || it == '\t' }) {
                val deindented = when {
                    lineBeforeCursor.endsWith('\t') -> lineBeforeCursor.dropLast(1)
                    lineBeforeCursor.endsWith(" ".repeat(tabSize)) -> lineBeforeCursor.dropLast(tabSize)
                    else -> {
                        val spaceCount = lineBeforeCursor.takeLastWhile { it == ' ' }.length
                        lineBeforeCursor.dropLast(spaceCount.coerceAtMost(tabSize))
                    }
                }
                val textBeforeLine = oldText.substring(0, lineStart)
                val textAfter = oldText.substring(oldEnd)
                val fullText = textBeforeLine + deindented + "}" + textAfter
                val cursorPos = textBeforeLine.length + deindented.length + 1
                return TextFieldValue(fullText, TextRange(cursorPos))
            }
        }

        // ---------------------------------------------------------
        // 5. Auto-closing pairs: ( [ { " '
        // ---------------------------------------------------------
        val closingPartner = PAIRS[typedChar]
        if (closingPartner != null) {
            // Quotes constraints: do not auto-pair inside a word or when next to alphanumeric
            if (typedChar == '"' || typedChar == '\'') {
                if (oldStart > 0) {
                    val prevChar = oldText[oldStart - 1]
                    if (prevChar.isLetterOrDigit() || prevChar == '_' || prevChar == '\\') {
                        return newValue
                    }
                }
                if (oldStart < oldLen) {
                    val nextChar = oldText[oldStart]
                    if (nextChar.isLetterOrDigit() || nextChar == '_') {
                        return newValue
                    }
                }
            }

            val textBefore = oldText.substring(0, oldStart)
            val textAfter = oldText.substring(oldEnd)
            val fullText = textBefore + typedChar + closingPartner + textAfter
            val cursorPos = oldStart + 1

            return TextFieldValue(fullText, TextRange(cursorPos))
        }

        return newValue
    }

    /**
     * Programmatic symbol insertion (used by extra-keys bar / quick keys toolbar).
     */
    fun insertSymbol(
        current: TextFieldValue,
        symbol: String,
        isGoFile: Boolean = false,
        tabSize: Int = 4
    ): TextFieldValue {
        val sel = current.selection
        val currentText = current.text
        val start = sel.min
        val end = sel.max

        // Tab insertion
        if (symbol == "Tab" || symbol == "    " || symbol == "\t") {
            val indent = if (isGoFile) "\t" else " ".repeat(tabSize)
            val updated = currentText.substring(0, start) + indent + currentText.substring(end)
            return TextFieldValue(updated, TextRange(start + indent.length))
        }

        // Single character symbols -> route through processChange
        if (symbol.length == 1) {
            val syntheticNewText = currentText.substring(0, start) + symbol + currentText.substring(end)
            val syntheticNewValue = TextFieldValue(syntheticNewText, TextRange(start + 1))
            return processChange(current, syntheticNewValue, isGoFile, tabSize)
        }

        // Multi-character string insertion
        val updated = currentText.substring(0, start) + symbol + currentText.substring(end)
        return TextFieldValue(updated, TextRange(start + symbol.length))
    }

    /**
     * Finds indices of the bracket adjacent to the cursor and its matching pair.
     * Highlights bracket matching in real-time.
     */
    fun findMatchingBrackets(text: String, cursorIndex: Int): Set<Int> {
        if (text.isEmpty() || cursorIndex < 0 || cursorIndex > text.length) return emptySet()

        // Test character to the left of cursor first, then right of cursor
        val candidates = listOfNotNull(
            if (cursorIndex > 0) cursorIndex - 1 else null,
            if (cursorIndex < text.length) cursorIndex else null
        )

        for (idx in candidates) {
            val ch = text[idx]
            val matchIdx = when (ch) {
                '(' -> scanForward(text, idx, '(', ')')
                '[' -> scanForward(text, idx, '[', ']')
                '{' -> scanForward(text, idx, '{', '}')
                ')' -> scanBackward(text, idx, '(', ')')
                ']' -> scanBackward(text, idx, '[', ']')
                '}' -> scanBackward(text, idx, '{', '}')
                else -> -1
            }
            if (matchIdx != -1) {
                return setOf(idx, matchIdx)
            }
        }

        return emptySet()
    }

    private fun scanForward(text: String, startIdx: Int, openChar: Char, closeChar: Char): Int {
        var depth = 1
        for (i in (startIdx + 1) until text.length) {
            val c = text[i]
            if (c == openChar) depth++
            else if (c == closeChar) {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }

    private fun scanBackward(text: String, startIdx: Int, openChar: Char, closeChar: Char): Int {
        var depth = 1
        for (i in (startIdx - 1) downTo 0) {
            val c = text[i]
            if (c == closeChar) depth++
            else if (c == openChar) {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }
}
