package com.example.storage

import android.content.Context
import android.net.Uri
import com.example.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManager(private val context: Context) {

    val internalWorkspaceDir: File by lazy {
        File(context.filesDir, "workspace").apply {
            if (!exists()) {
                mkdirs()
                createStarterProjects(this)
            }
        }
    }

    private fun createStarterProjects(workspace: File) {
        // 1. Web demo
        val webDir = File(workspace, "WebStarter").apply { mkdirs() }
        File(webDir, "index.html").writeText(
            """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NovaCode Web Preview</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="card">
        <h1>⚡ NovaCode Studio</h1>
        <p>Live dev server and in-app responsive preview are ready.</p>
        <button id="counterBtn">Clicks: <span id="count">0</span></button>
    </div>
    <script src="app.js"></script>
</body>
</html>"""
        )

        File(webDir, "style.css").writeText(
            """body {
    margin: 0;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #0f172a;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    color: #f8fafc;
}
.card {
    background: #1e293b;
    padding: 2.5rem;
    border-radius: 1rem;
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
    text-align: center;
    border: 1px solid #334155;
    max-width: 400px;
}
button {
    background: #00e5ff;
    color: #0b1120;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 9999px;
    font-weight: 700;
    cursor: pointer;
    font-size: 1rem;
    transition: transform 0.1s ease;
}
button:active {
    transform: scale(0.96);
}"""
        )

        File(webDir, "app.js").writeText(
            """let count = 0;
const btn = document.getElementById('counterBtn');
const label = document.getElementById('count');

btn.addEventListener('click', () => {
    count++;
    label.textContent = count;
    console.log("Button clicked, count is now:", count);
});"""
        )

        // 2. Python Starter
        val pyDir = File(workspace, "PythonStarter").apply { mkdirs() }
        File(pyDir, "main.py").writeText(
            """# NovaCode Python Starter
import sys

def main():
    print("Welcome to NovaCode IDE on Android!")
    print(f"Python Version: {sys.version}")
    
    # Calculate Fibonacci numbers
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

        // 3. Kotlin Starter
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

        // 4. README
        File(workspace, "README.md").writeText(
            """# 🚀 Welcome to NovaCode IDE

NovaCode is an ultra-fast, professional mobile development environment built for Android.

### Features
- **Touch-First Syntax Editor**: Ultra-smooth syntax highlighting with extra-keys row.
- **Full Device File Access**: Tier 1 App Sandbox, Tier 2 SAF, Tier 3 All Files Access.
- **Theme Engine**: 10 distinct themes including Dracula, Liquid Glass, Monokai, and AMOLED.
- **Live Preview & Dev Server**: Test HTML/CSS/JS with live DOM rendering and split-screen.
- **Multi-File Search & Git Integration Ready**.

*Developed by Ljhay • Ljhay Softworks LLC*
"""
        )
    }

    suspend fun listFiles(directory: File): List<FileItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) return@withContext emptyList()

        val files = directory.listFiles() ?: return@withContext emptyList()
        files.map { file ->
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
    }

    suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use { it.readText() }
            } ?: throw IllegalStateException("Could not open URI stream: $path")
        } else {
            val file = File(path)
            if (!file.exists()) throw IllegalArgumentException("File does not exist: $path")
            file.readText()
        }
    }

    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (path.startsWith("content://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                    stream.bufferedWriter().use { it.write(content) }
                } ?: return@withContext false
                true
            } else {
                val file = File(path)
                file.parentFile?.let { if (!it.exists()) it.mkdirs() }
                file.writeText(content)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createFile(parentDir: File, name: String, isFolder: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val target = File(parentDir, name)
            if (target.exists()) return@withContext false
            if (isFolder) {
                target.mkdirs()
            } else {
                target.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun renameFile(oldPath: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val oldFile = File(oldPath)
            if (!oldFile.exists()) return@withContext false
            val newFile = File(oldFile.parentFile, newName)
            oldFile.renameTo(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun searchInFiles(rootDir: File, query: String, searchContent: Boolean): List<FileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<FileItem>()
        if (query.isBlank() || !rootDir.exists()) return@withContext results

        fun walk(dir: File) {
            val entries = dir.listFiles() ?: return
            for (file in entries) {
                if (file.name.startsWith(".")) continue // Skip hidden files/git
                if (file.isDirectory) {
                    if (file.name.contains(query, ignoreCase = true)) {
                        results.add(
                            FileItem(
                                name = file.name,
                                path = file.absolutePath,
                                isDirectory = true,
                                size = 0L,
                                lastModified = file.lastModified()
                            )
                        )
                    }
                    walk(file)
                } else {
                    var match = file.name.contains(query, ignoreCase = true)
                    if (!match && searchContent && file.length() < 2 * 1024 * 1024) { // Limit search to 2MB files for instant response
                        try {
                            val text = file.readText()
                            if (text.contains(query, ignoreCase = true)) {
                                match = true
                            }
                        } catch (_: Exception) {}
                    }
                    if (match) {
                        results.add(
                            FileItem(
                                name = file.name,
                                path = file.absolutePath,
                                isDirectory = false,
                                size = file.length(),
                                lastModified = file.lastModified()
                            )
                        )
                    }
                }
            }
        }

        walk(rootDir)
        results
    }

    suspend fun zipDirectory(sourceDir: File, outputZip: File): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZip))).use { out ->
                val base = sourceDir.absolutePath
                fun compress(file: File) {
                    val pathInZip = file.absolutePath.substring(base.length).trimStart(File.separatorChar)
                    if (file.isDirectory) {
                        val children = file.listFiles() ?: return
                        for (child in children) {
                            compress(child)
                        }
                    } else {
                        val entry = ZipEntry(pathInZip)
                        out.putNextEntry(entry)
                        FileInputStream(file).use { input ->
                            input.copyTo(out)
                        }
                        out.closeEntry()
                    }
                }
                compress(sourceDir)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unzip(zipFileUri: Uri, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(zipFileUri)?.use { rawStream ->
                ZipInputStream(BufferedInputStream(rawStream)).use { zipIn ->
                    var entry: ZipEntry? = zipIn.nextEntry
                    while (entry != null) {
                        val newFile = File(destinationDir, entry.name)
                        // Security check: Zip Slip vulnerability prevention
                        if (!newFile.canonicalPath.startsWith(destinationDir.canonicalPath)) {
                            throw SecurityException("Zip Slip detected: ${entry.name}")
                        }
                        if (entry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            FileOutputStream(newFile).use { out ->
                                zipIn.copyTo(out)
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
