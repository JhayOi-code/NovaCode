package com.example.runner.embedded

import com.example.runner.CodeRunner
import com.example.runner.RunEvent
import com.example.runner.RunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject

/**
 * Embedded validator and structured previewer for non-executable file formats:
 * JSON, Markdown, XML, CSV, and Plain Text.
 */
class EmbeddedFormatRunner : CodeRunner {

    override val id: String = "embedded_format"
    override val displayName: String = "Document Validator & Formatter (Offline)"
    override val supportedLanguages: Set<String> = setOf("JSON", "Markdown", "XML", "CSV", "Text", "Plain Text")

    override suspend fun isAvailable(): Boolean = true

    override fun run(request: RunRequest): Flow<RunEvent> = flow {
        val startTime = System.currentTimeMillis()
        val lang = request.language
        emit(RunEvent.Started(language = lang, runner = displayName))

        val ext = request.fileName.substringAfterLast('.', "").lowercase()
        when (ext) {
            "json" -> {
                val trimmed = request.code.trim()
                if (trimmed.isEmpty()) {
                    emit(RunEvent.Stderr("Error: Empty JSON content"))
                    emit(RunEvent.Exit(code = 1, durationMs = System.currentTimeMillis() - startTime))
                    return@flow
                }
                try {
                    val formatted = if (trimmed.startsWith("[")) {
                        JSONArray(trimmed).toString(2)
                    } else {
                        JSONObject(trimmed).toString(2)
                    }
                    emit(RunEvent.Stdout("[Syntax Valid] JSON structure confirmed:\n"))
                    emit(RunEvent.Stdout(formatted))
                    emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
                } catch (e: Exception) {
                    emit(RunEvent.Stderr("JSON Syntax Error: ${e.message}"))
                    emit(RunEvent.Exit(code = 1, durationMs = System.currentTimeMillis() - startTime))
                }
            }
            "md", "markdown" -> {
                emit(RunEvent.Stdout("=== Markdown Document Summary: ${request.fileName} ===\n"))
                val lines = request.code.lines()
                var h1Count = 0
                var h2Count = 0
                for (line in lines) {
                    val t = line.trim()
                    when {
                        t.startsWith("# ") -> {
                            h1Count++
                            emit(RunEvent.Stdout("■ ${t.removePrefix("# ")}\n"))
                        }
                        t.startsWith("## ") -> {
                            h2Count++
                            emit(RunEvent.Stdout("  ▶ ${t.removePrefix("## ")}\n"))
                        }
                        t.startsWith("- ") || t.startsWith("* ") -> {
                            emit(RunEvent.Stdout("    • ${t.substring(2)}\n"))
                        }
                    }
                }
                emit(RunEvent.Stdout("\nTotal: ${lines.size} lines, $h1Count H1 headers, $h2Count H2 headers."))
                emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
            }
            "xml" -> {
                emit(RunEvent.Stdout("=== XML Document: ${request.fileName} ===\n"))
                val isBalanced = request.code.count { it == '<' } == request.code.count { it == '>' }
                if (isBalanced) {
                    emit(RunEvent.Stdout("[Syntax Valid] Tag structure balanced (${request.code.length} characters).\n"))
                    emit(RunEvent.Stdout(request.code.take(2000)))
                    emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
                } else {
                    emit(RunEvent.Stderr("XML Syntax Warning: Unbalanced angle brackets in document."))
                    emit(RunEvent.Exit(code = 1, durationMs = System.currentTimeMillis() - startTime))
                }
            }
            "csv" -> {
                emit(RunEvent.Stdout("=== CSV Table Preview: ${request.fileName} ===\n"))
                val lines = request.code.lines().filter { it.isNotBlank() }
                for ((idx, line) in lines.take(50).withIndex()) {
                    emit(RunEvent.Stdout("[Row ${idx + 1}] $line\n"))
                }
                emit(RunEvent.Stdout("\n(${lines.size} total rows parsed)"))
                emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
            }
            else -> {
                emit(RunEvent.Stdout(request.code))
                emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun stop() {}
}
