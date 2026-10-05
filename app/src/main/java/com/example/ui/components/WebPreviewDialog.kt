package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EditorTheme
import com.example.server.ServerRequestLog
import com.example.server.ServerStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeviceFrame(val label: String, val widthDp: Int?) {
    MOBILE("Phone", 360),
    TABLET("Tablet", 600),
    DESKTOP("Full Screen", null)
}

@Composable
fun WebPreviewDialog(
    theme: EditorTheme,
    serverUrl: String?,
    htmlContent: String? = null,
    serverLogs: List<ServerRequestLog> = emptyList(),
    serverStatus: ServerStatus = ServerStatus.Stopped,
    onRestartServer: () -> Unit = {},
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var activeFrame by remember { mutableStateOf(DeviceFrame.DESKTOP) }
    var showBottomDrawer by remember { mutableStateOf(false) }
    var activeDrawerTab by remember { mutableIntStateOf(0) } // 0: Server Logs, 1: Browser Console
    val consoleLogs = remember { mutableStateListOf<String>() }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val activeUrl = serverUrl ?: "https://localhost/"

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
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Web Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor
                        )

                        // Server Live Status Badge
                        val isServerRunning = serverStatus.isRunning
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isServerRunning) Color(0xFF14532D) else Color(0xFF334155),
                            modifier = Modifier.padding(start = 4.dp).testTag("badge_server_status")
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
                                        .background(if (isServerRunning) Color(0xFF22C55E) else Color(0xFF94A3B8))
                                )
                                Text(
                                    text = if (isServerRunning) "HTTP LIVE :${serverStatus.portOrNull ?: 8080}" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isServerRunning) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Device Frame Switchers
                        IconButton(
                            onClick = { activeFrame = DeviceFrame.MOBILE },
                            modifier = Modifier.size(30.dp).testTag("frame_phone")
                        ) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = "Mobile Frame",
                                tint = if (activeFrame == DeviceFrame.MOBILE) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { activeFrame = DeviceFrame.TABLET },
                            modifier = Modifier.size(30.dp).testTag("frame_tablet")
                        ) {
                            Icon(
                                Icons.Default.Tablet,
                                contentDescription = "Tablet Frame",
                                tint = if (activeFrame == DeviceFrame.TABLET) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { activeFrame = DeviceFrame.DESKTOP },
                            modifier = Modifier.size(30.dp).testTag("frame_desktop")
                        ) {
                            Icon(
                                Icons.Default.Laptop,
                                contentDescription = "Full Screen Frame",
                                tint = if (activeFrame == DeviceFrame.DESKTOP) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Open in External Browser
                        if (serverUrl != null) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.size(32.dp).testTag("btn_open_external_browser")
                            ) {
                                Icon(
                                    Icons.Default.OpenInBrowser,
                                    contentDescription = "Open in Chrome/Browser",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Refresh
                        IconButton(
                            onClick = {
                                if (serverUrl != null) {
                                    webViewRef?.reload()
                                } else if (htmlContent != null) {
                                    webViewRef?.loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_refresh_preview")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = theme.onSurfaceColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Toggle Server Logs & Console
                        IconButton(
                            onClick = { showBottomDrawer = !showBottomDrawer },
                            modifier = Modifier.size(32.dp).testTag("btn_toggle_drawer")
                        ) {
                            Icon(
                                Icons.Default.Dns,
                                contentDescription = "Server & Console Logs",
                                tint = if (showBottomDrawer) theme.primaryColor else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp).testTag("btn_close_preview")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = theme.onSurfaceColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Interactive Address Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF070B14),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "URL:",
                                fontSize = 11.sp,
                                color = theme.lineNumberColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeUrl,
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("dev_server_url", activeUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied URL: $activeUrl", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp).testTag("btn_copy_url")
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy URL",
                                    tint = theme.lineNumberColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
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
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true
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

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                    }
                                }

                                if (serverUrl != null) {
                                    loadUrl(serverUrl)
                                } else if (htmlContent != null) {
                                    loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                                }
                                webViewRef = this
                            }
                        },
                        update = { webView ->
                            if (serverUrl != null && webView.url != serverUrl) {
                                webView.loadUrl(serverUrl)
                            } else if (serverUrl == null && htmlContent != null) {
                                webView.loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                            }
                        }
                    )
                }

                // DevTools & Server Logs Drawer
                AnimatedVisibility(visible = showBottomDrawer) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        color = Color(0xFF0B0F19),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Tabs
                            TabRow(
                                selectedTabIndex = activeDrawerTab,
                                containerColor = Color(0xFF070B14),
                                contentColor = theme.primaryColor,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[activeDrawerTab]),
                                        color = theme.primaryColor
                                    )
                                },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Tab(
                                    selected = activeDrawerTab == 0,
                                    onClick = { activeDrawerTab = 0 },
                                    text = {
                                        Text(
                                            "Server Requests (${serverLogs.size})",
                                            fontSize = 11.sp,
                                            fontWeight = if (activeDrawerTab == 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                                Tab(
                                    selected = activeDrawerTab == 1,
                                    onClick = { activeDrawerTab = 1 },
                                    text = {
                                        Text(
                                            "Browser Console (${consoleLogs.size})",
                                            fontSize = 11.sp,
                                            fontWeight = if (activeDrawerTab == 1) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }

                            // Drawer Content
                            if (activeDrawerTab == 0) {
                                // Server Request Logs
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .testTag("server_request_logs")
                                ) {
                                    if (serverLogs.isEmpty()) {
                                        item {
                                            Text(
                                                text = "No HTTP requests received yet. Navigate or reload preview to see live traffic.",
                                                color = theme.lineNumberColor,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    } else {
                                        items(serverLogs) { log ->
                                            ServerRequestRow(log = log)
                                        }
                                    }
                                }
                            } else {
                                // Browser Console Logs
                                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { consoleLogs.clear() }) {
                                            Text("Clear Console", fontSize = 11.sp, color = theme.lineNumberColor)
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
    }
}

@Composable
private fun ServerRequestRow(log: ServerRequestLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Method badge
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (log.method == "GET") Color(0xFF1E3A8A) else Color(0xFF065F46)
        ) {
            Text(
                text = log.method,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                fontFamily = FontFamily.Monospace
            )
        }

        // Status code
        val statusColor = when (log.statusCode) {
            in 200..299 -> Color(0xFF22C55E)
            in 300..399 -> Color(0xFF38BDF8)
            in 400..499 -> Color(0xFFFBBF24)
            else -> Color(0xFFEF4444)
        }
        Text(
            text = "${log.statusCode}",
            fontSize = 11.sp,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // URI
        Text(
            text = log.uri,
            fontSize = 11.sp,
            color = Color(0xFFE2E8F0),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Duration
        Text(
            text = "${log.durationMs}ms",
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            fontFamily = FontFamily.Monospace
        )
    }
}
