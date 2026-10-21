package com.example.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.model.EditorTheme
import java.util.regex.Pattern

object PrismSyntaxHighlighter {

    private val pythonKeywords = setOf(
        "and", "as", "assert", "async", "await", "break", "class", "continue",
        "def", "del", "elif", "else", "except", "finally", "for", "from",
        "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
        "or", "pass", "raise", "return", "try", "while", "with", "yield",
        "match", "case"
    )

    private val pythonBuiltins = setOf(
        "True", "False", "None", "self", "print", "range", "len", "open", "type",
        "int", "str", "float", "bool", "list", "dict", "set", "tuple",
        "enumerate", "zip", "map", "filter", "sum", "min", "max", "abs",
        "round", "input", "isinstance", "issubclass", "super", "property",
        "sys", "time", "os", "math", "random", "json", "stdout", "write", "flush", "sleep"
    )

    private val javaKeywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "record", "sealed", "permits",
        "var", "yield", "String", "Object", "System", "out", "println", "print", "printf",
        "true", "false", "null"
    )

    private val kotlinKeywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "interface", "is", "null", "object", "package", "return",
        "super", "this", "throw", "true", "try", "typealias", "val", "var",
        "when", "while", "by", "companion", "data", "enum", "sealed", "override",
        "private", "protected", "public", "internal", "expect", "actual", "inline",
        "suspend", "constructor", "init", "it", "println", "print", "Unit", "Any",
        "String", "Int", "Boolean", "Double", "Float", "Long", "List", "Map", "Set"
    )

    private val jsKeywords = setOf(
        "async", "await", "break", "case", "catch", "class", "const", "continue",
        "debugger", "default", "delete", "do", "else", "export", "extends",
        "finally", "for", "function", "if", "import", "in", "instanceof", "let",
        "new", "return", "super", "switch", "this", "throw", "try", "typeof",
        "var", "void", "while", "with", "yield", "true", "false", "null", "undefined",
        "from", "as", "of", "console", "log", "warn", "error", "info", "document", "window"
    )

    private val bashKeywords = setOf(
        "if", "then", "else", "elif", "fi", "case", "esac", "for", "select",
        "while", "until", "do", "done", "in", "function", "time", "return",
        "exit", "export", "echo", "local", "readonly", "source", "alias", "sh", "bash"
    )

    private val cppKeywords = setOf(
        "auto", "const", "double", "float", "int", "short", "struct", "unsigned",
        "break", "continue", "else", "for", "long", "signed", "switch", "void",
        "case", "default", "enum", "goto", "sizeof", "typedef", "char", "do",
        "if", "return", "static", "while", "class", "namespace", "template",
        "public", "private", "protected", "virtual", "bool", "true", "false",
        "new", "delete", "this", "try", "catch", "include", "std", "cout", "cin", "endl", "printf"
    )

    private val wordPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
    private val numberPattern = Pattern.compile("\\b(0x[0-9a-fA-F]+|\\d+(\\.\\d+)?([eE][+-]?\\d+)?)\\b")
    private val decoratorPattern = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*")

    // Multiline & single line strings
    private val pyMultilineStringPattern = Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''")
    private val stringPattern = Pattern.compile("\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|`(\\\\.|[^`\\\\])*`")

    // Comments
    private val pyLineCommentPattern = Pattern.compile("#.*")
    private val cLineCommentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/|<!--[\\s\\S]*?-->")

    // Operators and Brackets
    private val operatorPattern = Pattern.compile("(==|!=|<=|>=|&&|\\|\\||->|=>|\\+\\+|--|\\+=|-=|\\*=|/=|%=|<<|>>|[+\\-*/%=<>!&|^~])")
    private val bracketPattern = Pattern.compile("([\\[\\]{}();,:])")

    /**
     * Accurately highlights code into AnnotatedString with high-contrast, beautiful syntax tokens.
     */
    fun highlightCode(text: String, language: String, theme: EditorTheme): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")

        val lang = language.lowercase().trim()
        val isPython = lang == "python" || lang == "py"
        val isKotlin = lang == "kotlin" || lang == "kt" || lang == "kts"
        val isJs = lang in setOf("javascript", "typescript", "js", "ts", "html", "htm")
        val isBash = lang in setOf("bash", "sh", "shell")
        val isCpp = lang in setOf("cpp", "c", "h", "hpp")

        val keywords = when {
            isPython -> pythonKeywords
            isKotlin -> kotlinKeywords
            isJs -> jsKeywords
            isBash -> bashKeywords
            isCpp -> cppKeywords
            else -> javaKeywords
        }

        val builtins = when {
            isPython -> pythonBuiltins
            else -> setOf("println", "print", "console", "log", "System", "String", "Math", "true", "false", "null")
        }

        return buildAnnotatedString {
            append(text)

            // Base style: Ensure every character has default onSurfaceColor
            addStyle(
                SpanStyle(color = theme.onSurfaceColor, fontFamily = FontFamily.Monospace),
                0,
                text.length
            )

            // BitSet to protect already highlighted ranges (like comments and strings)
            val protectedMask = BooleanArray(text.length)

            // 1. Comments
            val commentRegex = if (isPython || isBash) pyLineCommentPattern else cLineCommentPattern
            val commentMatcher = commentRegex.matcher(text)
            while (commentMatcher.find()) {
                val start = commentMatcher.start()
                val end = commentMatcher.end()
                addStyle(
                    SpanStyle(color = theme.commentColor, fontFamily = FontFamily.Monospace),
                    start,
                    end
                )
                for (idx in start until end) {
                    if (idx < protectedMask.size) protectedMask[idx] = true
                }
            }

            // 2. Multiline strings (Python """ or ''')
            if (isPython) {
                val multiMatcher = pyMultilineStringPattern.matcher(text)
                while (multiMatcher.find()) {
                    val start = multiMatcher.start()
                    val end = multiMatcher.end()
                    addStyle(
                        SpanStyle(color = theme.stringColor, fontWeight = FontWeight.Normal),
                        start,
                        end
                    )
                    for (idx in start until end) {
                        if (idx < protectedMask.size) protectedMask[idx] = true
                    }
                }
            }

            // 3. Normal Strings ("..." and '...')
            val strMatcher = stringPattern.matcher(text)
            while (strMatcher.find()) {
                val start = strMatcher.start()
                val end = strMatcher.end()
                if (start < protectedMask.size && protectedMask[start]) continue

                addStyle(
                    SpanStyle(color = theme.stringColor, fontWeight = FontWeight.Normal),
                    start,
                    end
                )
                for (idx in start until end) {
                    if (idx < protectedMask.size) protectedMask[idx] = true
                }
            }

            // 4. Numbers
            val numMatcher = numberPattern.matcher(text)
            while (numMatcher.find()) {
                val start = numMatcher.start()
                val end = numMatcher.end()
                if (start < protectedMask.size && protectedMask[start]) continue

                addStyle(
                    SpanStyle(color = theme.numberColor, fontWeight = FontWeight.Bold),
                    start,
                    end
                )
                for (idx in start until end) {
                    if (idx < protectedMask.size) protectedMask[idx] = true
                }
            }

            // 5. Decorators / Annotations (@...)
            val decMatcher = decoratorPattern.matcher(text)
            while (decMatcher.find()) {
                val start = decMatcher.start()
                val end = decMatcher.end()
                if (start < protectedMask.size && protectedMask[start]) continue

                addStyle(
                    SpanStyle(color = theme.primaryColor, fontWeight = FontWeight.SemiBold),
                    start,
                    end
                )
                for (idx in start until end) {
                    if (idx < protectedMask.size) protectedMask[idx] = true
                }
            }

            // 6. Operators (+, -, *, /, =, ==, !=, etc.)
            val opMatcher = operatorPattern.matcher(text)
            while (opMatcher.find()) {
                val start = opMatcher.start()
                val end = opMatcher.end()
                if (start < protectedMask.size && protectedMask[start]) continue

                addStyle(
                    SpanStyle(color = theme.primaryColor, fontWeight = FontWeight.Bold),
                    start,
                    end
                )
            }

            // 7. Brackets and Delimiters ([ ], { }, ( ), ;, :, ,)
            val brMatcher = bracketPattern.matcher(text)
            while (brMatcher.find()) {
                val start = brMatcher.start()
                val end = brMatcher.end()
                if (start < protectedMask.size && protectedMask[start]) continue

                addStyle(
                    SpanStyle(color = theme.secondaryColor),
                    start,
                    end
                )
            }

            // 8. Words: Keywords, Builtins, Functions, Variables
            val wordMatcher = wordPattern.matcher(text)
            while (wordMatcher.find()) {
                val start = wordMatcher.start()
                val end = wordMatcher.end()
                if (start < protectedMask.size && protectedMask[start]) continue

                val word = wordMatcher.group()

                when {
                    keywords.contains(word) -> {
                        addStyle(
                            SpanStyle(color = theme.keywordColor, fontWeight = FontWeight.Bold),
                            start,
                            end
                        )
                    }
                    builtins.contains(word) -> {
                        addStyle(
                            SpanStyle(color = theme.secondaryColor, fontWeight = FontWeight.SemiBold),
                            start,
                            end
                        )
                    }
                    // Function call: word followed by optional whitespace and (
                    isFollowedByParen(text, end) -> {
                        addStyle(
                            SpanStyle(color = theme.functionColor, fontWeight = FontWeight.Medium),
                            start,
                            end
                        )
                    }
                    word.all { it.isUpperCase() || it == '_' } && word.length > 1 -> {
                        // Constants: PI, MAX_VALUE
                        addStyle(
                            SpanStyle(color = theme.numberColor, fontWeight = FontWeight.SemiBold),
                            start,
                            end
                        )
                    }
                    word.first().isUpperCase() -> {
                        // Class/Type Name: MyClass, String, Exception
                        addStyle(
                            SpanStyle(color = theme.primaryColor, fontWeight = FontWeight.SemiBold),
                            start,
                            end
                        )
                    }
                    else -> {
                        addStyle(
                            SpanStyle(color = theme.variableColor),
                            start,
                            end
                        )
                    }
                }
            }
        }
    }

    private fun isFollowedByParen(text: String, startIdx: Int): Boolean {
        var idx = startIdx
        while (idx < text.length && text[idx].isWhitespace()) {
            idx++
        }
        return idx < text.length && text[idx] == '('
    }
}
