package com.example.storage

import android.content.Context
import com.example.model.FileItem
import java.io.File

class FileRepositoryImpl(private val context: Context) : FileRepository {

    override val internalWorkspaceDir: File = File(context.filesDir, "workspace").apply {
        if (!exists()) {
            mkdirs()
        }
        createStarterProjects(this)
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
                if (!destFile.exists() || destFile.length() == 0L) {
                    destFile.writeText(fallback)
                }
            }
        }

        copyAssetOrDefault(
            "index.html",
            """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Linq Studio</title>
</head>
<body>
  <h1>Hello from Linq Studio!</h1>
</body>
</html>"""
        )

        copyAssetOrDefault(
            "style.css",
            "body { background: #0B0F17; color: #FFF; font-family: sans-serif; }"
        )

        copyAssetOrDefault(
            "app.js",
            "console.log('Linq Studio initialized');"
        )

        copyAssetOrDefault(
            "logo.svg",
            ""
        )
    }

    override fun listFiles(dir: File): List<FileItem> {
        val actualDir = if (dir.exists()) dir else internalWorkspaceDir
        val list = actualDir.listFiles() ?: return emptyList()
        return list.map { FileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    override fun readFile(file: File): String {
        return try {
            if (file.exists()) file.readText() else ""
        } catch (_: Exception) {
            ""
        }
    }

    override fun writeFile(file: File, content: String): Boolean {
        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun createFile(parent: File, name: String): File? {
        val newFile = File(parent, name)
        return try {
            if (newFile.createNewFile()) newFile else null
        } catch (_: Exception) {
            null
        }
    }

    override fun createFolder(parent: File, name: String): File? {
        val newFolder = File(parent, name)
        return try {
            if (newFolder.mkdirs() || newFolder.exists()) newFolder else null
        } catch (_: Exception) {
            null
        }
    }

    override fun delete(file: File): Boolean {
        return try {
            if (file.isDirectory) file.deleteRecursively() else file.delete()
        } catch (_: Exception) {
            false
        }
    }

    override fun rename(file: File, newName: String): File? {
        val dest = File(file.parentFile, newName)
        return if (file.renameTo(dest)) dest else null
    }

    override fun duplicate(file: File): File? {
        val baseName = file.nameWithoutExtension
        val ext = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
        val newFile = File(file.parentFile, "${baseName}_copy$ext")
        return try {
            file.copyTo(newFile, overwrite = true)
            newFile
        } catch (_: Exception) {
            null
        }
    }
}
