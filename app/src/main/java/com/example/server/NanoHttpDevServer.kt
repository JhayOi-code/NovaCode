package com.example.server

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class NanoHttpDevServer : LocalDevServer {

    private var nanoServer: InternalServer? = null
    private var currentPort = 8080
    private var currentRoot: File? = null

    override var status: ServerStatus = ServerStatus()
        private set

    override fun start(rootDir: File, port: Int): Boolean {
        stop()
        return try {
            currentPort = port
            currentRoot = rootDir
            nanoServer = InternalServer(rootDir, port).apply {
                start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            }
            status = ServerStatus(isRunning = true, portOrNull = port, rootDir = rootDir)
            true
        } catch (_: Exception) {
            status = ServerStatus(isRunning = false, portOrNull = null, rootDir = null)
            false
        }
    }

    override fun stop() {
        try {
            nanoServer?.stop()
        } catch (_: Exception) {
        } finally {
            nanoServer = null
            status = ServerStatus(isRunning = false, portOrNull = null, rootDir = null)
        }
    }

    override fun restart(): Boolean {
        val root = currentRoot ?: return false
        return start(root, currentPort)
    }

    private class InternalServer(private val rootDir: File, port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri.trimStart('/')
            val targetFile = if (uri.isEmpty() || uri == "/") {
                File(rootDir, "index.html")
            } else {
                File(rootDir, uri)
            }

            if (!targetFile.exists() || targetFile.isDirectory) {
                val indexInDir = File(targetFile, "index.html")
                if (indexInDir.exists()) {
                    return newFixedLengthResponse(Response.Status.OK, "text/html", indexInDir.readText())
                }
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found: $uri")
            }

            val mimeType = when (targetFile.extension.lowercase()) {
                "html", "htm" -> "text/html"
                "css" -> "text/css"
                "js" -> "application/javascript"
                "json" -> "application/json"
                "svg" -> "image/svg+xml"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "txt" -> "text/plain"
                else -> "application/octet-stream"
            }

            return try {
                val stream = FileInputStream(targetFile)
                newChunkedResponse(Response.Status.OK, mimeType, stream)
            } catch (_: Exception) {
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error reading file")
            }
        }
    }
}
