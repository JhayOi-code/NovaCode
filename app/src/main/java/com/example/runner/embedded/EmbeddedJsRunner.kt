package com.example.runner.embedded

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
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
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Embedded JavaScript runtime utilizing Android's built-in Chromium V8 JavaScript engine.
 * Provides console.log/warn/error/info, setTimeout, JSON, Math, async execution,
 * and accurate runtime exception reporting with zero external dependencies.
 */
class EmbeddedJsRunner(private val context: Context) : CodeRunner {

    override val id: String = "embedded_javascript"
    override val displayName: String = "Embedded V8 JavaScript Engine (Offline)"
    override val supportedLanguages: Set<String> = setOf("JavaScript", "js", "mjs")

    private var activeWebView: WebView? = null
    private val isRunning = AtomicBoolean(false)

    override suspend fun isAvailable(): Boolean = true

    override fun run(request: RunRequest): Flow<RunEvent> = callbackFlow {
        isRunning.set(true)
        val startTime = System.currentTimeMillis()
        trySend(RunEvent.Started(language = "JavaScript", runner = displayName))

        val mainHandler = Handler(Looper.getMainLooper())

        // Create JS Interface Bridge
        class ConsoleBridge {
            @JavascriptInterface
            fun onLog(message: String) {
                trySend(RunEvent.Stdout(message))
            }

            @JavascriptInterface
            fun onWarn(message: String) {
                trySend(RunEvent.Stdout("[WARN] $message"))
            }

            @JavascriptInterface
            fun onError(message: String) {
                trySend(RunEvent.Stderr(message))
            }

            @JavascriptInterface
            fun onDone(exitCode: Int) {
                if (isRunning.getAndSet(false)) {
                    val duration = System.currentTimeMillis() - startTime
                    trySend(RunEvent.Exit(code = exitCode, durationMs = duration))
                    close()
                }
            }
        }

        mainHandler.post {
            try {
                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    addJavascriptInterface(ConsoleBridge(), "__linq_bridge__")
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            if (consoleMessage != null) {
                                val msg = consoleMessage.message()
                                val line = consoleMessage.lineNumber()
                                if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                    trySend(RunEvent.Stderr("$msg (line $line)"))
                                }
                            }
                            return true
                        }
                    }
                }
                activeWebView = webView

                // Prepare script with harness intercepting console and handling unhandled rejections
                val wrappedHtml = buildHtmlHarness(request.code, request.fileName)
                webView.loadDataWithBaseURL("https://linq.local/", wrappedHtml, "text/html", "UTF-8", null)

                // Schedule safety timeout
                mainHandler.postDelayed({
                    if (isRunning.getAndSet(false)) {
                        trySend(RunEvent.Stderr("Execution timed out (exceeded ${request.timeoutMs / 1000}s)."))
                        trySend(RunEvent.Exit(code = 124, durationMs = System.currentTimeMillis() - startTime))
                        close()
                    }
                }, request.timeoutMs)

            } catch (e: Exception) {
                isRunning.set(false)
                trySend(RunEvent.Failure("Failed to initialize JavaScript engine: ${e.message}"))
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

    private fun buildHtmlHarness(userCode: String, fileName: String): String {
        val escapedCode = userCode.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body>
            <script>
            (function() {
                var bridge = window.__linq_bridge__;
                function stringifyArg(a) {
                    if (a === undefined) return "undefined";
                    if (a === null) return "null";
                    if (typeof a === "object") {
                        try { return JSON.stringify(a, null, 2); } catch(e) { return String(a); }
                    }
                    return String(a);
                }

                console.log = function() {
                    var out = Array.prototype.slice.call(arguments).map(stringifyArg).join(" ");
                    if (bridge) bridge.onLog(out);
                };
                console.info = console.log;
                console.warn = function() {
                    var out = Array.prototype.slice.call(arguments).map(stringifyArg).join(" ");
                    if (bridge) bridge.onWarn(out);
                };
                console.error = function() {
                    var out = Array.prototype.slice.call(arguments).map(stringifyArg).join(" ");
                    if (bridge) bridge.onError(out);
                };

                window.onerror = function(msg, url, line, col, err) {
                    var trace = err && err.stack ? err.stack : (msg + " at line " + line);
                    if (bridge) {
                        bridge.onError(trace);
                        bridge.onDone(1);
                    }
                    return true;
                };

                window.onunhandledrejection = function(event) {
                    var reason = event.reason;
                    var text = (reason && reason.stack) ? reason.stack : String(reason);
                    if (bridge) {
                        bridge.onError("Unhandled Promise Rejection: " + text);
                        bridge.onDone(1);
                    }
                };

                try {
                    // Execute user program
                    eval(`$escapedCode`);
                    setTimeout(function() {
                        if (bridge) bridge.onDone(0);
                    }, 50);
                } catch(e) {
                    var msg = e && e.stack ? e.stack : (e.name + ": " + e.message);
                    if (bridge) {
                        bridge.onError(msg);
                        bridge.onDone(1);
                    }
                }
            })();
            </script>
            </body>
            </html>
        """.trimIndent()
    }
}
