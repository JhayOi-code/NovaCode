package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.server.ServerStatus
import java.io.File

enum class DeviceFrame(val label: String, val maxWidthDp: Int) {
    MOBILE("Mobile", 380),
    TABLET("Tablet", 620),
    FIT_DEVICE("Fit Screen", Int.MAX_VALUE)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPreviewDialog(
    theme: EditorTheme,
    files: List<CodeFile>,
    initialFileName: String? = null,
    serverStatus: ServerStatus,
    currentHostFolder: String = "www",
    availableFolders: List<String> = listOf("www", "src", "public", "docs", "dist", "site"),
    onSelectHostFolder: (String) -> Unit = {},
    onCreateHostFolder: (String) -> Unit = {},
    onRestartServer: () -> Unit,
    onClose: () -> Unit
) {
    val webFiles = remember(files) {
        val found = files.filter {
            it.name.endsWith(".html", ignoreCase = true) ||
            it.name.endsWith(".htm", ignoreCase = true) ||
            it.name.endsWith(".svg", ignoreCase = true)
        }
        if (found.isNotEmpty()) found else listOf(
            CodeFile(name = "index.html", path = "/workspace/index.html", content = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Linq Studio Web Preview</title>
                  <style>
                    body { margin: 0; padding: 24px; font-family: -apple-system, BlinkMacSystemFont, sans-serif; background: #0F172A; color: #F8FAFC; text-align: center; }
                    .card { background: #1E293B; border-radius: 16px; padding: 32px 20px; max-width: 480px; margin: 40px auto; border: 1px solid #334155; }
                    h1 { color: #38BDF8; font-size: 26px; margin-bottom: 8px; }
                    p { color: #94A3B8; font-size: 15px; line-height: 1.6; }
                    .badge { display: inline-block; background: #064E3B; color: #6EE7B7; padding: 6px 14px; border-radius: 999px; font-weight: bold; font-size: 12px; margin-top: 12px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>Linq Dev Server</h1>
                    <p>Live local web environment running on port 8080. Serving folder: <code>/$currentHostFolder</code></p>
                    <div class="badge">HTTP SERVER ACTIVE</div>
                  </div>
                </body>
                </html>
            """.trimIndent())
        )
    }

    var selectedFile by remember(initialFileName, webFiles) {
        val target = webFiles.find { it.name.equals(initialFileName, ignoreCase = true) } ?: webFiles.first()
        mutableStateOf(target)
    }

    var activeFrame by remember { mutableStateOf(DeviceFrame.FIT_DEVICE) }
    var isSourceViewMode by remember { mutableStateOf(false) }
    var isFullScreenPreview by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreenPreview) 0.dp else 8.dp)
                .testTag("web_preview_dialog"),
            shape = RoundedCornerShape(if (isFullScreenPreview) 0.dp else 16.dp),
            color = theme.surfaceColor,
            border = if (isFullScreenPreview) null else androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR ROW 1: Header, Host Folder Switcher, and Main Action Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Brand & Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Localhost Dev Server",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            maxLines = 1
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
                                    text = if (isRunning) ":${serverStatus.portOrNull ?: 8080} LIVE" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRunning) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Right: Folder Selector Button & Action Icons (Organized with zero overlap)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // CHOOSE HOST FOLDER BUTTON
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.primaryColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor),
                            modifier = Modifier
                                .clickable { showFolderDialog = true }
                                .testTag("btn_choose_host_folder")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = "Choose Host Folder",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Host: $currentHostFolder",
                                    color = theme.primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                refreshKey++
                                onRestartServer()
                            },
                            modifier = Modifier.size(34.dp).testTag("btn_preview_refresh")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Server", tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { isFullScreenPreview = !isFullScreenPreview },
                            modifier = Modifier.size(34.dp).testTag("btn_preview_fullscreen")
                        ) {
                            Icon(
                                imageVector = if (isFullScreenPreview) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullScreenPreview) "Exit Full Screen" else "Full Screen",
                                tint = if (isFullScreenPreview) Color(0xFF10B981) else theme.lineNumberColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(34.dp).testTag("btn_preview_close")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // TOP BAR ROW 2: File Switcher Tabs, Result/Source Switcher, and Device Frame toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.editorBackground)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Files:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.lineNumberColor
                    )

                    webFiles.forEach { file ->
                        val isSelected = file.name == selectedFile.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceColor,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) theme.primaryColor else theme.borderColor
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedFile = file
                                    refreshKey++
                                }
                                .testTag("select_web_file_${file.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = if (isSelected) theme.primaryColor else theme.lineNumberColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = file.name,
                                    color = if (isSelected) theme.primaryColor else theme.onSurfaceColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // View Mode Switcher: Result vs Source
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (!isSourceViewMode) theme.primaryColor else Color.Transparent,
                                modifier = Modifier.clickable { isSourceViewMode = false }.testTag("tab_preview_result")
                            ) {
                                Text(
                                    text = "Result",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSourceViewMode) Color.Black else theme.lineNumberColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSourceViewMode) theme.primaryColor else Color.Transparent,
                                modifier = Modifier.clickable { isSourceViewMode = true }.testTag("tab_preview_source")
                            ) {
                                Text(
                                    text = "Source",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSourceViewMode) Color.Black else theme.lineNumberColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Device frame toggles
                    IconButton(
                        onClick = { activeFrame = DeviceFrame.MOBILE },
                        modifier = Modifier.size(30.dp).testTag("btn_frame_mobile")
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
                        modifier = Modifier.size(30.dp).testTag("btn_frame_tablet")
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
                        modifier = Modifier.size(30.dp).testTag("btn_frame_fit")
                    ) {
                        Icon(
                            Icons.Default.FitScreen,
                            contentDescription = "Fit Screen",
                            tint = if (activeFrame == DeviceFrame.FIT_DEVICE) Color(0xFF10B981) else theme.lineNumberColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = theme.borderColor)

                // VIEWPORT CANVAS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF070B14))
                        .padding(if (isFullScreenPreview) 0.dp else 8.dp),
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
                            key(selectedFile.name, currentHostFolder, refreshKey) {
                                AndroidView(
                                    factory = { ctx ->
                                        try {
                                            File(ctx.cacheDir, "WebView/Default/HTTP Cache").mkdirs()
                                        } catch (_: Exception) {}

                                        WebView(ctx).apply {
                                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.allowFileAccess = true
                                            settings.databaseEnabled = true
                                            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

                                            webViewClient = object : WebViewClient() {
                                                override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean = true
                                            }

                                            webChromeClient = object : WebChromeClient() {
                                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true
                                            }

                                            val url = "http://localhost:${serverStatus.portOrNull ?: 8080}/${selectedFile.name}"
                                            if (serverStatus.isRunning) {
                                                loadUrl(url)
                                            } else {
                                                loadDataWithBaseURL("http://localhost:8080/", selectedFile.content, "text/html", "UTF-8", null)
                                            }
                                        }
                                    },
                                    update = { webView ->
                                        val url = "http://localhost:${serverStatus.portOrNull ?: 8080}/${selectedFile.name}"
                                        if (serverStatus.isRunning) {
                                            webView.loadUrl(url)
                                        } else {
                                            webView.loadDataWithBaseURL("http://localhost:8080/", selectedFile.content, "text/html", "UTF-8", null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize().testTag("webview_rendered_result")
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF0B0F17)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Text(
                                        text = "// Source of ${selectedFile.name} (Dev Server on http://localhost:8080/${selectedFile.name})",
                                        color = Color(0xFF64748B),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = selectedFile.content,
                                        color = Color(0xFF86EFAC),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CHOOSE HOST FOLDER DIALOG
    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = theme.primaryColor)
                    Text("Select Website Folder", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose which workspace folder the local development server hosts as http://localhost:8080/:",
                        fontSize = 13.sp,
                        color = theme.lineNumberColor
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(availableFolders) { folder ->
                            val isCurrent = folder.equals(currentHostFolder, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) theme.primaryColor.copy(alpha = 0.2f) else theme.surfaceColor,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) theme.primaryColor else theme.borderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        onSelectHostFolder(folder)
                                        showFolderDialog = false
                                        refreshKey++
                                    }
                                    .testTag("folder_option_$folder")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isCurrent) theme.primaryColor else theme.lineNumberColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "/$folder",
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) theme.primaryColor else theme.onSurfaceColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(Icons.Default.Check, contentDescription = "Active Host", tint = theme.primaryColor, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = theme.borderColor)

                    // Create & Host New Folder input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newFolderNameInput,
                            onValueChange = { newFolderNameInput = it },
                            placeholder = { Text("new-folder-name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(50.dp).testTag("input_new_host_folder"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primaryColor,
                                unfocusedBorderColor = theme.borderColor
                            )
                        )
                        Button(
                            onClick = {
                                val trimmed = newFolderNameInput.trim().replace(Regex("[/\\\\]"), "")
                                if (trimmed.isNotBlank()) {
                                    onCreateHostFolder(trimmed)
                                    onSelectHostFolder(trimmed)
                                    showFolderDialog = false
                                    refreshKey++
                                    newFolderNameInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(50.dp).testTag("btn_create_host_folder")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Host", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("Close", color = theme.primaryColor)
                }
            }
        )
    }
}
