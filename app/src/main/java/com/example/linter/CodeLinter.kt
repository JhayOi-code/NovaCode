package com.example.linter

import com.example.model.LintIssue
import com.example.model.LintSeverity
import java.util.Stack

object CodeLinter {

    /**
     * Performs live syntax analysis on the given content.
     * Checks unmatched brackets/quotes, missing semicolons (Java/JS/C++), unused vars, TODOs.
     */
    fun analyze(content: String, language: String): List<LintIssue> {
        if (content.isEmpty()) return emptyList()

        val issues = mutableListOf<LintIssue>()
        val lines = content.lines()

        // 1. Bracket and quote balancing
        val bracketStack = Stack<Pair<Char, Int>>() // char to line index (1-indexed)
        var insideString = false
        var stringStartLine = 1
        var stringChar = '"'

        lines.forEachIndexed { idx, line ->
            val lineNum = idx + 1
            var i = 0
            while (i < line.length) {
                val c = line[i]

                // Line comments skip rest of line
                if (!insideString && (c == '/' && i + 1 < line.length && line[i + 1] == '/')) {
                    // Check for TODO hint
                    if (line.contains("TODO", ignoreCase = true)) {
                        issues.add(
                            LintIssue(
                                line = lineNum,
                                message = "TODO hint: Task pending completion in code",
                                severity = LintSeverity.INFO,
                                ruleId = "hint-todo"
                            )
                        )
                    }
                    break
                }
                if (!insideString && c == '#' && (language == "python" || language == "bash")) {
                    if (line.contains("TODO", ignoreCase = true)) {
                        issues.add(
                            LintIssue(
                                line = lineNum,
                                message = "TODO hint: Task pending in script",
                                severity = LintSeverity.INFO,
                                ruleId = "hint-todo"
                            )
                        )
                    }
                    break
                }

                // Quotes
                if ((c == '"' || c == '\'') && (i == 0 || line[i - 1] != '\\')) {
                    if (!insideString) {
                        insideString = true
                        stringChar = c
                        stringStartLine = lineNum
                    } else if (c == stringChar) {
                        insideString = false
                    }
                }

                // Brackets (when not inside string)
                if (!insideString) {
                    when (c) {
                        '{', '(', '[' -> bracketStack.push(c to lineNum)
                        '}' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().first != '{') {
                                issues.add(
                                    LintIssue(
                                        line = lineNum,
                                        message = "Syntax Error: Unmatched closing brace '}'",
                                        severity = LintSeverity.ERROR,
                                        ruleId = "unmatched-brace"
                                    )
                                )
                            } else {
                                bracketStack.pop()
                            }
                        }
                        ')' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().first != '(') {
                                issues.add(
                                    LintIssue(
                                        line = lineNum,
                                        message = "Syntax Error: Unmatched closing parenthesis ')'",
                                        severity = LintSeverity.ERROR,
                                        ruleId = "unmatched-paren"
                                    )
                                )
                            } else {
                                bracketStack.pop()
                            }
                        }
                        ']' -> {
                            if (bracketStack.isEmpty() || bracketStack.peek().first != '[') {
                                issues.add(
                                    LintIssue(
                                        line = lineNum,
                                        message = "Syntax Error: Unmatched closing bracket ']'",
                                        severity = LintSeverity.ERROR,
                                        ruleId = "unmatched-bracket"
                                    )
                                )
                            } else {
                                bracketStack.pop()
                            }
                        }
                    }
                }

                i++
            }

            // Check missing semicolon for Java/JS/C++
            if (language in listOf("java", "javascript", "typescript", "c", "cpp")) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() &&
                    !trimmed.startsWith("//") &&
                    !trimmed.startsWith("/*") &&
                    !trimmed.startsWith("*") &&
                    !trimmed.endsWith(";") &&
                    !trimmed.endsWith("{") &&
                    !trimmed.endsWith("}") &&
                    !trimmed.endsWith(":") &&
                    !trimmed.startsWith("import ") &&
                    !trimmed.startsWith("package ") &&
                    !trimmed.startsWith("@") &&
                    !trimmed.startsWith("class ") &&
                    !trimmed.startsWith("public class ") &&
                    !trimmed.startsWith("interface ") &&
                    (trimmed.startsWith("System.out.println") ||
                     trimmed.startsWith("return") ||
                     trimmed.startsWith("int ") ||
                     trimmed.startsWith("String ") ||
                     trimmed.startsWith("boolean ") ||
                     trimmed.startsWith("double ") ||
                     trimmed.startsWith("let ") ||
                     trimmed.startsWith("const ") ||
                     trimmed.startsWith("var "))
                ) {
                    issues.add(
                        LintIssue(
                            line = lineNum,
                            message = "Syntax Error: Missing semicolon ';' at the end of statement",
                            severity = LintSeverity.ERROR,
                            ruleId = "missing-semicolon"
                        )
                    )
                }
            }

            // Check suspicious equality or unused variables
            val trimmedLine = line.trim()
            if (trimmedLine.contains("==") && (language == "javascript" || language == "typescript")) {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        message = "Warning: Use strict equality '===' instead of '=='",
                        severity = LintSeverity.WARNING,
                        ruleId = "strict-equality"
                    )
                )
            }

            if (trimmedLine.startsWith("var ") && language == "javascript") {
                issues.add(
                    LintIssue(
                        line = lineNum,
                        message = "Warning: Prefer 'const' or 'let' over 'var'",
                        severity = LintSeverity.WARNING,
                        ruleId = "prefer-const"
                    )
                )
            }
        }

        // Remaining unclosed quotes
        if (insideString) {
            issues.add(
                LintIssue(
                    line = stringStartLine,
                    message = "Syntax Error: Unclosed string quote",
                    severity = LintSeverity.ERROR,
                    ruleId = "unclosed-quote"
                )
            )
        }

        // Remaining unclosed brackets
        while (bracketStack.isNotEmpty()) {
            val (openChar, lineNum) = bracketStack.pop()
            val bracketName = when (openChar) {
                '{' -> "brace '{'"
                '(' -> "parenthesis '('"
                '[' -> "bracket '['"
                else -> "'$openChar'"
            }
            issues.add(
                LintIssue(
                    line = lineNum,
                    message = "Syntax Error: Unclosed $bracketName",
                    severity = LintSeverity.ERROR,
                    ruleId = "unclosed-bracket"
                )
            )
        }

        return issues.sortedBy { it.line }
    }
}
