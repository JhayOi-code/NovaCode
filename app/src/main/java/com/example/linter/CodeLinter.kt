package com.example.linter

enum class LintSeverity {
    ERROR,
    WARNING,
    INFO
}

data class LintIssue(
    val line: Int, // 1-indexed
    val column: Int = 1,
    val message: String,
    val severity: LintSeverity = LintSeverity.ERROR,
    val rule: String = "syntax"
)

object CodeLinter {

    fun lint(code: String, language: String): List<LintIssue> {
        val normalized = language.lowercase().trim()
        return when {
            normalized.contains("json") -> {
                lintJson(code)
            }
            normalized.contains("css") -> {
                lintCss(code)
            }
            normalized.contains("javascript") || normalized.contains("typescript") || normalized == "js" || normalized == "ts" -> {
                lintJavaScript(code)
            }
            normalized.contains("html") || normalized.contains("web") -> {
                lintHtml(code)
            }
            else -> emptyList()
        }
    }

    // ==========================================
    // 1. JAVASCRIPT / TYPESCRIPT LINTER
    // ==========================================
    private fun lintJavaScript(code: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val lines = code.lines()

        val bracketStack = ArrayDeque<Pair<Char, Int>>() // char, lineNum

        var inBlockComment = false

        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = line.trim()

            if (inBlockComment) {
                if (trimmed.contains("*/")) {
                    inBlockComment = false
                }
                continue
            }

            if (trimmed.startsWith("/*")) {
                if (!trimmed.contains("*/")) {
                    inBlockComment = true
                }
                continue
            }

            if (trimmed.isEmpty() || trimmed.startsWith("//")) continue

            // 1. Check for unclosed string literal on single line (quotes that are not escaped)
            var inSingle = false
            var inDouble = false
            var inTemplate = false
            var escaped = false

            for (ch in line) {
                if (escaped) {
                    escaped = false
                    continue
                }
                if (ch == '\\') {
                    escaped = true
                    continue
                }
                if (ch == '\'' && !inDouble && !inTemplate) inSingle = !inSingle
                else if (ch == '"' && !inSingle && !inTemplate) inDouble = !inDouble
                else if (ch == '`' && !inSingle && !inDouble) inTemplate = !inTemplate
                else if (!inSingle && !inDouble && !inTemplate) {
                    if (ch == '{' || ch == '(' || ch == '[') {
                        bracketStack.addLast(Pair(ch, lineNum))
                    } else if (ch == '}' || ch == ')' || ch == ']') {
                        if (bracketStack.isEmpty()) {
                            issues.add(
                                LintIssue(
                                    line = lineNum,
                                    message = "Unexpected closing bracket '$ch' with no matching opening bracket",
                                    severity = LintSeverity.ERROR,
                                    rule = "no-unmatched-brackets"
                                )
                            )
                        } else {
                            val (topChar, _) = bracketStack.removeLast()
                            val matches = (topChar == '{' && ch == '}') ||
                                    (topChar == '(' && ch == ')') ||
                                    (topChar == '[' && ch == ']')
                            if (!matches) {
                                issues.add(
                                    LintIssue(
                                        line = lineNum,
                                        message = "Mismatched closing bracket '$ch' for opening '$topChar'",
                                        severity = LintSeverity.ERROR,
                                        rule = "bracket-mismatch"
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (inSingle) {
                issues.add(LintIssue(line = lineNum, message = "Unterminated single-quoted string literal", severity = LintSeverity.ERROR, rule = "unterminated-string"))
            }
            if (inDouble) {
                issues.add(LintIssue(line = lineNum, message = "Unterminated double-quoted string literal", severity = LintSeverity.ERROR, rule = "unterminated-string"))
            }

            // 2. Missing initializer in const
            if (trimmed.startsWith("const ") && !trimmed.contains("=") && (trimmed.endsWith(";") || !trimmed.endsWith(","))) {
                issues.add(LintIssue(line = lineNum, message = "Missing initializer in 'const' declaration", severity = LintSeverity.ERROR, rule = "const-initializer"))
            }

            // 3. Double assignment or broken equality (e.g., === =)
            if (trimmed.contains("=== =") || trimmed.contains("== =")) {
                issues.add(LintIssue(line = lineNum, message = "Syntax error in operator expression", severity = LintSeverity.ERROR, rule = "invalid-operator"))
            }

            // 4. Broken arrow function syntax
            if (trimmed.contains("= >")) {
                issues.add(LintIssue(line = lineNum, message = "Unexpected space in arrow function operator '=>'", severity = LintSeverity.WARNING, rule = "arrow-spacing"))
            }

            // 5. Duplicate consecutive commas (e.g. func(a,, b))
            if (trimmed.contains(",,")) {
                issues.add(LintIssue(line = lineNum, message = "Unexpected consecutive commas", severity = LintSeverity.ERROR, rule = "no-consecutive-commas"))
            }
        }

        // Remaining unclosed brackets
        while (bracketStack.isNotEmpty()) {
            val (openChar, openLine) = bracketStack.removeLast()
            val expected = when (openChar) {
                '{' -> '}'
                '(' -> ')'
                '[' -> ']'
                else -> ' '
            }
            issues.add(
                LintIssue(
                    line = openLine,
                    message = "Unclosed '$openChar' - missing matching '$expected'",
                    severity = LintSeverity.ERROR,
                    rule = "unclosed-bracket"
                )
            )
        }

        return issues
    }

    // ==========================================
    // 2. CSS LINTER
    // ==========================================
    private fun lintCss(code: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val lines = code.lines()

        var insideBlock = false
        var blockStartLine = 1
        var inComment = false

        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = line.trim()

            if (inComment) {
                if (trimmed.contains("*/")) inComment = false
                continue
            }
            if (trimmed.startsWith("/*")) {
                if (!trimmed.contains("*/")) inComment = true
                continue
            }

            if (trimmed.isEmpty()) continue

            if (trimmed.contains("{")) {
                if (insideBlock) {
                    issues.add(LintIssue(line = lineNum, message = "Unexpected nested '{' without closing previous rule block", severity = LintSeverity.WARNING, rule = "css-nested-rules"))
                }
                insideBlock = true
                blockStartLine = lineNum
            }

            if (trimmed.contains("}")) {
                if (!insideBlock) {
                    issues.add(LintIssue(line = lineNum, message = "Unexpected closing '}' without matching selector block", severity = LintSeverity.ERROR, rule = "css-unmatched-brace"))
                }
                insideBlock = false
            }

            // Inside a CSS declaration block
            if (insideBlock && !trimmed.contains("{") && !trimmed.contains("}")) {
                // Must have a colon separating property and value
                if (!trimmed.contains(":") && !trimmed.startsWith("@") && !trimmed.startsWith("/*")) {
                    issues.add(LintIssue(line = lineNum, message = "Missing colon ':' in CSS declaration (format: property: value;)", severity = LintSeverity.ERROR, rule = "css-missing-colon"))
                } else if (trimmed.contains(":")) {
                    // Check for missing semicolon if there's a subsequent property on the next non-empty line
                    val hasNextProp = (idx + 1 until lines.size).any {
                        val nextTrim = lines[it].trim()
                        nextTrim.isNotEmpty() && !nextTrim.contains("}") && nextTrim.contains(":")
                    }
                    if (!trimmed.endsWith(";") && hasNextProp) {
                        issues.add(LintIssue(line = lineNum, message = "Missing semicolon ';' at end of CSS declaration", severity = LintSeverity.WARNING, rule = "css-missing-semicolon"))
                    }
                }
            }
        }

        if (insideBlock) {
            issues.add(LintIssue(line = blockStartLine, message = "Unclosed CSS rule block - missing '}'", severity = LintSeverity.ERROR, rule = "css-unclosed-block"))
        }

        return issues
    }

    // ==========================================
    // 3. JSON LINTER
    // ==========================================
    private fun lintJson(code: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return issues

        val lines = code.lines()
        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val t = line.trim()
            if (t.isEmpty()) continue

            // Check for unquoted key: word:
            if (t.contains(":") && !t.startsWith("\"") && !t.startsWith("{") && !t.startsWith("[")) {
                val key = t.substringBefore(":").trim()
                if (!key.startsWith("\"") && key.isNotEmpty() && !key.startsWith("//")) {
                    issues.add(LintIssue(line = lineNum, message = "JSON keys must be enclosed in double quotes", severity = LintSeverity.ERROR, rule = "json-key-quotes"))
                }
            }

            // Check for single quotes
            if (t.contains("'")) {
                issues.add(LintIssue(line = lineNum, message = "Invalid single quote in JSON. JSON requires double quotes '\"'", severity = LintSeverity.ERROR, rule = "json-no-single-quotes"))
            }
        }

        return issues
    }

    // ==========================================
    // 4. HTML LINTER
    // ==========================================
    private fun lintHtml(code: String): List<LintIssue> {
        val issues = mutableListOf<LintIssue>()
        val lines = code.lines()
        val voidTags = setOf("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr")

        val tagStack = ArrayDeque<Pair<String, Int>>()

        for ((idx, line) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = line.trim()
            if (trimmed.startsWith("<!--") || trimmed.startsWith("<!DOCTYPE")) continue

            val tagRegex = Regex("</?([a-zA-Z0-9\\-]+)([^>]*)>")
            for (match in tagRegex.findAll(line)) {
                val full = match.value
                val tagName = match.groupValues[1].lowercase()
                val isClosing = full.startsWith("</")
                val isSelfClosing = full.endsWith("/>") || tagName in voidTags

                if (isClosing) {
                    if (tagStack.isEmpty()) {
                        issues.add(LintIssue(line = lineNum, message = "Unexpected closing tag '</$tagName>' without opening tag", severity = LintSeverity.ERROR, rule = "html-unmatched-closing-tag"))
                    } else {
                        val (topTag, _) = tagStack.removeLast()
                        if (topTag != tagName) {
                            issues.add(LintIssue(line = lineNum, message = "Mismatched closing tag '</$tagName>', expected '</$topTag>'", severity = LintSeverity.ERROR, rule = "html-tag-mismatch"))
                        }
                    }
                } else if (!isSelfClosing) {
                    tagStack.addLast(Pair(tagName, lineNum))
                }
            }
        }

        while (tagStack.isNotEmpty()) {
            val (openTag, openLine) = tagStack.removeLast()
            if (openTag !in setOf("html", "body")) {
                issues.add(LintIssue(line = openLine, message = "Unclosed HTML tag '<$openTag>' - missing closing tag '</$openTag>'", severity = LintSeverity.WARNING, rule = "html-unclosed-tag"))
            }
        }

        return issues
    }
}
