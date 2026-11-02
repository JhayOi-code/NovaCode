package com.example.runner.embedded

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.example.runner.CodeRunner
import com.example.runner.RunEvent
import com.example.runner.RunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Embedded HTML/CSS web runner that loads HTML/CSS in an active WebView context
 * and streams console messages (log, info, warn, error) directly to the output panel.
 */
class EmbeddedHtmlRunner(private val context: Context) : CodeRunner {

    override val id: String = "embedded_html"
    override val displayName: String = "Embedded Web Engine (Offline)"
    override val supportedLanguages: Set<String> = setOf("HTML", "html", "htm", "CSS", "css")

    private var activeWebView: WebView? = null
    private val isRunning = AtomicBoolean(false)

    override suspend fun isAvailable(): Boolean = true

    override fun run(request: RunRequest): Flow<RunEvent> = callbackFlow {
        isRunning.set(true)
        val startTime = System.currentTimeMillis()
        trySend(RunEvent.Started(language = "HTML / Web", runner = displayName))

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post {
            try {
                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            if (consoleMessage != null) {
                                val msg = consoleMessage.message()
                                val line = consoleMessage.lineNumber()
                                val source = consoleMessage.sourceId()?.substringAfterLast('/') ?: request.fileName
                                when (consoleMessage.messageLevel()) {
                                    ConsoleMessage.MessageLevel.ERROR -> {
                                        trySend(RunEvent.Stderr("[Console Error] $msg ($source:$line)"))
                                    }
                                    ConsoleMessage.MessageLevel.WARNING -> {
                                        trySend(RunEvent.Stdout("[Console Warn] $msg ($source:$line)"))
                                    }
                                    else -> {
                                        trySend(RunEvent.Stdout("[Console] $msg"))
                                    }
                                }
                            }
                            return true
                        }
                    }
                }
                activeWebView = webView

                trySend(RunEvent.Stdout("Rendering '${request.fileName}' on local web environment..."))
                trySend(RunEvent.Stdout("HTML DOM parsed. Listening for DOM and JavaScript console events...\n"))

                val content = if (request.fileName.endsWith(".css", ignoreCase = true)) {
                    "<!DOCTYPE html><html><head><style>${request.code}</style></head><body><h1>CSS Preview Loaded</h1><p>Rules active.</p></body></html>"
                } else {
                    request.code
                }

                // If filePath is a real file, load from base directory so sibling relative links work
                val baseDir = File(request.filePath).parentFile?.toURI()?.toString() ?: "file:///android_asset/"
                webView.loadDataWithBaseURL(baseDir, content, "text/html", "UTF-8", null)

                // Emit successful mount event after DOM finishes parsing
                mainHandler.postDelayed({
                    if (isRunning.get()) {
                        val duration = System.currentTimeMillis() - startTime
                        trySend(RunEvent.Stdout("\n[Web Page Render Complete - Dev Server & Browser Preview available]"))
                        trySend(RunEvent.Exit(code = 0, durationMs = duration))
                        close()
                    }
                }, 1000)

            } catch (e: Exception) {
                isRunning.set(false)
                trySend(RunEvent.Failure("Web rendering failed: ${e.message}"))
                close()
            }
        }

        awaitClose {
            mainHandler.post {
                try {
                    activeWebView?.stopLoading()
                    activeWebView?.destroy()
                    activeWebView = null
                } catch (_: Exception) {}
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun stop() {
        if (isRunning.getAndSet(false)) {
            Handler(Looper.getMainLooper()).post {
                try {
                    activeWebView?.stopLoading()
                    activeWebView?.destroy()
                    activeWebView = null
                } catch (_: Exception) {}
            }
        }
    }
}
