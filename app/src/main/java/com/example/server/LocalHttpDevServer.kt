package com.example.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder

class LocalHttpDevServer : LocalDevServer {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var currentPort = 8080
    private var currentRoot: File? = null

    override var status: ServerStatus = ServerStatus()
        private set

    override fun start(rootDir: File, port: Int): Boolean {
        stop()
        currentPort = port
        currentRoot = rootDir

        return try {
            val socket = ServerSocket(port)
            serverSocket = socket
            status = ServerStatus(isRunning = true, portOrNull = port, rootDir = rootDir)

            serverJob = scope.launch {
                while (!socket.isClosed) {
                    try {
                        val client = socket.accept()
                        launch { handleClient(client, rootDir) }
                    } catch (_: Exception) {
                        break
                    }
                }
            }
            true
        } catch (_: Exception) {
            status = ServerStatus(isRunning = false, portOrNull = null, rootDir = null)
            false
        }
    }

    override fun stop() {
        try {
            serverJob?.cancel()
            serverSocket?.close()
        } catch (_: Exception) {
        } finally {
            serverSocket = null
            serverJob = null
            status = ServerStatus(isRunning = false, portOrNull = null, rootDir = null)
        }
    }

    override fun restart(): Boolean {
        val root = currentRoot ?: return false
        return start(root, currentPort)
    }

    private fun handleClient(client: Socket, rootDir: File) {
        client.use { socket ->
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val out = socket.getOutputStream()
                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val rawUri = parts[1].substringBefore("?")
                val decodedUri = URLDecoder.decode(rawUri, "UTF-8").trimStart('/')

                val targetFile = if (decodedUri.isEmpty() || decodedUri == "/") {
                    File(rootDir, "index.html")
                } else {
                    File(rootDir, decodedUri)
                }

                if (!targetFile.exists() || targetFile.isDirectory) {
                    val indexInDir = File(targetFile, "index.html")
                    if (indexInDir.exists()) {
                        sendResponse(out, 200, "OK", "text/html", indexInDir.readBytes())
                    } else {
                        val notFoundHtml = """
                            <!DOCTYPE html>
                            <html>
                            <head><title>404 Not Found</title><style>body{background:#0F172A;color:#E2E8F0;font-family:sans-serif;padding:30px;}</style></head>
                            <body>
                              <h2>404 - File Not Found</h2>
                              <p>The requested path <code>/$decodedUri</code> does not exist in workspace.</p>
                            </body>
                            </html>
                        """.trimIndent()
                        sendResponse(out, 404, "Not Found", "text/html", notFoundHtml.toByteArray(Charsets.UTF_8))
                    }
                    return
                }

                val mimeType = when (targetFile.extension.lowercase()) {
                    "html", "htm" -> "text/html; charset=UTF-8"
                    "css" -> "text/css; charset=UTF-8"
                    "js" -> "application/javascript; charset=UTF-8"
                    "json" -> "application/json; charset=UTF-8"
                    "svg" -> "image/svg+xml"
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    "ico" -> "image/x-icon"
                    "txt" -> "text/plain; charset=UTF-8"
                    else -> "application/octet-stream"
                }

                val fileBytes = targetFile.readBytes()
                sendResponse(out, 200, "OK", mimeType, fileBytes)
            } catch (_: Exception) {}
        }
    }

    private fun sendResponse(out: OutputStream, code: Int, statusMsg: String, contentType: String, data: ByteArray) {
        val header = "HTTP/1.1 $code $statusMsg\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${data.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(data)
        out.flush()
    }
}
