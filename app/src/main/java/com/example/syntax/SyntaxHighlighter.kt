package com.example.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.model.EditorTheme
import com.example.model.SupportedLanguage
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val KEYWORDS_MAP = mapOf(
        SupportedLanguage.KOTLIN to setOf(
            "val", "var", "fun", "class", "interface", "object", "package", "import",
            "return", "if", "else", "when", "for", "while", "do", "try", "catch",
            "finally", "throw", "null", "true", "false", "this", "super", "override",
            "public", "private", "protected", "internal", "companion", "data", "enum",
            "sealed", "abstract", "open", "in", "is", "as", "by", "suspend", "inline",
            "reified", "typealias", "constructor", "init"
        ),
        SupportedLanguage.JAVA to setOf(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null", "record"
        ),
        SupportedLanguage.PYTHON to setOf(
            "def", "class", "import", "from", "as", "return", "if", "elif", "else",
            "for", "while", "break", "continue", "try", "except", "finally", "raise",
            "with", "yield", "lambda", "global", "nonlocal", "pass", "assert", "del",
            "in", "is", "not", "and", "or", "True", "False", "None", "async", "await"
        ),
        SupportedLanguage.JAVASCRIPT to setOf(
            "function", "const", "let", "var", "return", "if", "else", "for", "while",
            "do", "switch", "case", "break", "continue", "try", "catch", "finally",
            "throw", "class", "extends", "new", "this", "super", "import", "export",
            "default", "from", "as", "async", "await", "yield", "typeof", "instanceof",
            "void", "delete", "in", "of", "null", "undefined", "true", "false"
        ),
        SupportedLanguage.TYPESCRIPT to setOf(
            "function", "const", "let", "var", "return", "if", "else", "for", "while",
            "do", "switch", "case", "break", "continue", "try", "catch", "finally",
            "throw", "class", "extends", "new", "this", "super", "import", "export",
            "default", "from", "as", "async", "await", "yield", "typeof", "instanceof",
            "void", "delete", "in", "of", "null", "undefined", "true", "false",
            "interface", "type", "enum", "namespace", "declare", "abstract", "implements",
            "readonly", "keyof", "unique", "unknown", "never", "any"
        ),
        SupportedLanguage.CPP to setOf(
            "auto", "break", "case", "char", "const", "continue", "default", "do",
            "double", "else", "enum", "extern", "float", "for", "goto", "if", "int",
            "long", "register", "return", "short", "signed", "sizeof", "static",
            "struct", "switch", "typedef", "union", "unsigned", "void", "volatile",
            "while", "class", "namespace", "template", "typename", "using", "public",
            "private", "protected", "virtual", "friend", "inline", "new", "delete",
            "operator", "this", "throw", "try", "catch", "constexpr", "nullptr", "bool"
        ),
        SupportedLanguage.RUST to setOf(
            "fn", "let", "mut", "pub", "struct", "enum", "impl", "trait", "type",
            "match", "if", "else", "loop", "while", "for", "in", "return", "break",
            "continue", "use", "mod", "crate", "super", "self", "Self", "as",
            "async", "await", "move", "where", "const", "static", "unsafe", "true", "false"
        ),
        SupportedLanguage.GO to setOf(
            "break", "case", "chan", "const", "continue", "default", "defer", "else",
            "fallthrough", "for", "func", "go", "goto", "if", "import", "interface",
            "map", "package", "range", "return", "select", "struct", "switch", "type",
            "var", "true", "false", "nil", "iota"
        ),
        SupportedLanguage.SQL to setOf(
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE",
            "TABLE", "DROP", "ALTER", "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "ON",
            "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "OFFSET", "UNION", "ALL",
            "AS", "AND", "OR", "NOT", "IN", "IS", "NULL", "PRIMARY", "KEY", "FOREIGN"
        ),
        SupportedLanguage.SHELL to setOf(
            "if", "then", "else", "elif", "fi", "case", "esac", "for", "select",
            "while", "until", "do", "done", "in", "function", "time", "return", "exit",
            "export", "alias", "source", "echo", "cat", "cd", "mkdir", "rm", "cp", "mv"
        )
    )

    private val NUMBER_PATTERN = Pattern.compile("\\b(\\d+\\.?\\d*([eE][+-]?\\d+)?|0x[0-9a-fA-F]+)\\b")
    private val STRING_PATTERN = Pattern.compile("(\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|`([^`\\\\]|\\\\.)*`)")
    private val OPERATOR_PATTERN = Pattern.compile("[+\\-*/%=<>!&|^~?:]")
    private val HTML_TAG_PATTERN = Pattern.compile("</?[a-zA-Z0-9\\-]+(\\s+[^>]*)?>")

    fun highlight(
        code: String,
        language: SupportedLanguage,
        theme: EditorTheme,
        searchQuery: String = ""
    ): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(code)

            val keywords = KEYWORDS_MAP[language] ?: KEYWORDS_MAP[SupportedLanguage.KOTLIN] ?: emptySet()
            val commentPrefix = language.singleLineComment

            // 1. Strings
            val stringMatcher = STRING_PATTERN.matcher(code)
            while (stringMatcher.find()) {
                addStyle(
                    SpanStyle(color = theme.stringColor),
                    stringMatcher.start(),
                    stringMatcher.end()
                )
            }

            // 2. Numbers
            val numberMatcher = NUMBER_PATTERN.matcher(code)
            while (numberMatcher.find()) {
                addStyle(
                    SpanStyle(color = theme.numberColor),
                    numberMatcher.start(),
                    numberMatcher.end()
                )
            }

            // 3. Keywords & Identifiers
            if (keywords.isNotEmpty()) {
                val wordPattern = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b")
                val wordMatcher = wordPattern.matcher(code)
                while (wordMatcher.find()) {
                    val word = wordMatcher.group(1)
                    if (keywords.contains(word) || (language == SupportedLanguage.SQL && keywords.contains(word.uppercase()))) {
                        addStyle(
                            SpanStyle(color = theme.keywordColor, fontWeight = FontWeight.SemiBold),
                            wordMatcher.start(),
                            wordMatcher.end()
                        )
                    } else if (word.first().isUpperCase()) {
                        // Types / Classes conventionally start with uppercase
                        addStyle(
                            SpanStyle(color = theme.typeColor),
                            wordMatcher.start(),
                            wordMatcher.end()
                        )
                    }
                }
            }

            // 4. HTML/XML Tags
            if (language == SupportedLanguage.HTML || language.isMarkup) {
                val tagMatcher = HTML_TAG_PATTERN.matcher(code)
                while (tagMatcher.find()) {
                    addStyle(
                        SpanStyle(color = theme.tagColor, fontWeight = FontWeight.Medium),
                        tagMatcher.start(),
                        tagMatcher.end()
                    )
                }
            }

            // 5. Operators
            val opMatcher = OPERATOR_PATTERN.matcher(code)
            while (opMatcher.find()) {
                addStyle(
                    SpanStyle(color = theme.operatorColor),
                    opMatcher.start(),
                    opMatcher.end()
                )
            }

            // 6. Single-line Comments (override earlier spans)
            if (commentPrefix.isNotEmpty()) {
                val lines = code.split("\n")
                var currentOffset = 0
                for (line in lines) {
                    val commentIdx = line.indexOf(commentPrefix)
                    if (commentIdx != -1) {
                        val start = currentOffset + commentIdx
                        val end = currentOffset + line.length
                        addStyle(
                            SpanStyle(color = theme.commentColor, fontFamily = FontFamily.Monospace),
                            start,
                            end
                        )
                    }
                    currentOffset += line.length + 1
                }
            }

            // 7. Search query highlights (highest priority)
            if (searchQuery.isNotEmpty()) {
                var searchIndex = code.indexOf(searchQuery, 0, ignoreCase = true)
                while (searchIndex != -1) {
                    addStyle(
                        SpanStyle(
                            background = theme.searchHighlightColor,
                            fontWeight = FontWeight.Bold
                        ),
                        searchIndex,
                        searchIndex + searchQuery.length
                    )
                    searchIndex = code.indexOf(searchQuery, searchIndex + searchQuery.length, ignoreCase = true)
                }
            }
        }
    }
}
