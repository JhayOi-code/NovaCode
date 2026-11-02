package com.example.runner

import kotlinx.coroutines.flow.Flow

/**
 * Common request representation for running code in any supported language.
 */
data class RunRequest(
    val language: String,
    val code: String,
    val filePath: String,
    val fileName: String,
    val stdin: String? = null,
    val args: List<String> = emptyList(),
    val timeoutMs: Long = 30_000L
)

/**
 * Sealed hierarchy of live execution events streamed from a [CodeRunner].
 */
sealed class RunEvent {
    data class Started(val language: String, val runner: String) : RunEvent()
    data class Stdout(val text: String) : RunEvent()
    data class Stderr(val text: String) : RunEvent()
    data class Exit(val code: Int, val durationMs: Long) : RunEvent()
    data class Failure(val message: String) : RunEvent()
}

/**
 * Standard contract for executing code across embedded, Termux, and remote execution engines.
 */
interface CodeRunner {
    /** Unique identifier for this runner (e.g. "embedded_sql", "termux", "remote_piston"). */
    val id: String

    /** Human-readable display name (e.g. "Embedded Engine", "Termux Local", "Remote Cloud"). */
    val displayName: String

    /** Set of normalized language names supported by this runner. */
    val supportedLanguages: Set<String>

    /**
     * Checks if this runner is currently available on the device.
     */
    suspend fun isAvailable(): Boolean

    /**
     * Runs the requested code and streams [RunEvent]s until completion or cancellation.
     */
    fun run(request: RunRequest): Flow<RunEvent>

    /**
     * Sends stdin input into the active process or session, if supported.
     */
    fun sendInput(input: String) {}

    /**
     * Stops the running execution immediately and performs cleanup.
     */
    fun stop()
}
