package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

object PrismSyntaxHighlighter {

    // Prism.js Dark Theme Palette
    private val COLOR_COMMENT = Color(0xFF64748B)
    private val COLOR_KEYWORD = Color(0xFFC084FC)
    private val COLOR_STRING = Color(0xFF34D399)
    private val COLOR_NUMBER = Color(0xFFFB923C)
    private val COLOR_TAG = Color(0xFFF43F5E)
    private val COLOR_ATTR_NAME = Color(0xFF38BDF8)
    private val COLOR_PROPERTY = Color(0xFF38BDF8)
    private val COLOR_SELECTOR = Color(0xFFFBBF24)
    private val COLOR_BUILTIN = Color(0xFF22D3EE)
    private val COLOR_FUNCTION = Color(0xFF60A5FA)
    private val COLOR_OPERATOR = Color(0xFF67E8F9)
    private val COLOR_PUNCTUATION = Color(0xFF94A3B8)
    private val COLOR_ANNOTATION = Color(0xFFF472B6)

    // Patterns for HTML / XML
    private val HTML_COMMENT = Pattern.compile("<!--[\\s\\S]*?-->")
    private val HTML_DOCTYPE = Pattern.compile("(?i)<!DOCTYPE[^>]*>")
    private val HTML_TAG = Pattern.compile("</?[a-zA-Z0-9\\-]+")
    private val HTML_ATTR_NAME = Pattern.compile("[a-zA-Z0-9\\-]+(?=\\s*=)")
    private val HTML_STRING = Pattern.compile("\"[^\"]*\"|'[^']*'")
    private val HTML_PUNCTUATION = Pattern.compile("[<>/=]")

    // Patterns for CSS
    private val CSS_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/")
    private val CSS_SELECTOR = Pattern.compile("(?m)^[^{]+(?=\\{)|[.#][a-zA-Z0-9_\\-]+")
    private val CSS_PROPERTY = Pattern.compile("[a-zA-Z\\-]+(?=\\s*:)")
    private val CSS_NUMBER = Pattern.compile("\\b\\d+(\\.\\d+)?(px|em|rem|%|vh|vw|s|ms|deg)?\\b")
    private val CSS_COLOR_HEX = Pattern.compile("#[0-9a-fA-F]{3,8}")
    private val CSS_KEYWORD = Pattern.compile("\\b(none|block|flex|grid|inline|absolute|relative|fixed|center|auto|inherit|important|solid|dashed)\\b")

    // Patterns for JS / TS
    private val JS_COMMENT_LINE = Pattern.compile("//.*")
    private val JS_COMMENT_BLOCK = Pattern.compile("/\\*[\\s\\S]*?\\*/")
    private val JS_STRING = Pattern.compile("\"[^\"]*\"|'[^']*'|`[^`]*`")
    private val JS_KEYWORD = Pattern.compile("\\b(function|const|let|var|return|if|else|for|while|class|import|export|from|async|await|new|this|typeof|switch|case|break|try|catch|finally|throw|default|of|in|extends|super)\\b")
    private val JS_BOOLEAN = Pattern.compile("\\b(true|false|null|undefined|NaN)\\b")
    private val JS_NUMBER = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
    private val JS_BUILTIN = Pattern.compile("\\b(console|document|window|Math|JSON|Array|Object|String|Promise|setTimeout|setInterval|fetch)\\b")
    private val JS_FUNCTION = Pattern.compile("[a-zA-Z0-9_\$]+(?=\\s*\\()")
    private val JS_OPERATOR = Pattern.compile("=>|===|!==|==|!=|&&|\\|\\||[+\\-*/%=<>!&|^~]")

    // Patterns for Python
    private val PY_COMMENT = Pattern.compile("#.*")
    private val PY_TRIPLE_STRING = Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''")
    private val PY_STRING = Pattern.compile("f?\"[^\"]*\"|f?'[^']*'")
    private val PY_KEYWORD = Pattern.compile("\\b(def|class|return|if|elif|else|for|while|in|is|not|and|or|import|from|as|try|except|finally|raise|with|pass|break|continue|lambda|yield|global|nonlocal|assert)\\b")
    private val PY_BOOLEAN = Pattern.compile("\\b(True|False|None)\\b")
    private val PY_DECORATOR = Pattern.compile("@[a-zA-Z0-9_.]+")

    // Patterns for Kotlin / Java
    private val KT_KEYWORD = Pattern.compile("\\b(fun|val|var|class|interface|object|enum|companion|package|import|return|if|else|when|for|while|try|catch|finally|throw|public|private|protected|internal|override|abstract|open|data|sealed|inline|suspend)\\b")
    private val KT_ANNOTATION = Pattern.compile("@[a-zA-Z0-9_]+")

    // Patterns for C / C++ / Rust / Go
    private val C_PREPROCESSOR = Pattern.compile("#[a-zA-Z]+")
    private val C_KEYWORD = Pattern.compile("\\b(int|char|float|double|void|bool|struct|class|namespace|using|public|private|protected|template|typename|return|if|else|for|while|switch|case|break|fn|let|mut|pub|impl|trait|match|package|func|type|defer|go)\\b")

    // Patterns for SQL
    private val SQL_KEYWORD = Pattern.compile("(?i)\\b(SELECT|FROM|WHERE|INSERT|INTO|VALUES|UPDATE|SET|DELETE|CREATE|TABLE|DROP|ALTER|JOIN|LEFT|RIGHT|INNER|OUTER|ON|GROUP BY|ORDER BY|ASC|DESC|LIMIT|OFFSET|AND|OR|NOT|IN|PRIMARY|KEY|FOREIGN|REFERENCES|NOT NULL|DEFAULT)\\b")

    // Patterns for JSON
    private val JSON_KEY = Pattern.compile("\"[^\"]+\"(?=\\s*:)")

    // Patterns for Markdown
    private val MD_HEADER = Pattern.compile("(?m)^#{1,6}\\s.*$")
    private val MD_BOLD = Pattern.compile("\\*\\*[^*]+\\*\\*|__[^_]+__")
    private val MD_CODE = Pattern.compile("`[^`]+`")

    fun highlight(text: String, language: String): AnnotatedString {
        val builder = AnnotatedString.Builder(text)
        val normalizedLang = language.lowercase()

        when {
            normalizedLang.contains("html") || normalizedLang.contains("web") || normalizedLang.contains("xml") -> {
                highlightHtml(text, builder)
            }
            normalizedLang.contains("css") -> {
                highlightCss(text, builder)
            }
            normalizedLang.contains("js") || normalizedLang.contains("javascript") || normalizedLang.contains("ts") || normalizedLang.contains("typescript") -> {
                highlightJs(text, builder)
            }
            normalizedLang.contains("python") || normalizedLang.contains("py") -> {
                highlightPython(text, builder)
            }
            normalizedLang.contains("kotlin") || normalizedLang.contains("java") -> {
                highlightKotlinJava(text, builder)
            }
            normalizedLang.contains("c++") || normalizedLang.contains("cpp") || normalizedLang.contains(" c") || normalizedLang == "c" || normalizedLang.contains("rust") || normalizedLang.contains("go") -> {
                highlightNative(text, builder)
            }
            normalizedLang.contains("sql") -> {
                highlightSql(text, builder)
            }
            normalizedLang.contains("json") -> {
                highlightJson(text, builder)
            }
            normalizedLang.contains("markdown") || normalizedLang.contains("md") -> {
                highlightMarkdown(text, builder)
            }
            else -> {
                highlightJs(text, builder)
            }
        }

        return builder.toAnnotatedString()
    }

    private fun highlightHtml(text: String, builder: AnnotatedString.Builder) {
        applyRegex(HTML_TAG, text, builder, SpanStyle(color = COLOR_TAG, fontWeight = FontWeight.Bold))
        applyRegex(HTML_ATTR_NAME, text, builder, SpanStyle(color = COLOR_ATTR_NAME))
        applyRegex(HTML_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(HTML_PUNCTUATION, text, builder, SpanStyle(color = COLOR_PUNCTUATION))
        applyRegex(HTML_DOCTYPE, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(HTML_COMMENT, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightCss(text: String, builder: AnnotatedString.Builder) {
        applyRegex(CSS_SELECTOR, text, builder, SpanStyle(color = COLOR_SELECTOR, fontWeight = FontWeight.SemiBold))
        applyRegex(CSS_PROPERTY, text, builder, SpanStyle(color = COLOR_PROPERTY))
        applyRegex(CSS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(CSS_COLOR_HEX, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(CSS_KEYWORD, text, builder, SpanStyle(color = COLOR_KEYWORD))
        applyRegex(CSS_COMMENT, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightJs(text: String, builder: AnnotatedString.Builder) {
        applyRegex(JS_OPERATOR, text, builder, SpanStyle(color = COLOR_OPERATOR))
        applyRegex(JS_FUNCTION, text, builder, SpanStyle(color = COLOR_FUNCTION))
        applyRegex(JS_BUILTIN, text, builder, SpanStyle(color = COLOR_BUILTIN, fontWeight = FontWeight.SemiBold))
        applyRegex(JS_KEYWORD, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(JS_BOOLEAN, text, builder, SpanStyle(color = COLOR_NUMBER, fontWeight = FontWeight.SemiBold))
        applyRegex(JS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(JS_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(JS_COMMENT_LINE, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
        applyRegex(JS_COMMENT_BLOCK, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightPython(text: String, builder: AnnotatedString.Builder) {
        applyRegex(JS_FUNCTION, text, builder, SpanStyle(color = COLOR_FUNCTION))
        applyRegex(PY_KEYWORD, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(PY_BOOLEAN, text, builder, SpanStyle(color = COLOR_NUMBER, fontWeight = FontWeight.SemiBold))
        applyRegex(JS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(PY_DECORATOR, text, builder, SpanStyle(color = COLOR_ANNOTATION))
        applyRegex(PY_TRIPLE_STRING, text, builder, SpanStyle(color = COLOR_STRING, fontStyle = FontStyle.Italic))
        applyRegex(PY_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(PY_COMMENT, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightKotlinJava(text: String, builder: AnnotatedString.Builder) {
        applyRegex(JS_FUNCTION, text, builder, SpanStyle(color = COLOR_FUNCTION))
        applyRegex(KT_KEYWORD, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(KT_ANNOTATION, text, builder, SpanStyle(color = COLOR_ANNOTATION))
        applyRegex(JS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(JS_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(JS_COMMENT_LINE, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
        applyRegex(JS_COMMENT_BLOCK, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightNative(text: String, builder: AnnotatedString.Builder) {
        applyRegex(C_PREPROCESSOR, text, builder, SpanStyle(color = COLOR_TAG, fontWeight = FontWeight.Bold))
        applyRegex(C_KEYWORD, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(JS_FUNCTION, text, builder, SpanStyle(color = COLOR_FUNCTION))
        applyRegex(JS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(JS_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(JS_COMMENT_LINE, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
        applyRegex(JS_COMMENT_BLOCK, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightSql(text: String, builder: AnnotatedString.Builder) {
        applyRegex(SQL_KEYWORD, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(JS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(JS_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(JS_COMMENT_LINE, text, builder, SpanStyle(color = COLOR_COMMENT, fontStyle = FontStyle.Italic))
    }

    private fun highlightJson(text: String, builder: AnnotatedString.Builder) {
        applyRegex(JSON_KEY, text, builder, SpanStyle(color = COLOR_ATTR_NAME, fontWeight = FontWeight.SemiBold))
        applyRegex(JS_NUMBER, text, builder, SpanStyle(color = COLOR_NUMBER))
        applyRegex(JS_STRING, text, builder, SpanStyle(color = COLOR_STRING))
        applyRegex(JS_BOOLEAN, text, builder, SpanStyle(color = COLOR_KEYWORD))
    }

    private fun highlightMarkdown(text: String, builder: AnnotatedString.Builder) {
        applyRegex(MD_HEADER, text, builder, SpanStyle(color = COLOR_KEYWORD, fontWeight = FontWeight.Bold))
        applyRegex(MD_BOLD, text, builder, SpanStyle(color = COLOR_SELECTOR, fontWeight = FontWeight.Bold))
        applyRegex(MD_CODE, text, builder, SpanStyle(color = COLOR_STRING))
    }

    private fun applyRegex(pattern: Pattern, text: String, builder: AnnotatedString.Builder, style: SpanStyle) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            builder.addStyle(style, matcher.start(), matcher.end())
        }
    }
}

class PrismSyntaxVisualTransformation(
    private val language: String
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = PrismSyntaxHighlighter.highlight(text.text, language)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}
