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
    KOTLIN("Kotlin", "kt"),
    JAVA("Java", "java"),
    JAVASCRIPT("JavaScript", "js"),
    TYPESCRIPT("TypeScript", "ts"),
    HTML("HTML", "html"),
    CSS("CSS", "css"),
    JSON("JSON", "json"),
    PYTHON("Python", "py"),
    XML("XML", "xml"),
    MARKDOWN("Markdown", "md"),
    TEXT("Text", "txt");

    companion object {
        fun fromExtension(ext: String): SupportedLanguage {
            return entries.find { it.extension.equals(ext, ignoreCase = true) } ?: TEXT
        }
    }
}

data class TerminalLine(
    val text: String,
    val isError: Boolean = false,
    val isCommand: Boolean = false
)
