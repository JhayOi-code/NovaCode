package com.example.model

enum class SupportedLanguage(
    val displayName: String,
    val extensions: List<String>,
    val singleLineComment: String = "//",
    val isMarkup: Boolean = false
) {
    KOTLIN("Kotlin", listOf("kt", "kts"), "//"),
    JAVA("Java", listOf("java"), "//"),
    PYTHON("Python", listOf("py", "pyw"), "#"),
    JAVASCRIPT("JavaScript", listOf("js", "mjs", "cjs"), "//"),
    TYPESCRIPT("TypeScript", listOf("ts", "tsx"), "//"),
    HTML("HTML", listOf("html", "htm"), "<!--", isMarkup = true),
    CSS("CSS", listOf("css", "scss", "sass"), "/*"),
    C("C", listOf("c", "h"), "//"),
    CPP("C++", listOf("cpp", "hpp", "cc", "cxx"), "//"),
    RUST("Rust", listOf("rs"), "//"),
    GO("Go", listOf("go"), "//"),
    DART("Dart", listOf("dart"), "//"),
    PHP("PHP", listOf("php"), "//"),
    MARKDOWN("Markdown", listOf("md", "markdown"), "<!--", isMarkup = true),
    JSON("JSON", listOf("json"), "//"),
    YAML("YAML", listOf("yaml", "yml"), "#"),
    TOML("TOML", listOf("toml"), "#"),
    SQL("SQL", listOf("sql"), "--"),
    SHELL("Shell", listOf("sh", "bash", "zsh"), "#"),
    PLAINTEXT("Plain Text", listOf("txt", "log"), "");

    companion object {
        fun fromExtension(ext: String): SupportedLanguage {
            val cleanExt = ext.lowercase().trimStart('.')
            return entries.firstOrNull { it.extensions.contains(cleanExt) } ?: PLAINTEXT
        }

        fun fromFilename(filename: String): SupportedLanguage {
            val ext = filename.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}
