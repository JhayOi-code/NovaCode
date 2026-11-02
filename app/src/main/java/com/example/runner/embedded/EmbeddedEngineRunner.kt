package com.example.runner.embedded

import android.content.Context
import com.example.engine.CodeExecutionEngine
import com.example.engine.PyWindowManager
import com.example.model.CodeFile
import com.example.runner.CodeRunner
import com.example.runner.RunEvent
import com.example.runner.RunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Universal on-device code execution engine implementing [CodeRunner].
 * Executes Python, Java, Kotlin, C/C++, Rust, Go, PHP, Ruby, Lua, Shell, SQL, JS, HTML,
 * and arbitrary languages offline on the local device without requiring any server,
 * API key, authentication, or Termux.
 */
class EmbeddedEngineRunner(
    private val context: Context,
    private val openFilesProvider: () -> List<CodeFile> = { emptyList() },
    var windowManager: PyWindowManager? = null
) : CodeRunner {

    override val id: String = "embedded_engine"
    override val displayName: String = "On-Device Engine (Offline)"

    override val supportedLanguages: Set<String> = setOf(
        "python", "py", "pyw",
        "java",
        "kotlin", "kt", "kts",
        "javascript", "js", "mjs", "cjs", "jsx",
        "typescript", "ts", "tsx",
        "c", "cpp", "cc", "cxx", "h", "hpp",
        "rust", "rs",
        "go",
        "php",
        "ruby", "rb",
        "lua",
        "sql",
        "bash", "sh", "shell",
        "html", "htm", "css", "svg",
        "json", "xml", "markdown", "md", "csv", "text"
    )

    private val isRunning = AtomicBoolean(false)

    override suspend fun isAvailable(): Boolean = true

    override fun run(request: RunRequest): Flow<RunEvent> = channelFlow {
        isRunning.set(true)
        val startTime = System.currentTimeMillis()

        send(RunEvent.Started(language = request.language, runner = displayName))

        try {
            val codeFile = CodeFile(
                name = request.fileName.ifEmpty { "main" },
                path = request.filePath.ifEmpty { "/workspace/${request.fileName}" },
                content = request.code,
                language = request.language
            )

            val openFiles = openFilesProvider()
            val allFiles = if (openFiles.none { it.name == codeFile.name }) {
                openFiles + codeFile
            } else {
                openFiles
            }

            var streamedAny = false

            val result = CodeExecutionEngine.execute(
                file = codeFile,
                allFiles = allFiles,
                onChunk = { chunk ->
                    if (isRunning.get()) {
                        streamedAny = true
                        trySend(RunEvent.Stdout(chunk))
                    }
                },
                windowManager = windowManager,
                isCancelled = { !isRunning.get() }
            )

            if (!isRunning.get()) {
                send(RunEvent.Stderr("\nExecution stopped by user."))
                send(RunEvent.Exit(code = 130, durationMs = System.currentTimeMillis() - startTime))
                return@channelFlow
            }

            if (!streamedAny) {
                for (line in result.output) {
                    if (result.isSuccess) {
                        send(RunEvent.Stdout(line))
                    } else {
                        send(RunEvent.Stderr(line))
                    }
                }
            } else if (!result.isSuccess) {
                for (err in result.output) {
                    send(RunEvent.Stderr(err))
                }
            }

            send(RunEvent.Exit(code = result.exitCode, durationMs = result.executionTimeMs))
        } catch (e: Exception) {
            val msg = e.message ?: "An unexpected error occurred during execution."
            send(RunEvent.Stderr(msg))
            send(RunEvent.Exit(code = 1, durationMs = System.currentTimeMillis() - startTime))
        } finally {
            isRunning.set(false)
        }
    }.flowOn(Dispatchers.IO)

    override fun stop() {
        isRunning.set(false)
    }
}
