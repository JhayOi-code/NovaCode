package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.provider.DocumentsContract
import com.example.model.CodeFile
import com.example.model.EditorTheme
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

@Composable
fun FolderPickerBottomSheet(
    theme: EditorTheme,
    activeFolder: String,
    workspaceFiles: List<CodeFile>,
    availableFolders: List<String>,
    onSelectWorkspaceFolder: (String) -> Unit,
    onImportDeviceDirectory: (File, String) -> Unit,
    onImportZipArchive: (File, String) -> Unit,
    onImportMultipleFiles: (File, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customFolderName by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    // Launcher for Option 2: Folder from device (Storage Access Framework)
    val folderTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importStatusMessage = "Reading device folder..."
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                val docId = DocumentsContract.getTreeDocumentId(uri)
                val folderName = (docId.substringAfterLast(":").substringAfterLast("/"))
                    .replace(Regex("[/\\\\?%*:|\"<>]"), "_")
                    .ifBlank { "device_folder" }

                val targetDir = File(context.cacheDir, folderName).apply {
                    deleteRecursively()
                    mkdirs()
                }

                // Copy files recursively using DocumentsContract
                copyDocumentTree(context, uri, docId, targetDir)

                onImportDeviceDirectory(targetDir, folderName)
                Toast.makeText(context, "Serving device folder '$folderName' on :8080", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing folder: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isImporting = false
            }
        }
    }

    // Launcher for Option 3: ZIP file
    val zipFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importStatusMessage = "Extracting ZIP archive..."
            try {
                val displayName = getFileNameFromUri(context, uri) ?: "website_bundle.zip"
                val bundleName = displayName.substringBeforeLast(".").ifBlank { "zip_site" }
                val targetDir = File(context.cacheDir, bundleName).apply {
                    deleteRecursively()
                    mkdirs()
                }

                // Extract ZIP avoiding Zip Slip path traversal
                extractZipSafely(context, uri, targetDir)

                onImportZipArchive(targetDir, bundleName)
                Toast.makeText(context, "Extracted & serving '$bundleName' on :8080", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Error extracting ZIP: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isImporting = false
            }
        }
    }

    // Launcher for Option 4: Choose file(s)
    val multipleFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isImporting = true
            importStatusMessage = "Bundling ${uris.size} files..."
            try {
                val bundleFolder = "custom_files"
                val targetDir = File(context.cacheDir, bundleFolder).apply {
                    mkdirs()
                }

                uris.forEach { fileUri ->
                    val name = getFileNameFromUri(context, fileUri) ?: "file_${System.currentTimeMillis()}"
                    val destFile = File(targetDir, name)
                    context.contentResolver.openInputStream(fileUri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                onImportMultipleFiles(targetDir, bundleFolder)
                Toast.makeText(context, "Serving ${uris.size} file(s) on :8080", Toast.LENGTH_SHORT).show()
                onDismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Error copying files: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isImporting = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val isTablet = maxWidth >= 600.dp
            val windowModifier = if (isTablet) {
                Modifier
                    .widthIn(max = 560.dp)
                    .wrapContentHeight()
                    .padding(24.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * 0.92f)
                    .align(Alignment.BottomCenter)
            }

            val windowShape = if (isTablet) {
                RoundedCornerShape(24.dp)
            } else {
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            }

            Surface(
                shape = windowShape,
                color = theme.surfaceColor,
                border = BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.5f)),
                tonalElevation = 10.dp,
                modifier = windowModifier.testTag("folder_picker_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .padding(bottom = 12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = theme.primaryColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Choose Dev Server Folder",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = theme.onSurfaceColor
                                )
                                Text(
                                    text = "Select a source to serve on localhost:8080",
                                    fontSize = 12.sp,
                                    color = theme.lineNumberColor
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_folder_picker")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isImporting) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = theme.primaryColor, strokeWidth = 2.dp)
                                Text(
                                    text = importStatusMessage ?: "Importing...",
                                    color = theme.onSurfaceColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // --- OPTION 1: LINQ WORKSPACE FOLDERS ---
                    Text(
                        text = "1. Linq Workspace Folders",
                        color = theme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Compute counts per folder
                    val standardFolders = (availableFolders + listOf("src", "scripts", "styles", "docs", "data", "www", "site", "root")).distinct()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        standardFolders.forEach { folderName ->
                            val isCurrent = folderName == activeFolder
                            val count = workspaceFiles.count {
                                val f = it.path.substringBefore("/", "")
                                if (f.isEmpty()) folderName == "root" || folderName == "src" else f == folderName
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) theme.primaryColor.copy(alpha = 0.15f) else theme.surfaceColor,
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) theme.primaryColor else theme.borderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectWorkspaceFolder(folderName)
                                        onDismiss()
                                    }
                                    .testTag("host_folder_option_$folderName")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrent) Icons.Default.FolderSpecial else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isCurrent) theme.primaryColor else theme.lineNumberColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "/$folderName",
                                            color = theme.onSurfaceColor,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = "($count files)",
                                            color = theme.lineNumberColor,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isCurrent) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = theme.primaryColor,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE HOST",
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Select",
                                            color = theme.primaryColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // Inline quick create folder
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customFolderName,
                                onValueChange = { customFolderName = it },
                                placeholder = { Text("New folder name...", fontSize = 12.sp, color = theme.lineNumberColor) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("input_new_host_folder"),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = theme.onSurfaceColor)
                            )

                            Button(
                                onClick = {
                                    val clean = customFolderName.trim().replace(Regex("[/\\\\?%*:|\"<>]"), "").ifBlank { "new_site" }
                                    onSelectWorkspaceFolder(clean)
                                    customFolderName = ""
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("btn_create_and_host_folder")
                            ) {
                                Text("Host", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // --- OPTION 2: FOLDER FROM DEVICE ---
                    Text(
                        text = "2. Folder from my device",
                        color = theme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { folderTreeLauncher.launch(null) }
                            .testTag("option_device_folder")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DriveFolderUpload, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pick Directory from Device", color = theme.onSurfaceColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Uses Android Document Tree / Storage Access Framework", color = theme.lineNumberColor, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- OPTION 3: ZIP FILE ---
                    Text(
                        text = "3. ZIP file",
                        color = theme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { zipFileLauncher.launch("*/*") }
                            .testTag("option_zip_file")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Upload ZIP Website Bundle", color = theme.onSurfaceColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Auto-extracts keeping subfolder hierarchy in virtual folder", color = theme.lineNumberColor, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- OPTION 4: CHOOSE FILE(S) ---
                    Text(
                        text = "4. Choose file(s)",
                        color = theme.primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceVariant.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { multipleFilesLauncher.launch("*/*") }
                            .testTag("option_choose_files")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Select Multiple Files", color = theme.onSurfaceColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Serve together in virtual folder 'custom_files'", color = theme.lineNumberColor, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer with Server URL
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981))
                                )
                                Text(
                                    text = "http://localhost:8080 (/$activeFolder)",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "Auto-Restarting",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helpers for device folder copying and ZIP extraction with path traversal safety
private fun copyDocumentTree(context: Context, treeUri: Uri, parentDocId: String, destDir: File) {
    try {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val childDocId = cursor.getString(idCol) ?: continue
                val childName = cursor.getString(nameCol) ?: "unnamed"
                val mimeType = cursor.getString(mimeCol)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val subDir = File(destDir, childName).apply { mkdirs() }
                    copyDocumentTree(context, treeUri, childDocId, subDir)
                } else {
                    val destFile = File(destDir, childName)
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)
                    context.contentResolver.openInputStream(docUri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    } catch (_: Exception) {}
}

private fun extractZipSafely(context: Context, zipUri: Uri, destDir: File) {
    val canonicalDest = destDir.canonicalFile
    context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(destDir, entry.name)
                // Zip Slip check
                if (!newFile.canonicalFile.path.startsWith(canonicalDest.path)) {
                    throw SecurityException("ZIP entry outside destination: ${entry.name}")
                }
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return cursor.getString(idx)
            }
        }
    }
    return uri.lastPathSegment
}
