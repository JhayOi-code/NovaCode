package com.example.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

/**
 * Fast compiled syntax tokenizer designed for per-line tokenization with zero heap fragmentation.
 * Supports lazy loading (regex compilation & keyword sets deferred until activated)
 * and memory eviction for low-RAM devices.
 */
object FastSyntaxTokenizer {

    @Volatile
    private var isInitialized = false

    private lateinit var KEYWORDS: Set<String>
    private var NUMBER_PATTERN: Pattern? = null
    private var STRING_PATTERN: Pattern? = null
    private var WORD_PATTERN: Pattern? = null
    private var OPERATOR_PATTERN: Pattern? = null

    /**
     * Lazily initialize syntax patterns and keywords when the syntax highlighter is first needed.
     */
    @Synchronized
    fun ensureInitialized() {
        if (isInitialized) return

        KEYWORDS = setOf(
            // Kotlin & Java
            "val", "var", "fun", "class", "interface", "object", "package", "import",
            "return", "if", "else", "when", "for", "while", "do", "try", "catch",
            "finally", "throw", "null", "true", "false", "this", "super", "override",
            "public", "private", "protected", "internal", "data", "enum", "sealed",
            "suspend", "inline", "abstract", "const", "new", "static", "void", "int",
            "boolean", "char", "float", "double", "long", "short", "byte",
            // Python
            "def", "from", "as", "elif", "except", "lambda", "global", "nonlocal",
            "pass", "assert", "del", "in", "is", "not", "and", "or", "None", "async", "await",
            // JS/TS
            "const", "let", "function", "export", "default", "typeof", "instanceof",
            "type", "declare", "readonly",
            // C/C++/Rust/Go
            "fn", "struct", "impl", "trait", "mut", "pub", "match", "loop", "chan",
            "defer", "go", "auto", "nullptr", "sizeof",
            // SQL
            "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "JOIN", "TABLE"
        )

        NUMBER_PATTERN = Pattern.compile("\\b(\\d+\\.?\\d*|0x[0-9a-fA-F]+)\\b")
        STRING_PATTERN = Pattern.compile("(\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|`([^`\\\\]|\\\\.)*`)")
        WORD_PATTERN = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b")
        OPERATOR_PATTERN = Pattern.compile("[+\\-*/%=<>!&|^~?:;,.]")

        isInitialized = true
    }

    /**
     * Clear loaded patterns and references on memory pressure.
     */
    @Synchronized
    fun evictMemory() {
        if (!isInitialized) return
        NUMBER_PATTERN = null
        STRING_PATTERN = null
        WORD_PATTERN = null
        OPERATOR_PATTERN = null
        isInitialized = false
    }

    fun isLoaded(): Boolean = isInitialized

    fun highlightLine(
        lineText: String,
        colorScheme: EditorColorScheme,
        searchQuery: String = ""
    ): AnnotatedString {
        if (lineText.isEmpty()) return AnnotatedString("")

        if (!isInitialized) {
            ensureInitialized()
        }

        val numPattern = NUMBER_PATTERN ?: return AnnotatedString(lineText)
        val strPattern = STRING_PATTERN ?: return AnnotatedString(lineText)
        val wordPattern = WORD_PATTERN ?: return AnnotatedString(lineText)
        val opPattern = OPERATOR_PATTERN ?: return AnnotatedString(lineText)

        return buildAnnotatedString {
            append(lineText)

            // 1. Strings
            val strMatcher = strPattern.matcher(lineText)
            while (strMatcher.find()) {
                addStyle(
                    SpanStyle(color = colorScheme.string),
                    strMatcher.start(),
                    strMatcher.end()
                )
            }

            // 2. Numbers
            val numMatcher = numPattern.matcher(lineText)
            while (numMatcher.find()) {
                addStyle(
                    SpanStyle(color = colorScheme.number),
                    numMatcher.start(),
                    numMatcher.end()
                )
            }

            // 3. Keywords & Types
            val wordMatcher = wordPattern.matcher(lineText)
            while (wordMatcher.find()) {
                val word = wordMatcher.group(1) ?: continue
                if (KEYWORDS.contains(word)) {
                    addStyle(
                        SpanStyle(color = colorScheme.keyword, fontWeight = FontWeight.SemiBold),
                        wordMatcher.start(),
                        wordMatcher.end()
                    )
                } else if (word.first().isUpperCase()) {
                    addStyle(
                        SpanStyle(color = colorScheme.type),
                        wordMatcher.start(),
                        wordMatcher.end()
                    )
                }
            }

            // 4. Operators
            val opMatcher = opPattern.matcher(lineText)
            while (opMatcher.find()) {
                addStyle(
                    SpanStyle(color = colorScheme.operator),
                    opMatcher.start(),
                    opMatcher.end()
                )
            }

            // 5. Comments (Single line // or #)
            val commentIdx = when {
                lineText.contains("//") -> lineText.indexOf("//")
                lineText.contains("#") -> lineText.indexOf("#")
                lineText.contains("--") -> lineText.indexOf("--")
                else -> -1
            }

            if (commentIdx != -1) {
                addStyle(
                    SpanStyle(color = colorScheme.comment, fontFamily = FontFamily.Monospace),
                    commentIdx,
                    lineText.length
                )
            }

            // 6. Search Highlights
            if (searchQuery.isNotEmpty()) {
                var searchIdx = lineText.indexOf(searchQuery, 0, ignoreCase = true)
                while (searchIdx != -1) {
                    addStyle(
                        SpanStyle(
                            background = colorScheme.searchHighlight,
                            fontWeight = FontWeight.Bold
                        ),
                        searchIdx,
                        searchIdx + searchQuery.length
                    )
                    searchIdx = lineText.indexOf(searchQuery, searchIdx + searchQuery.length, ignoreCase = true)
                }
            }
        }
    }
}
