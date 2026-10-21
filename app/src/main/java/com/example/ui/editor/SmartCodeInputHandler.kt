package com.example.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object SmartCodeInputHandler {

    private val pairOpenToClose = mapOf(
        '{' to '}',
        '(' to ')',
        '[' to ']',
        '"' to '"',
        '\'' to '\''
    )

    private val closingChars = setOf('}', ')', ']', '"', '\'')

    /**
     * Intercepts and transforms text field modifications to support:
     * - Auto-closing pairs: { -> {}, ( -> (), [ -> [], " -> "", ' -> ''
     * - Cursor placed between the pair
     * - Typing closing character when already next to cursor skips over it
     * - Backspace inside an empty pair deletes both characters
     * - Smart Enter between pairs: new indented line + closing char on original indent
     * - Keeps previous line's indentation on every normal Enter
     */
    fun handleTextChange(
        oldValue: TextFieldValue,
        newValue: TextFieldValue
    ): TextFieldValue {
        val oldText = oldValue.text
        val newText = newValue.text
        val oldCursor = oldValue.selection.start
        val newCursor = newValue.selection.start

        // 1. Backspace check: deleted 1 char?
        if (newText.length == oldText.length - 1 && oldCursor > 0 && oldCursor == oldValue.selection.end) {
            val deletedChar = oldText.getOrNull(oldCursor - 1)
            val charAfterDeleted = oldText.getOrNull(oldCursor)
            if (deletedChar != null && charAfterDeleted != null) {
                val expectedClosing = pairOpenToClose[deletedChar]
                if (expectedClosing == charAfterDeleted) {
                    // Backspace inside empty pair deletes BOTH!
                    val resultText = oldText.substring(0, oldCursor - 1) + oldText.substring(oldCursor + 1)
                    val newPos = oldCursor - 1
                    return TextFieldValue(resultText, TextRange(newPos))
                }
            }
            return newValue
        }

        // 2. Single character added?
        if (newText.length == oldText.length + 1 && newCursor > 0) {
            val addedChar = newText[newCursor - 1]

            // Check Enter key
            if (addedChar == '\n') {
                return handleEnterKey(oldText, oldCursor)
            }

            // Check if user typed a closing character that is already next to cursor -> skip over it!
            if (closingChars.contains(addedChar)) {
                val charRightOfOldCursor = oldText.getOrNull(oldCursor)
                if (charRightOfOldCursor == addedChar) {
                    // Skip over instead of duplicating
                    return TextFieldValue(oldText, TextRange(oldCursor + 1))
                }
            }

            // Check if user typed an opening character -> auto-close!
            val closing = pairOpenToClose[addedChar]
            if (closing != null) {
                // If it's quotes, check if we're skipping over existing quote
                val charRightOfOld = oldText.getOrNull(oldCursor)
                if ((addedChar == '"' || addedChar == '\'') && charRightOfOld == addedChar) {
                    return TextFieldValue(oldText, TextRange(oldCursor + 1))
                }

                // Insert pair and place cursor between them
                val before = oldText.substring(0, oldCursor)
                val after = oldText.substring(oldCursor)
                val combined = before + addedChar + closing + after
                return TextFieldValue(combined, TextRange(oldCursor + 1))
            }
        }

        return newValue
    }

    /**
     * Smart Enter handling:
     * When cursor is between {|} or (|) or [|]:
     * newline + indent + newline + original indent + closing
     * Example:
     * public static void main(String[] args) {|}
     * After:
     * public static void main(String[] args) {
     *     |
     * }
     */
    fun handleEnterKey(text: String, cursor: Int): TextFieldValue {
        val before = text.substring(0, cursor)
        val after = text.substring(cursor)

        // Find indentation of current line
        val lastNewline = before.lastIndexOf('\n')
        val currentLineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val currentLineText = before.substring(currentLineStart)
        val indent = currentLineText.takeWhile { it == ' ' || it == '\t' }

        val charBefore = before.lastOrNull()
        val charAfter = after.firstOrNull()

        val isBetweenPair = (charBefore == '{' && charAfter == '}') ||
                (charBefore == '(' && charAfter == ')') ||
                (charBefore == '[' && charAfter == ']')

        return if (isBetweenPair) {
            val innerIndent = indent + "    " // 4 spaces
            val newText = before + "\n" + innerIndent + "\n" + indent + after
            val newCursorPos = before.length + 1 + innerIndent.length
            TextFieldValue(newText, TextRange(newCursorPos))
        } else {
            // Keep previous line's indentation
            val extraIndent = if (charBefore == '{' || charBefore == ':') "    " else ""
            val fullIndent = indent + extraIndent
            val newText = before + "\n" + fullIndent + after
            val newCursorPos = before.length + 1 + fullIndent.length
            TextFieldValue(newText, TextRange(newCursorPos))
        }
    }

    /**
     * Handles toolbar symbol taps following the same rules:
     * {} if = : && "" () []
     */
    fun insertToolbarSymbol(currentValue: TextFieldValue, symbol: String): TextFieldValue {
        val text = currentValue.text
        val cursor = currentValue.selection.start
        val before = text.substring(0, cursor)
        val after = text.substring(cursor)

        return when (symbol) {
            "{}" -> {
                val newText = "$before{}$after"
                TextFieldValue(newText, TextRange(cursor + 1))
            }
            "()" -> {
                val newText = "$before()$after"
                TextFieldValue(newText, TextRange(cursor + 1))
            }
            "[]" -> {
                val newText = "$before[]$after"
                TextFieldValue(newText, TextRange(cursor + 1))
            }
            "\"\"" -> {
                val newText = "$before\"\"$after"
                TextFieldValue(newText, TextRange(cursor + 1))
            }
            "''" -> {
                val newText = "$before''$after"
                TextFieldValue(newText, TextRange(cursor + 1))
            }
            "if" -> {
                val snippet = "if () {\n    \n}"
                val newText = before + snippet + after
                TextFieldValue(newText, TextRange(cursor + 4)) // inside ()
            }
            "&&" -> {
                val newText = "$before && $after"
                TextFieldValue(newText, TextRange(cursor + 4))
            }
            "=" -> {
                val newText = "$before = $after"
                TextFieldValue(newText, TextRange(cursor + 3))
            }
            ":" -> {
                val newText = "$before: $after"
                TextFieldValue(newText, TextRange(cursor + 2))
            }
            ";" -> {
                val newText = "$before;$after"
                TextFieldValue(newText, TextRange(cursor + 1))
            }
            "Tab" -> {
                val newText = "$before    $after"
                TextFieldValue(newText, TextRange(cursor + 4))
            }
            else -> {
                val newText = before + symbol + after
                TextFieldValue(newText, TextRange(cursor + symbol.length))
            }
        }
    }
}
