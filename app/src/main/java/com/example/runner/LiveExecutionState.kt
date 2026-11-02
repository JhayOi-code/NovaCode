package com.example.runner

/**
 * High-level execution status for UI presentation.
 */
enum class ExecutionStatus {
    IDLE,
    RUNNING,
    FINISHED,
    ERROR,
    STOPPED
}

/**
 * Categorization of streamed output lines for color coding.
 */
enum class OutputLineType {
    STDOUT,
    STDERR,
    SYSTEM
}

/**
 * Individual output line with optional jump-to-editor source location.
 */
data class OutputLine(
    val id: Long,
    val text: String,
    val type: OutputLineType,
    val fileTarget: String? = null,
    val lineTarget: Int? = null
)

/**
 * Full live state of active or last code execution session.
 */
data class LiveExecutionState(
    val language: String = "",
    val runnerName: String = "",
    val status: ExecutionStatus = ExecutionStatus.IDLE,
    val lines: List<OutputLine> = emptyList(),
    val durationMs: Long = 0L,
    val exitCode: Int? = null,
    val isRunning: Boolean = false,
    val isTruncated: Boolean = false,
    val activeFileName: String = "",
    val activeFilePath: String = ""
) {
    val totalRawText: String
        get() = lines.joinToString("\n") { it.text }
}

/**
 * Utilities for parsing compiler/interpreter stack traces and error messages into clickable line targets.
 */
object ErrorLineParser {

    private val pythonRegex = Regex("""File\s+["']([^"']+)["'],\s+line\s+(\d+)""", RegexOption.IGNORE_CASE)
    private val gccClangRegex = Regex("""^([a-zA-Z0-9_\-./\\]+):(\d+)(?::(\d+))?:\s+(?:fatal\s+)?(?:error|warning):""", RegexOption.IGNORE_CASE)
    private val javaRegex = Regex("""^([a-zA-Z0-9_\-./\\]+\.java):(\d+):\s+(?:error|warning):""", RegexOption.IGNORE_CASE)
    private val javaStackRegex = Regex("""(?:\(|at\s+)([a-zA-Z0-9_\-./\\]+\.java):(\d+)\)?""", RegexOption.IGNORE_CASE)
    private val jsNodeRegex = Regex("""(?:\(|at\s+)?([a-zA-Z0-9_\-./\\]+\.js):(\d+)(?::\d+)?\)?$""", RegexOption.IGNORE_CASE)
    private val rustRegex = Regex("""-->\s+([a-zA-Z0-9_\-./\\]+\.rs):(\d+):(\d+)""", RegexOption.IGNORE_CASE)
    private val goRegex = Regex("""^([a-zA-Z0-9_\-./\\]+\.go):(\d+):(?:\d+:)?\s+""", RegexOption.IGNORE_CASE)

    fun parseLineTarget(text: String): Pair<String, Int>? {
        val trimmed = text.trim()

        pythonRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        gccClangRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        javaRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        javaStackRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        rustRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        goRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        jsNodeRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        // Generic fallback for any language: filename.ext:line:
        val genericFileLineRegex = Regex("""(?:^|\s)([\w\-. ]+\.[a-zA-Z0-9]+):(\d+)(?::\d+)?(?::|\s)""", RegexOption.IGNORE_CASE)
        genericFileLineRegex.find(trimmed)?.let { match ->
            val file = match.groupValues[1].trim().substringAfterLast('/')
            val line = match.groupValues[2].toIntOrNull() ?: 1
            return Pair(file, line)
        }

        return null
    }
}
