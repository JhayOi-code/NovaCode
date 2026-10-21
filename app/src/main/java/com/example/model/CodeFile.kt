package com.example.model

import java.util.UUID

data class CodeFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val content: String,
    val originalContent: String = content,
    val isModified: Boolean = false,
    val language: String = detectLanguage(name)
) {
    companion object {
        fun detectLanguage(filename: String): String {
            return when {
                filename.endsWith(".java", ignoreCase = true) -> "java"
                filename.endsWith(".kt", ignoreCase = true) || filename.endsWith(".kts", ignoreCase = true) -> "kotlin"
                filename.endsWith(".py", ignoreCase = true) -> "python"
                filename.endsWith(".js", ignoreCase = true) -> "javascript"
                filename.endsWith(".ts", ignoreCase = true) -> "typescript"
                filename.endsWith(".html", ignoreCase = true) || filename.endsWith(".htm", ignoreCase = true) -> "html"
                filename.endsWith(".css", ignoreCase = true) -> "css"
                filename.endsWith(".json", ignoreCase = true) -> "json"
                filename.endsWith(".sh", ignoreCase = true) || filename.endsWith(".bash", ignoreCase = true) -> "bash"
                filename.endsWith(".md", ignoreCase = true) -> "markdown"
                filename.endsWith(".xml", ignoreCase = true) -> "xml"
                filename.endsWith(".c", ignoreCase = true) || filename.endsWith(".cpp", ignoreCase = true) || filename.endsWith(".h", ignoreCase = true) -> "cpp"
                else -> "plaintext"
            }
        }
    }
}
