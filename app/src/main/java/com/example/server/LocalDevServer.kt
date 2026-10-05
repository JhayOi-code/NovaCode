package com.example.server

import kotlinx.coroutines.flow.StateFlow
import java.io.File

data class ServerRequestLog(
    val id: Long,
    val method: String,
    val uri: String,
    val statusCode: Int,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val clientIp: String = "127.0.0.1"
)

sealed class ServerStatus {
    object Stopped : ServerStatus()
    data class Running(val port: Int, val url: String, val rootDir: File) : ServerStatus()
    data class Error(val message: String) : ServerStatus()

    val isRunning: Boolean get() = this is Running
    val urlOrNull: String? get() = (this as? Running)?.url
    val portOrNull: Int? get() = (this as? Running)?.port
}

sealed class ServerStartResult {
    data class Success(val port: Int, val url: String, val rootDir: File) : ServerStartResult()
    data class Failure(val error: String) : ServerStartResult()
}

interface LocalDevServer {
    val status: StateFlow<ServerStatus>
    val requestLogs: StateFlow<List<ServerRequestLog>>

    fun start(rootDir: File, preferredPort: Int = 8080): ServerStartResult
    fun stop()
    fun restart(rootDir: File, preferredPort: Int = 8080): ServerStartResult
    fun clearLogs()
}
