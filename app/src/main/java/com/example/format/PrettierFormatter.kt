package com.example.format

object PrettierFormatter {

    fun format(content: String, language: String): String {
        if (content.isBlank()) return content

        val lines = content.lines()
        val result = StringBuilder()
        var indentLevel = 0
        val indentUnit = "    " // 4 spaces

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                result.append("\n")
                continue
            }

            // Decrement indent if line starts with closing brace
            val startsWithClosing = trimmed.startsWith("}") || trimmed.startsWith(")") || trimmed.startsWith("]")
            val effectiveIndent = if (startsWithClosing) maxOf(0, indentLevel - 1) else indentLevel

            // Build line with indentation
            for (i in 0 until effectiveIndent) {
                result.append(indentUnit)
            }
            result.append(trimmed).append("\n")

            // Adjust indentLevel for next lines based on net braces
            val openCount = trimmed.count { it == '{' || it == '(' || it == '[' }
            val closeCount = trimmed.count { it == '}' || it == ')' || it == ']' }
            val netChange = openCount - closeCount
            indentLevel = maxOf(0, indentLevel + netChange)
        }

        return result.toString().trimEnd() + "\n"
    }
}
