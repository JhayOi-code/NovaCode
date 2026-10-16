package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EditorTheme
import com.example.server.ServerStatus

enum class DeviceFrame(val label: String, val maxWidthDp: Int) {
    MOBILE("Mobile", 380),
    TABLET("Tablet", 600),
    FIT_DEVICE("Fit Screen", Int.MAX_VALUE)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPreviewDialog(
    theme: EditorTheme,
    serverUrl: String?,
    htmlContent: String? = null,
    serverStatus: ServerStatus,
    onRestartServer: () -> Unit,
    onClose: () -> Unit,
    onConsoleLog: ((String, String, String?, Int?) -> Unit)? = null
) {
    var activeFrame by remember { mutableStateOf(DeviceFrame.FIT_DEVICE) }
    var isSourceViewMode by remember { mutableStateOf(false) } // Default: Result View
    var isFullScreenPreview by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreenPreview) 0.dp else 6.dp)
                .testTag("web_preview_dialog"),
            shape = RoundedCornerShape(if (isFullScreenPreview) 0.dp else 14.dp),
            color = theme.surfaceColor,
            border = if (isFullScreenPreview) null else androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Toolbar (Scrollable to prevent overlapping on any device size)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Web Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )

                    // Status Badge
                    val isRunning = serverStatus.isRunning
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRunning) Color(0xFF14532D) else Color(0xFF334155)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) Color(0xFF22C55E) else Color(0xFF94A3B8))
                            )
                            Text(
                                text = if (isRunning) "HTTP :${serverStatus.portOrNull ?: 8080}" else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Result vs Source View Switcher
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.editorBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isSourceViewMode) theme.primaryColor else Color.Transparent,
                                modifier = Modifier
                                    .clickable { isSourceViewMode = false }
                                    .testTag("tab_preview_result")
                            ) {
                                Text(
                                    text = "Result",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSourceViewMode) Color.White else theme.lineNumberColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSourceViewMode) theme.primaryColor else Color.Transparent,
                                modifier = Modifier
                                    .clickable { isSourceViewMode = true }
                                    .testTag("tab_preview_code")
                            ) {
                                Text(
                                    text = "Source",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSourceViewMode) Color.White else theme.lineNumberColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Device Frame mode toggles
                    IconButton(
                        onClick = { activeFrame = DeviceFrame.MOBILE },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = "Mobile View",
                            tint = if (activeFrame == DeviceFrame.MOBILE) theme.primaryColor else theme.lineNumberColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { activeFrame = DeviceFrame.TABLET },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Tablet,
                            contentDescription = "Tablet View",
                            tint = if (activeFrame == DeviceFrame.TABLET) theme.primaryColor else theme.lineNumberColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { activeFrame = DeviceFrame.FIT_DEVICE },
                        modifier = Modifier.size(28.dp).testTag("btn_fit_device_frame")
                    ) {
                        Icon(
                            Icons.Default.FitScreen,
                            contentDescription = "Fit & Match Screen",
                            tint = if (activeFrame == DeviceFrame.FIT_DEVICE) Color(0xFF10B981) else theme.lineNumberColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Full Screen Button
                    IconButton(
                        onClick = { isFullScreenPreview = !isFullScreenPreview },
                        modifier = Modifier.size(28.dp).testTag("btn_preview_fullscreen")
                    ) {
                        Icon(
                            imageVector = if (isFullScreenPreview) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullScreenPreview) "Exit Full Screen" else "Full Screen",
                            tint = if (isFullScreenPreview) Color(0xFF10B981) else theme.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onRestartServer, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = theme.borderColor)

                // Viewport Container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                        .padding(if (isFullScreenPreview) 0.dp else 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val widthModifier = if (activeFrame == DeviceFrame.FIT_DEVICE || isFullScreenPreview) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .widthIn(max = activeFrame.maxWidthDp.dp)
                            .fillMaxHeight()
                    }

                    Box(
                        modifier = widthModifier
                            .clip(RoundedCornerShape(if (isFullScreenPreview) 0.dp else 8.dp))
                            .border(if (isFullScreenPreview) 0.dp else 1.dp, Color(0xFF334155), RoundedCornerShape(if (isFullScreenPreview) 0.dp else 8.dp))
                            .background(Color.White)
                    ) {
                        if (!isSourceViewMode) {
                            // RENDERED WEBVIEW RESULT (Configured with software rendering and safe cache)
                            AndroidView(
                                factory = { ctx ->
                                    try {
                                        File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/js").mkdirs()
                                        File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm").mkdirs()
                                    } catch (_: Exception) {}

                                    WebView(ctx).apply {
                                        // Disable hardware acceleration to avoid MESA rendernode failure on emulator
                                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.allowFileAccess = true
                                        settings.databaseEnabled = true
                                        settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

                                        webViewClient = object : WebViewClient() {
                                            override fun onRenderProcessGone(
                                                view: WebView?,
                                                detail: android.webkit.RenderProcessGoneDetail?
                                            ): Boolean {
                                                return true
                                            }
                                        }

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                                if (consoleMessage != null) {
                                                    val level = when (consoleMessage.messageLevel()) {
                                                        ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                                        ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                                        ConsoleMessage.MessageLevel.LOG -> "LOG"
                                                        ConsoleMessage.MessageLevel.TIP -> "INFO"
                                                        else -> "DEBUG"
                                                    }
                                                    onConsoleLog?.invoke(
                                                        level,
                                                        consoleMessage.message(),
                                                        consoleMessage.sourceId(),
                                                        consoleMessage.lineNumber()
                                                    )
                                                }
                                                return true
                                            }
                                        }

                                        if (serverUrl != null) {
                                            loadUrl(serverUrl)
                                        } else if (htmlContent != null) {
                                            loadDataWithBaseURL("http://localhost:8080/", htmlContent, "text/html", "UTF-8", null)
                                        }
                                    }
                                },
                                update = { webView ->
                                    if (serverUrl != null) {
                                        webView.loadUrl(serverUrl)
                                    } else if (htmlContent != null) {
                                        webView.loadDataWithBaseURL("http://localhost:8080/", htmlContent, "text/html", "UTF-8", null)
                                    }
                                },
                                modifier = Modifier.fillMaxSize().testTag("webview_rendered_result")
                            )
                        } else {
                            // RAW SOURCE CODE VIEW
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF0B0F17)
                            ) {
                                Text(
                                    text = htmlContent ?: "Source loaded via HTTP Dev Server at $serverUrl",
                                    color = Color(0xFF86EFAC),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        // Floating exit full screen toggle when in full screen preview
                        if (isFullScreenPreview) {
                            SmallFloatingActionButton(
                                onClick = { isFullScreenPreview = false },
                                containerColor = theme.surfaceColor.copy(alpha = 0.92f),
                                contentColor = Color(0xFF10B981),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Full Screen", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
