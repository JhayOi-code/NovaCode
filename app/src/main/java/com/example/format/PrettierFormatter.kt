package com.example.format

import org.json.JSONArray
import org.json.JSONObject

object PrettierFormatter {

    fun format(content: String, language: String): String {
        if (content.isBlank()) return content

        return when (language.lowercase().trim()) {
            "html", "htm", "html / web", "xml" -> formatHtml(content)
            "css", "scss", "less" -> formatCss(content)
            "json" -> formatJson(content)
            "javascript", "js", "typescript", "ts" -> formatJavaScript(content)
            "python", "py" -> formatPython(content)
            "kotlin", "kt", "java" -> formatJavaOrKotlin(content)
            "c", "cpp", "c++", "rust", "rs", "go" -> formatCStyle(content)
            "sql" -> formatSql(content)
            else -> formatGeneric(content)
        }
    }

    private fun formatJson(content: String): String {
        val trimmed = content.trim()
        return try {
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                obj.toString(2)
            } else if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                arr.toString(2)
            } else {
                formatGeneric(content)
            }
        } catch (_: Exception) {
            formatGeneric(content)
        }
    }

    private fun formatHtml(content: String): String {
        val lines = content.lines()
        val result = StringBuilder()
        var indentLevel = 0
        val voidTags = setOf(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr", "!doctype"
        )

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                if (result.isNotEmpty() && !result.endsWith("\n\n")) {
                    result.append("\n")
                }
                continue
            }

            // Check if line starts with closing tag
            val isClosing = line.startsWith("</")
            if (isClosing && indentLevel > 0) {
                indentLevel--
            }

            val indent = "  ".repeat(indentLevel)
            result.append(indent).append(line).append("\n")

            // Count open and close tags on this line
            val openTagMatches = Regex("<([a-zA-Z0-9]+)[^>]*>").findAll(line)
            for (match in openTagMatches) {
                val tagName = match.groupValues[1].lowercase()
                val fullMatch = match.value
                val isSelfClosing = fullMatch.endsWith("/>") || voidTags.contains(tagName)
                if (!isSelfClosing && !line.contains("</$tagName>")) {
                    indentLevel++
                }
            }
        }
        return result.toString().trimEnd() + "\n"
    }

    private fun formatCss(content: String): String {
        val preprocessed = content
            .replace("{", " {\n")
            .replace("}", "\n}\n")
            .replace(";", ";\n")
        val lines = preprocessed.lines()
        val result = StringBuilder()
        var indentLevel = 0

        for (rawLine in lines) {
            var line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("}")) {
                if (indentLevel > 0) indentLevel--
            }

            // Format property: value;
            if (line.contains(":") && !line.startsWith("@") && !line.endsWith("{")) {
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    val prop = parts[0].trim()
                    var value = parts[1].trim()
                    if (!value.endsWith(";") && !value.endsWith("}")) {
                        value += ";"
                    }
                    line = "$prop: $value"
                }
            }

            val indent = "  ".repeat(indentLevel)
            result.append(indent).append(line).append("\n")

            if (line.endsWith("{")) {
                indentLevel++
            }
        }
        return result.toString().trimEnd() + "\n"
    }

    private fun formatJavaScript(content: String): String {
        val lines = content.lines()
        val result = StringBuilder()
        var indentLevel = 0

        for (rawLine in lines) {
            var line = rawLine.trim()
            if (line.isEmpty()) {
                if (result.isNotEmpty() && !result.endsWith("\n\n")) {
                    result.append("\n")
                }
                continue
            }

            if (line.startsWith("}") || line.startsWith(")") || line.startsWith("]")) {
                if (indentLevel > 0) indentLevel--
            }

            val indent = "  ".repeat(indentLevel)
            result.append(indent).append(line).append("\n")

            val openCount = line.count { it == '{' || it == '[' }
            val closeCount = line.count { it == '}' || it == ']' }
            val diff = openCount - closeCount
            if (diff > 0) {
                indentLevel += diff
            }
        }
        return result.toString().trimEnd() + "\n"
    }

    private fun formatPython(content: String): String {
        val lines = content.lines()
        val result = StringBuilder()
        var indentLevel = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                if (result.isNotEmpty() && !result.endsWith("\n\n")) {
                    result.append("\n")
                }
                continue
            }

            // Simple python indentation tracker
            if (line.startsWith("return") || line.startsWith("pass") || line.startsWith("break") || line.startsWith("continue")) {
                val indent = "    ".repeat(indentLevel)
                result.append(indent).append(line).append("\n")
                if (indentLevel > 0 && !line.endsWith(":")) {
                    indentLevel--
                }
                continue
            }

            if (line.startsWith("elif ") || line.startsWith("else:") || line.startsWith("except") || line.startsWith("finally:")) {
                val currentIndent = maxOf(0, indentLevel - 1)
                val indent = "    ".repeat(currentIndent)
                result.append(indent).append(line).append("\n")
                if (line.endsWith(":")) {
                    indentLevel = currentIndent + 1
                }
                continue
            }

            val indent = "    ".repeat(indentLevel)
            result.append(indent).append(line).append("\n")

            if (line.endsWith(":")) {
                indentLevel++
            }
        }
        return result.toString().trimEnd() + "\n"
    }

    private fun formatJavaOrKotlin(content: String): String {
        return formatJavaScript(content)
    }

    private fun formatCStyle(content: String): String {
        return formatJavaScript(content)
    }

    private fun formatSql(content: String): String {
        val keywords = listOf(
            "SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "HAVING",
            "LIMIT", "OFFSET", "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN",
            "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM",
            "CREATE TABLE", "DROP TABLE", "ALTER TABLE", "AND", "OR"
        )
        var formatted = content
        for (kw in keywords) {
            val pattern = Regex("(?i)\\b${Regex.escape(kw)}\\b")
            formatted = pattern.replace(formatted, kw)
        }
        return formatGeneric(formatted)
    }

    private fun formatGeneric(content: String): String {
        return content.lines()
            .map { it.trimEnd() }
            .joinToString("\n")
            .trim() + "\n"
    }
}
