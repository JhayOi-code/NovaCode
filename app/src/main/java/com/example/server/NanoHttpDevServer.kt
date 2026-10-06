package com.example.server

import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicLong

class NanoHttpDevServer : LocalDevServer {

    private val _status = MutableStateFlow<ServerStatus>(ServerStatus.Stopped)
    override val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private val _requestLogs = MutableStateFlow<List<ServerRequestLog>>(emptyList())
    override val requestLogs: StateFlow<List<ServerRequestLog>> = _requestLogs.asStateFlow()

    private var activeServer: InternalNanoServer? = null
    private val logIdGenerator = AtomicLong(1)

    @Synchronized
    override fun start(rootDir: File, preferredPort: Int): ServerStartResult {
        stop()

        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }

        val availablePort = findAvailablePort(preferredPort)
        if (availablePort <= 0) {
            val errorMsg = "Unable to find an open port for the local dev server"
            _status.value = ServerStatus.Error(errorMsg)
            return ServerStartResult.Failure(errorMsg)
        }

        return try {
            val server = InternalNanoServer(availablePort, rootDir) { log ->
                _requestLogs.update { current ->
                    (listOf(log) + current).take(100)
                }
            }
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            activeServer = server

            val assignedPort = server.listeningPort
            val url = "http://127.0.0.1:$assignedPort/"
            val runningState = ServerStatus.Running(assignedPort, url, rootDir)
            _status.value = runningState
            ServerStartResult.Success(assignedPort, url, rootDir)
        } catch (e: Exception) {
            val errorMsg = "Failed to launch local HTTP server: ${e.message}"
            _status.value = ServerStatus.Error(errorMsg)
            ServerStartResult.Failure(errorMsg)
        }
    }

    @Synchronized
    override fun stop() {
        try {
            activeServer?.stop()
        } catch (_: Exception) {
        } finally {
            activeServer = null
            _status.value = ServerStatus.Stopped
        }
    }

    @Synchronized
    override fun restart(rootDir: File, preferredPort: Int): ServerStartResult {
        stop()
        return start(rootDir, preferredPort)
    }

    override fun clearLogs() {
        _requestLogs.value = emptyList()
    }

    private fun findAvailablePort(preferred: Int): Int {
        val candidates = listOf(preferred, 8080, 8081, 8082, 8088, 3000, 5000, 8888, 0)
        for (port in candidates) {
            try {
                val socket = ServerSocket(port)
                val boundPort = socket.localPort
                socket.close()
                return boundPort
            } catch (_: IOException) {
                // Try next candidate
            }
        }
        return -1
    }

    private inner class InternalNanoServer(
        port: Int,
        private val rootDir: File,
        private val onLog: (ServerRequestLog) -> Unit
    ) : NanoHTTPD("127.0.0.1", port) {

        override fun serve(session: IHTTPSession): Response {
            val startTime = System.currentTimeMillis()
            val method = session.method.name
            val rawUri = session.uri
            var statusCode = 200

            val response = try {
                if (session.method == Method.OPTIONS) {
                    newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "")
                } else {
                    serveFile(rawUri)
                }
            } catch (e: Exception) {
                statusCode = 500
                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    MIME_PLAINTEXT,
                    "Internal Server Error: ${e.message}"
                )
            }

            statusCode = response.status.requestStatus

            // Add standard CORS and cache control headers
            response.addHeader("Access-Control-Allow-Origin", "*")
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
            response.addHeader("Access-Control-Allow-Headers", "*")
            response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
            response.addHeader("Server", "LinqStudio-DevServer/1.0 (NanoHTTPD)")

            val duration = System.currentTimeMillis() - startTime
            val clientIp = session.headers["http-client-ip"]
                ?: session.headers["remote-addr"]
                ?: "127.0.0.1"

            onLog(
                ServerRequestLog(
                    id = logIdGenerator.getAndIncrement(),
                    method = method,
                    uri = rawUri,
                    statusCode = statusCode,
                    durationMs = duration,
                    clientIp = clientIp
                )
            )

            return response
        }

        private fun serveFile(uri: String): Response {
            val decodedUri = try {
                URLDecoder.decode(uri, "UTF-8")
            } catch (_: Exception) {
                uri
            }

            val cleanPath = decodedUri.removePrefix("/").ifEmpty { "." }
            val target = File(rootDir, cleanPath).canonicalFile

            // Path traversal security check
            if (!target.absolutePath.startsWith(rootDir.canonicalPath)) {
                return newFixedLengthResponse(
                    Response.Status.FORBIDDEN,
                    MIME_PLAINTEXT,
                    "Access Denied: Path traversal is restricted"
                )
            }

            if (!target.exists()) {
                return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    MIME_HTML,
                    createNotFoundHtml(decodedUri)
                )
            }

            if (target.isDirectory) {
                val indexFile = listOf("index.html", "index.htm", "main.html")
                    .map { File(target, it) }
                    .firstOrNull { it.exists() && it.isFile }

                if (indexFile != null) {
                    return deliverFile(indexFile, "text/html; charset=utf-8")
                }

                // Auto-generate directory listing
                return newFixedLengthResponse(
                    Response.Status.OK,
                    MIME_HTML,
                    createDirectoryListingHtml(target, decodedUri)
                )
            }

            val mimeType = getMimeTypeForPath(target.name)
            return deliverFile(target, mimeType)
        }

        private fun deliverFile(file: File, mimeType: String): Response {
            return try {
                val fis = FileInputStream(file)
                newFixedLengthResponse(Response.Status.OK, mimeType, fis, file.length())
            } catch (e: Exception) {
                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    MIME_PLAINTEXT,
                    "Could not read file: ${e.message}"
                )
            }
        }

        private fun getMimeTypeForPath(fileName: String): String {
            val ext = fileName.substringAfterLast('.', "").lowercase()
            return when (ext) {
                "html", "htm" -> "text/html; charset=utf-8"
                "css" -> "text/css; charset=utf-8"
                "js", "mjs" -> "application/javascript; charset=utf-8"
                "json" -> "application/json; charset=utf-8"
                "svg" -> "image/svg+xml"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "ico" -> "image/x-icon"
                "webp" -> "image/webp"
                "woff" -> "font/woff"
                "woff2" -> "font/woff2"
                "ttf" -> "font/ttf"
                "mp4" -> "video/mp4"
                "mp3" -> "audio/mpeg"
                "xml" -> "application/xml"
                "txt", "md", "py", "kt", "ts", "java", "c", "cpp" -> "text/plain; charset=utf-8"
                else -> "application/octet-stream"
            }
        }

        private fun createNotFoundHtml(uri: String): String {
            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>404 Not Found - Linq Studio Dev Server</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0F1218; color: #E2E8F0; padding: 40px 20px; text-align: center; }
                        h1 { color: #F43F5E; font-size: 28px; margin-bottom: 8px; }
                        p { color: #94A3B8; font-size: 15px; }
                        .uri { font-family: monospace; background: #1E293B; padding: 4px 8px; border-radius: 4px; color: #818CF8; }
                        .badge { display: inline-block; margin-top: 24px; padding: 6px 12px; background: #161D2C; border: 1px solid #272E3D; border-radius: 20px; font-size: 12px; color: #64748B; }
                    </style>
                </head>
                <body>
                    <h1>404 Not Found</h1>
                    <p>The requested file <span class="uri">$uri</span> does not exist in the active workspace root.</p>
                    <div class="badge">Linq Studio Local Dev Server</div>
                </body>
                </html>
            """.trimIndent()
        }

        private fun createDirectoryListingHtml(dir: File, currentUri: String): String {
            val files = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
            val parentPath = if (currentUri != "/" && currentUri.isNotEmpty()) {
                currentUri.substringBeforeLast('/', "").ifEmpty { "/" }
            } else null

            val rows = StringBuilder()
            if (parentPath != null) {
                rows.append("<li><a class=\"folder\" href=\"$parentPath\">📁 .. (Parent Directory)</a></li>")
            }

            for (f in files) {
                val name = f.name + (if (f.isDirectory) "/" else "")
                val href = (currentUri.trimEnd('/') + "/" + f.name).replace("//", "/")
                val icon = if (f.isDirectory) "📁" else "📄"
                val cssClass = if (f.isDirectory) "folder" else "file"
                val size = if (f.isFile) "(${f.length()} bytes)" else ""
                rows.append("<li><a class=\"$cssClass\" href=\"$href\">$icon $name</a> <span class=\"size\">$size</span></li>")
            }

            return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Index of $currentUri - Linq Studio Dev Server</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0F1218; color: #E2E8F0; padding: 24px; margin: 0; }
                        h1 { color: #818CF8; font-size: 20px; margin-bottom: 16px; border-bottom: 1px solid #272E3D; padding-bottom: 8px; }
                        ul { list-style: none; padding: 0; margin: 0; }
                        li { padding: 8px 12px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
                        li:hover { background: #161D2C; }
                        a { color: #F1F5F9; text-decoration: none; font-size: 14px; font-family: monospace; }
                        a.folder { color: #FBBF24; font-weight: bold; }
                        a:hover { color: #818CF8; }
                        .size { color: #64748B; font-size: 12px; font-family: monospace; }
                        .footer { margin-top: 24px; padding-top: 12px; border-top: 1px solid #272E3D; font-size: 12px; color: #64748B; }
                    </style>
                </head>
                <body>
                    <h1>📂 Directory: $currentUri</h1>
                    <ul>
                        $rows
                    </ul>
                    <div class="footer">Serving from workspace &bull; Linq Studio Local Dev Server</div>
                </body>
                </html>
            """.trimIndent()
        }
    }
}
