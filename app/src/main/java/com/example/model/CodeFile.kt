package com.example.model

import java.io.File

data class CodeFile(
    val file: File,
    val name: String = file.name,
    var content: String = "",
    var isModified: Boolean = false,
    val language: SupportedLanguage = SupportedLanguage.fromExtension(file.extension)
)

data class FileItem(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val sizeBytes: Long = if (file.isFile) file.length() else 0L,
    val extension: String = file.extension
)

enum class SupportedLanguage(val displayName: String, val extension: String) {
    HTML("HTML / Web", "html"),
    CSS("CSS", "css"),
    JAVASCRIPT("JavaScript", "js"),
    TYPESCRIPT("TypeScript", "ts"),
    PYTHON("Python", "py"),
    SHELL("Shell / Bash", "sh"),
    KOTLIN("Kotlin", "kt"),
    JAVA("Java", "java"),
    C("C", "c"),
    CPP("C++", "cpp"),
    RUST("Rust", "rs"),
    GO("Go", "go"),
    SQL("SQL", "sql"),
    PHP("PHP", "php"),
    JSON("JSON", "json"),
    MARKDOWN("Markdown", "md"),
    XML("XML", "xml"),
    TEXT("Text", "txt");

    companion object {
        fun fromExtension(ext: String): SupportedLanguage {
            val cleanExt = ext.lowercase()
            return when (cleanExt) {
                "html", "htm" -> HTML
                "css" -> CSS
                "js", "mjs", "cjs" -> JAVASCRIPT
                "ts", "tsx" -> TYPESCRIPT
                "py", "pyw" -> PYTHON
                "sh", "bash", "zsh" -> SHELL
                "kt", "kts" -> KOTLIN
                "java" -> JAVA
                "c", "h" -> C
                "cpp", "cc", "cxx", "hpp" -> CPP
                "rs" -> RUST
                "go" -> GO
                "sql" -> SQL
                "php" -> PHP
                "json" -> JSON
                "md", "markdown" -> MARKDOWN
                "xml" -> XML
                else -> TEXT
            }
        }
    }
}

data class TerminalLine(
    val text: String,
    val isError: Boolean = false,
    val isCommand: Boolean = false
)
