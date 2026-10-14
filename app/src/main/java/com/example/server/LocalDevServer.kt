package com.example.server

import java.io.File

data class ServerStatus(
    val isRunning: Boolean = false,
    val portOrNull: Int? = null,
    val rootDir: File? = null
)

interface LocalDevServer {
    val status: ServerStatus
    fun start(rootDir: File, port: Int = 8080): Boolean
    fun stop()
    fun restart(): Boolean
}
