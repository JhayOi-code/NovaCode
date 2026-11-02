package com.example.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
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

                val canonicalRoot = rootDir.canonicalFile
                var targetFile = if (decodedUri.isEmpty() || decodedUri == "/") {
                    File(canonicalRoot, "index.html")
                } else {
                    File(canonicalRoot, decodedUri)
                }

                var canonicalTarget = targetFile.canonicalFile

                // Path traversal protection: block paths escaping rootDir
                if (!canonicalTarget.path.startsWith(canonicalRoot.path)) {
                    val forbiddenHtml = """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head><meta charset="UTF-8"><title>403 Forbidden - Linq Studio</title><style>body{background:#0B0F17;color:#EF4444;font-family:sans-serif;padding:32px;text-align:center;}a{color:#00E5FF;text-decoration:none;font-weight:bold;}</style></head>
                        <body>
                          <h2>403 - Access Denied</h2>
                          <p>Path traversal attempts outside the workspace are blocked.</p>
                          <p><a href="/">Return to Home</a></p>
                        </body>
                        </html>
                    """.trimIndent()
                    sendResponse(out, 403, "Forbidden", "text/html; charset=UTF-8", forbiddenHtml.toByteArray(Charsets.UTF_8))
                    return
                }

                // If target is a directory or path requested is root
                if (canonicalTarget.isDirectory) {
                    val indexInDir = File(canonicalTarget, "index.html")
                    if (indexInDir.exists() && indexInDir.isFile) {
                        sendResponse(out, 200, "OK", "text/html; charset=UTF-8", indexInDir.readBytes(), "no-cache")
                        return
                    } else {
                        // Check if root index.html exists for SPA fallback
                        val rootIndex = File(canonicalRoot, "index.html")
                        if (rootIndex.exists() && rootIndex.isFile) {
                            sendResponse(out, 200, "OK", "text/html; charset=UTF-8", rootIndex.readBytes(), "no-cache")
                            return
                        }
                        // Otherwise serve auto-generated Directory Listing
                        val relativePath = canonicalTarget.toRelativeString(canonicalRoot).replace('\\', '/').let {
                            if (it.isEmpty()) "/" else "/$it"
                        }
                        val files = canonicalTarget.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
                        val rowsHtml = files.joinToString("\n") { f ->
                            val icon = if (f.isDirectory) "📁" else when (f.extension.lowercase()) {
                                "html", "htm" -> "🌐"
                                "css" -> "🎨"
                                "js", "mjs", "ts" -> "⚡"
                                "json" -> "📦"
                                "png", "jpg", "jpeg", "svg", "ico", "webp" -> "🖼️"
                                else -> "📄"
                            }
                            val href = (if (relativePath == "/") "" else relativePath) + "/" + f.name + (if (f.isDirectory) "/" else "")
                            val sizeStr = if (f.isDirectory) "-" else "${f.length()} B"
                            """<tr>
                                <td>$icon <a href="$href">${f.name}${if (f.isDirectory) "/" else ""}</a></td>
                                <td style="text-align:right;color:#94a3b8;">$sizeStr</td>
                            </tr>"""
                        }
                        val listingHtml = """
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                              <meta charset="UTF-8">
                              <meta name="viewport" content="width=device-width, initial-scale=1.0">
                              <title>Index of $relativePath - Linq Studio Dev Server</title>
                              <style>
                                body { background: #0B0F17; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; padding: 24px; max-width: 760px; margin: 0 auto; }
                                h1 { color: #00E5FF; font-size: 20px; border-bottom: 1px solid #1E293B; padding-bottom: 12px; }
                                table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 14px; }
                                th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #1E293B; }
                                th { color: #94A3B8; font-weight: 600; }
                                a { color: #38BDF8; text-decoration: none; }
                                a:hover { text-decoration: underline; color: #7DD3FC; }
                                .footer { margin-top: 24px; font-size: 12px; color: #64748B; text-align: center; }
                              </style>
                            </head>
                            <body>
                              <h1>📁 Directory: $relativePath</h1>
                              <table>
                                <thead><tr><th>Name</th><th style="text-align:right;">Size</th></tr></thead>
                                <tbody>
                                  ${if (relativePath != "/") "<tr><td>↩️ <a href=\"..\">../ (Parent Directory)</a></td><td style=\"text-align:right;\">-</td></tr>" else ""}
                                  $rowsHtml
                                </tbody>
                              </table>
                              <div class="footer">Linq Studio Local Dev Server (Port 8080)</div>
                            </body>
                            </html>
                        """.trimIndent()
                        sendResponse(out, 200, "OK", "text/html; charset=UTF-8", listingHtml.toByteArray(Charsets.UTF_8), "no-cache")
                        return
                    }
                }

                // Single Page Application (SPA) Fallback for non-file routes:
                // If the target file doesn't exist and does not have a static file extension, serve index.html
                val hasFileExtension = canonicalTarget.name.contains('.') && !canonicalTarget.name.endsWith('.')
                if (!canonicalTarget.exists()) {
                    val rootIndex = File(canonicalRoot, "index.html")
                    if (!hasFileExtension && rootIndex.exists() && rootIndex.isFile) {
                        // SPA fallback: Return index.html for all in-app routes, ?signin=..., deep links
                        sendResponse(out, 200, "OK", "text/html; charset=UTF-8", rootIndex.readBytes(), "no-cache")
                        return
                    }

                    // Otherwise, provide a themed friendly 404 page
                    val notFoundHtml = """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                          <meta charset="UTF-8">
                          <meta name="viewport" content="width=device-width, initial-scale=1.0">
                          <title>Page Not Found - Linq Studio</title>
                          <style>
                            body { background: #0B0F17; color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; padding: 40px 20px; text-align: center; }
                            .box { background: #1E293B; border: 1px solid #334155; border-radius: 16px; padding: 36px 24px; max-width: 480px; margin: 0 auto; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
                            h1 { color: #EF4444; font-size: 36px; margin: 0 0 8px 0; }
                            h2 { color: #F8FAFC; font-size: 18px; margin: 0 0 12px 0; }
                            p { color: #94A3B8; font-size: 14px; line-height: 1.6; margin: 12px 0 24px 0; }
                            code { background: #0F172A; color: #FBBF24; padding: 3px 8px; border-radius: 6px; font-size: 13px; }
                            .btn { display: inline-block; background: #00E5FF; color: #000; padding: 12px 24px; border-radius: 10px; font-weight: 700; text-decoration: none; font-size: 14px; }
                            .btn:hover { background: #38BDF8; }
                          </style>
                        </head>
                        <body>
                          <div class="box">
                            <h1>404</h1>
                            <h2>Page Not Found</h2>
                            <p>The requested URL <code>/$decodedUri</code> does not exist on this server.</p>
                            <a href="/" class="btn">Go to Home</a>
                          </div>
                        </body>
                        </html>
                    """.trimIndent()
                    sendResponse(out, 404, "Not Found", "text/html; charset=UTF-8", notFoundHtml.toByteArray(Charsets.UTF_8), "no-cache")
                    return
                }

                val mimeType = when (canonicalTarget.extension.lowercase()) {
                    "html", "htm" -> "text/html; charset=UTF-8"
                    "css" -> "text/css; charset=UTF-8"
                    "js", "mjs" -> "application/javascript; charset=UTF-8"
                    "json" -> "application/json; charset=UTF-8"
                    "svg" -> "image/svg+xml"
                    "png" -> "image/png"
                    "jpg", "jpeg" -> "image/jpeg"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    "ico" -> "image/x-icon"
                    "woff" -> "font/woff"
                    "woff2" -> "font/woff2"
                    "ttf" -> "font/ttf"
                    "map" -> "application/json; charset=UTF-8"
                    "txt", "md" -> "text/plain; charset=UTF-8"
                    "xml" -> "application/xml; charset=UTF-8"
                    "wasm" -> "application/wasm"
                    else -> "application/octet-stream"
                }

                val cacheControl = when (canonicalTarget.extension.lowercase()) {
                    "html", "htm" -> "no-cache, must-revalidate"
                    "css", "js", "png", "jpg", "jpeg", "svg", "ico", "woff", "woff2", "ttf" -> "public, max-age=86400"
                    else -> "public, max-age=3600"
                }

                val fileBytes = canonicalTarget.readBytes()
                sendResponse(out, 200, "OK", mimeType, fileBytes, cacheControl)
            } catch (_: Exception) {}
        }
    }

    private fun sendResponse(
        out: OutputStream,
        code: Int,
        statusMsg: String,
        contentType: String,
        data: ByteArray,
        cacheControl: String = "no-cache"
    ) {
        val header = "HTTP/1.1 $code $statusMsg\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${data.size}\r\n" +
                "Cache-Control: $cacheControl\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(data)
        out.flush()
    }
}
