package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EditorTheme

enum class DeviceFrame(val label: String, val widthDp: Int?) {
    MOBILE("Phone", 360),
    TABLET("Tablet", 600),
    DESKTOP("Full Screen", null)
}

@Composable
fun WebPreviewDialog(
    theme: EditorTheme,
    htmlContent: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var activeFrame by remember { mutableStateOf(DeviceFrame.DESKTOP) }
    var showConsole by remember { mutableStateOf(false) }
    val consoleLogs = remember { mutableStateListOf<String>() }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .testTag("web_preview_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Live Web Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = theme.onSurfaceColor
                        )

                        // Device Frame Switchers
                        IconButton(
                            onClick = { activeFrame = DeviceFrame.MOBILE },
                            modifier = Modifier.size(32.dp).testTag("frame_phone")
                        ) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = "Mobile Frame",
                                tint = if (activeFrame == DeviceFrame.MOBILE) theme.primaryColor else theme.lineNumberColor
                            )
                        }

                        IconButton(
                            onClick = { activeFrame = DeviceFrame.TABLET },
                            modifier = Modifier.size(32.dp).testTag("frame_tablet")
                        ) {
                            Icon(
                                Icons.Default.Tablet,
                                contentDescription = "Tablet Frame",
                                tint = if (activeFrame == DeviceFrame.TABLET) theme.primaryColor else theme.lineNumberColor
                            )
                        }

                        IconButton(
                            onClick = { activeFrame = DeviceFrame.DESKTOP },
                            modifier = Modifier.size(32.dp).testTag("frame_desktop")
                        ) {
                            Icon(
                                Icons.Default.Laptop,
                                contentDescription = "Full Screen Frame",
                                tint = if (activeFrame == DeviceFrame.DESKTOP) theme.primaryColor else theme.lineNumberColor
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Refresh
                        IconButton(
                            onClick = {
                                webViewRef?.loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.testTag("btn_refresh_preview")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = theme.primaryColor)
                        }

                        // Toggle Console
                        IconButton(
                            onClick = { showConsole = !showConsole },
                            modifier = Modifier.testTag("btn_toggle_console")
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = "Console",
                                tint = if (showConsole) theme.primaryColor else theme.lineNumberColor
                            )
                        }

                        // Close
                        IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_preview")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.onSurfaceColor)
                        }
                    }
                }

                // Preview Body (WebView container centered according to frame)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF030712)),
                    contentAlignment = Alignment.Center
                ) {
                    val widthModifier = if (activeFrame.widthDp != null) {
                        Modifier
                            .width(activeFrame.widthDp!!.dp)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp)
                            .border(2.dp, theme.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    } else {
                        Modifier.fillMaxSize()
                    }

                    AndroidView(
                        modifier = widthModifier.background(Color.White),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true

                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                                        msg?.let {
                                            consoleLogs.add("[${it.messageLevel()}] ${it.message()} (${it.lineNumber()})")
                                        }
                                        return super.onConsoleMessage(msg)
                                    }
                                }

                                webViewClient = WebViewClient()
                                loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                                webViewRef = this
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                        }
                    )
                }

                // DevTools Console Drawer
                if (showConsole) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        color = Color(0xFF0B0F19),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Console Output (${consoleLogs.size})",
                                    color = theme.primaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                androidx.compose.material3.TextButton(onClick = { consoleLogs.clear() }) {
                                    Text("Clear", fontSize = 11.sp, color = theme.lineNumberColor)
                                }
                            }

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                if (consoleLogs.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Console is empty. console.log() output will appear here.",
                                            color = theme.lineNumberColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    items(consoleLogs) { log ->
                                        Text(
                                            text = log,
                                            color = if (log.contains("ERROR")) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
