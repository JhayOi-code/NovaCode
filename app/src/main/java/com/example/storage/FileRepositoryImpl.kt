package com.example.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import com.example.model.FileItem
import com.example.model.SupportedLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileRepositoryImpl(
    private val context: Context
) : FileRepository {

    override val internalWorkspaceDir: File by lazy {
        File(context.filesDir, "workspace").apply {
            if (!exists()) {
                mkdirs()
                createStarterProjects(this)
            }
        }
    }

    private fun createStarterProjects(workspace: File) {
        val webDir = File(workspace, "WebStarter").apply { mkdirs() }

        fun copyAssetOrDefault(assetName: String, fallback: String) {
            val destFile = File(webDir, assetName)
            try {
                context.assets.open("web_explorer/$assetName").use { input ->
                    destFile.outputStream().use { out ->
                        input.copyTo(out)
                    }
                }
            } catch (_: Exception) {
                destFile.writeText(fallback)
            }
        }

        copyAssetOrDefault(
            "index.html",
            """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>NovaCode - Mobile Code Editor & Explorer</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div id="app" class="app-container">
    <div style="padding: 20px; color: #fff;">NovaCode Mobile Explorer</div>
  </div>
  <script src="app.js"></script>
</body>
</html>"""
        )

        copyAssetOrDefault(
            "style.css",
            """body { margin: 0; background: #050810; color: #f3f4f6; font-family: sans-serif; }"""
        )

        copyAssetOrDefault(
            "app.js",
            """console.log("NovaCode Mobile Explorer loaded");"""
        )

        val pyDir = File(workspace, "PythonStarter").apply { mkdirs() }
        File(pyDir, "main.py").writeText(
            """# NovaCode Python Starter
import sys

def main():
    print("Welcome to NovaCode IDE on Android!")
    print(f"Python Version: {sys.version}")
    
    def fib(n):
        a, b = 0, 1
        for _ in range(n):
            yield a
            a, b = b, a + b
            
    print("First 10 Fibonacci numbers:", list(fib(10)))

if __name__ == "__main__":
    main()
"""
        )

        val ktDir = File(workspace, "KotlinStarter").apply { mkdirs() }
        File(ktDir, "Main.kt").writeText(
            """package com.novacode.demo

data class Developer(val name: String, val role: String)

fun main() {
    val dev = Developer("Ljhay", "Android Architect")
    println("Hello from NovaCode Kotlin Engine!")
    println("Active Lead: " + dev.name + ", Role: " + dev.role)
}
"""
        )

        File(workspace, "README.md").writeText(
            """# 🚀 Welcome to NovaCode IDE

NovaCode is an ultra-fast, professional mobile development environment built for Android.

### Features
- **Touch-First Syntax Editor**: Ultra-smooth syntax highlighting with extra-keys row.
- **Full Device File Access**: Tier 1 App Sandbox, Tier 2 SAF, Tier 3 All Files Access.
- **Theme Engine**: 10 distinct themes including Dracula, Liquid Glass, Monokai, and AMOLED.
- **Live Preview & Dev Server**: Test HTML/CSS/JS with live DOM rendering and split-screen.
- **Kotlin Flows Repository**: Unified non-blocking IO abstraction for POSIX & SAF.

*Developed by Ljhay • Ljhay Softworks LLC*
"""
        )
    }

    override fun getStorageRoots(): Flow<List<FileItem>> = flow {
        val roots = mutableListOf<FileItem>()

        // 1. Internal App Workspace Root
        roots.add(
            FileItem(
                name = "App Workspace (Internal)",
                path = internalWorkspaceDir.absolutePath,
                isDirectory = true,
                isWritable = true
            )
        )

        // 2. Device Shared Storage (/sdcard)
        val externalStorage = Environment.getExternalStorageDirectory()
        if (externalStorage.canRead()) {
            roots.add(
                FileItem(
                    name = "Internal Storage (/sdcard)",
                    path = externalStorage.absolutePath,
                    isDirectory = true,
                    isWritable = externalStorage.canWrite()
                )
            )
        }

        // 3. User-granted SAF Directories (Persisted URIs)
        val persisted = context.contentResolver.persistedUriPermissions
        for (perm in persisted) {
            if (perm.isReadPermission) {
                val uriString = perm.uri.toString()
                val displayName = perm.uri.lastPathSegment?.substringAfterLast(':') ?: "Granted SAF Folder"
                roots.add(
                    FileItem(
                        name = "📁 $displayName (SAF)",
                        path = uriString,
                        isDirectory = true,
                        isWritable = perm.isWritePermission,
                        uriString = uriString
                    )
                )
            }
        }

        emit(roots)
    }.flowOn(Dispatchers.IO)

    override fun listFiles(targetPathOrUri: String): Flow<List<FileItem>> = flow {
        if (isSafUri(targetPathOrUri)) {
            emit(listSafChildren(targetPathOrUri))
        } else {
            val dir = File(targetPathOrUri)
            if (!dir.exists() || !dir.isDirectory) {
                emit(emptyList())
            } else {
                val files = dir.listFiles() ?: emptyArray()
                val items = files.map { file ->
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0L else file.length(),
                        lastModified = file.lastModified(),
                        isWritable = file.canWrite()
                    )
                }.sortedWith(
                    compareBy<FileItem> { !it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )
                emit(items)
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun readFile(targetPathOrUri: String): Flow<String> = flow {
        if (isSafUri(targetPathOrUri)) {
            val uri = Uri.parse(targetPathOrUri)
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: throw IllegalStateException("Unable to open SAF stream for reading: $targetPathOrUri")
            emit(content)
        } else {
            val file = File(targetPathOrUri)
            if (!file.exists()) {
                throw IllegalArgumentException("File does not exist: $targetPathOrUri")
            }
            emit(file.readText(Charsets.UTF_8))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun writeFile(targetPathOrUri: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isSafUri(targetPathOrUri)) {
                val uri = Uri.parse(targetPathOrUri)
                context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                    stream.bufferedWriter(Charsets.UTF_8).use { it.write(content) }
                } ?: return@withContext Result.failure(IllegalStateException("Could not open SAF output stream: $targetPathOrUri"))
                Result.success(Unit)
            } else {
                val file = File(targetPathOrUri)
                file.parentFile?.let { if (!it.exists()) it.mkdirs() }
                file.writeText(content, Charsets.UTF_8)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createFile(parentPathOrUri: String, name: String, isFolder: Boolean): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            if (isSafUri(parentPathOrUri)) {
                val parentUri = Uri.parse(parentPathOrUri)
                val mimeType = if (isFolder) {
                    DocumentsContract.Document.MIME_TYPE_DIR
                } else {
                    getMimeTypeForFilename(name)
                }

                val docId = if (DocumentsContract.isDocumentUri(context, parentUri)) {
                    DocumentsContract.getDocumentId(parentUri)
                } else {
                    DocumentsContract.getTreeDocumentId(parentUri)
                }
                val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)

                val newDocUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    parentDocUri,
                    mimeType,
                    name
                ) ?: return@withContext Result.failure(IllegalStateException("Failed to create SAF document: $name"))

                Result.success(
                    FileItem(
                        name = name,
                        path = newDocUri.toString(),
                        isDirectory = isFolder,
                        uriString = newDocUri.toString()
                    )
                )
            } else {
                val parent = File(parentPathOrUri)
                val target = File(parent, name)
                if (target.exists()) {
                    return@withContext Result.failure(IllegalArgumentException("File already exists: ${target.name}"))
                }
                val created = if (isFolder) target.mkdirs() else target.createNewFile()
                if (created) {
                    Result.success(
                        FileItem(
                            name = target.name,
                            path = target.absolutePath,
                            isDirectory = isFolder,
                            size = if (isFolder) 0L else target.length(),
                            lastModified = target.lastModified()
                        )
                    )
                } else {
                    Result.failure(IllegalStateException("Could not create target: ${target.name}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(targetPathOrUri: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isSafUri(targetPathOrUri)) {
                val uri = Uri.parse(targetPathOrUri)
                val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
                Result.success(deleted)
            } else {
                val file = File(targetPathOrUri)
                val deleted = if (file.isDirectory) file.deleteRecursively() else file.delete()
                Result.success(deleted)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameFile(targetPathOrUri: String, newName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            if (isSafUri(targetPathOrUri)) {
                val uri = Uri.parse(targetPathOrUri)
                val newUri = DocumentsContract.renameDocument(context.contentResolver, uri, newName)
                if (newUri != null) {
                    Result.success(
                        FileItem(
                            name = newName,
                            path = newUri.toString(),
                            isDirectory = false,
                            uriString = newUri.toString()
                        )
                    )
                } else {
                    Result.failure(IllegalStateException("SAF rename failed"))
                }
            } else {
                val file = File(targetPathOrUri)
                val newFile = File(file.parentFile, newName)
                val renamed = file.renameTo(newFile)
                if (renamed) {
                    Result.success(
                        FileItem(
                            name = newFile.name,
                            path = newFile.absolutePath,
                            isDirectory = newFile.isDirectory,
                            size = newFile.length(),
                            lastModified = newFile.lastModified()
                        )
                    )
                } else {
                    Result.failure(IllegalStateException("Rename failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchFiles(rootPathOrUri: String, query: String, searchContent: Boolean): Flow<List<FileItem>> = flow {
        val matches = mutableListOf<FileItem>()
        if (query.isBlank()) {
            emit(matches)
            return@flow
        }

        if (isSafUri(rootPathOrUri)) {
            // Traverse SAF children recursively
            fun searchSafTree(folderUriString: String) {
                val children = listSafChildren(folderUriString)
                for (child in children) {
                    var isMatch = child.name.contains(query, ignoreCase = true)
                    if (!isMatch && searchContent && !child.isDirectory && child.size < 2 * 1024 * 1024) {
                        try {
                            val uri = Uri.parse(child.path)
                            val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                            } ?: ""
                            if (text.contains(query, ignoreCase = true)) {
                                isMatch = true
                            }
                        } catch (_: Exception) {}
                    }
                    if (isMatch) matches.add(child)
                    if (child.isDirectory) {
                        searchSafTree(child.path)
                    }
                }
            }
            searchSafTree(rootPathOrUri)
            emit(matches)
        } else {
            val root = File(rootPathOrUri)
            if (!root.exists()) {
                emit(matches)
                return@flow
            }

            fun searchPosix(dir: File) {
                val files = dir.listFiles() ?: return
                for (file in files) {
                    if (file.name.startsWith(".")) continue
                    var isMatch = file.name.contains(query, ignoreCase = true)
                    if (!isMatch && searchContent && !file.isDirectory && file.length() < 2 * 1024 * 1024) {
                        try {
                            val text = file.readText(Charsets.UTF_8)
                            if (text.contains(query, ignoreCase = true)) {
                                isMatch = true
                            }
                        } catch (_: Exception) {}
                    }
                    if (isMatch) {
                        matches.add(
                            FileItem(
                                name = file.name,
                                path = file.absolutePath,
                                isDirectory = file.isDirectory,
                                size = file.length(),
                                lastModified = file.lastModified()
                            )
                        )
                    }
                    if (file.isDirectory) {
                        searchPosix(file)
                    }
                }
            }
            searchPosix(root)
            emit(matches)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun zipDirectory(sourcePathOrUri: String, targetZipFile: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isSafUri(sourcePathOrUri)) {
                // ZIP for SAF tree
                ZipOutputStream(BufferedOutputStream(FileOutputStream(targetZipFile))).use { out ->
                    fun compressSaf(folderUriString: String, baseSubPath: String) {
                        val children = listSafChildren(folderUriString)
                        for (child in children) {
                            val entryPath = if (baseSubPath.isEmpty()) child.name else "$baseSubPath/${child.name}"
                            if (child.isDirectory) {
                                compressSaf(child.path, entryPath)
                            } else {
                                val uri = Uri.parse(child.path)
                                val entry = ZipEntry(entryPath)
                                out.putNextEntry(entry)
                                context.contentResolver.openInputStream(uri)?.use { input ->
                                    input.copyTo(out)
                                }
                                out.closeEntry()
                            }
                        }
                    }
                    compressSaf(sourcePathOrUri, "")
                }
                Result.success(true)
            } else {
                val sourceDir = File(sourcePathOrUri)
                if (!sourceDir.exists()) return@withContext Result.failure(IllegalArgumentException("Source dir does not exist"))

                ZipOutputStream(BufferedOutputStream(FileOutputStream(targetZipFile))).use { out ->
                    val base = sourceDir.absolutePath
                    fun compress(file: File) {
                        val relative = file.absolutePath.substring(base.length).trimStart(File.separatorChar)
                        if (file.isDirectory) {
                            val children = file.listFiles() ?: return
                            for (child in children) compress(child)
                        } else {
                            val entry = ZipEntry(relative)
                            out.putNextEntry(entry)
                            FileInputStream(file).use { input -> input.copyTo(out) }
                            out.closeEntry()
                        }
                    }
                    compress(sourceDir)
                }
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isSafUri(pathOrUri: String): Boolean = pathOrUri.startsWith("content://")

    private fun listSafChildren(treeOrDocUriString: String): List<FileItem> {
        val results = mutableListOf<FileItem>()
        val uri = Uri.parse(treeOrDocUriString)

        val docId = if (DocumentsContract.isDocumentUri(context, uri)) {
            DocumentsContract.getDocumentId(uri)
        } else {
            DocumentsContract.getTreeDocumentId(uri)
        }

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, docId)

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(childrenUri, projection, null, null, null)
            if (cursor != null) {
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val modifiedIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childDocId = cursor.getString(idIdx)
                    val name = cursor.getString(nameIdx) ?: "Unknown"
                    val mime = cursor.getString(mimeIdx) ?: ""
                    val size = if (sizeIdx != -1) cursor.getLong(sizeIdx) else 0L
                    val modified = if (modifiedIdx != -1) cursor.getLong(modifiedIdx) else 0L

                    val isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR
                    val childUri = DocumentsContract.buildDocumentUriUsingTree(uri, childDocId)

                    results.add(
                        FileItem(
                            name = name,
                            path = childUri.toString(),
                            isDirectory = isDir,
                            size = size,
                            lastModified = modified,
                            isWritable = true,
                            uriString = childUri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return results.sortedWith(
            compareBy<FileItem> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
    }

    private fun getMimeTypeForFilename(filename: String): String {
        val ext = filename.substringAfterLast('.', "")
        return when (ext.lowercase()) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js", "mjs" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "text/xml"
            "md" -> "text/markdown"
            "py" -> "text/x-python"
            "kt", "kts" -> "text/x-kotlin"
            "java" -> "text/x-java"
            "c", "h" -> "text/x-c"
            "cpp", "hpp" -> "text/x-c++"
            "rs" -> "text/x-rust"
            "go" -> "text/x-go"
            "sh", "bash" -> "application/x-sh"
            "sql" -> "application/sql"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/plain"
        }
    }
}
