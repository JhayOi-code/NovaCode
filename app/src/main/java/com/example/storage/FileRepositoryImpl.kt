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

        // Python Starter
        val pyFile = File(workspace, "main.py")
        if (!pyFile.exists() || pyFile.length() == 0L) {
            pyFile.writeText(
                """# Linq Studio - Python Multi-Language Runner
def calculate_fibonacci(n):
    fib = [0, 1]
    for i in range(2, n):
        fib.append(fib[i - 1] + fib[i - 2])
    return fib

print("=== Linq Studio Python 3.11 Runtime ===")
print("System initialized successfully.")

# Math and variables
name = "Developer"
score = 42 * 2 + 10
print(f"Hello, {name}! Your computed score is {score}")

# Loops and lists
numbers = [1, 2, 3, 4, 5]
total = sum(numbers)
print("List sum:", total)

for i in range(4):
    print(f"Iteration {i}: square = {i * i}")

print("Python execution complete!")
"""
            )
        }

        // JavaScript Starter
        val jsFile = File(workspace, "script.js")
        if (!jsFile.exists() || jsFile.length() == 0L) {
            jsFile.writeText(
                """// Linq Studio - JavaScript Runner
console.log("=== Linq Studio JavaScript Runner ===");

const appName = "Linq Studio IDE";
const version = 2.0;
console.log("Environment: " + appName + " v" + version);

const numbers = [10, 25, 40, 55];
console.log("Input array: " + numbers.join(", "));

let sum = 0;
for (let i = 0; i < numbers.length; i++) {
    sum += numbers[i];
}
console.log("Computed total sum: " + sum);
console.log("Finished executing script.js with status OK");
"""
            )
        }

        // Bash / Shell Starter
        val shFile = File(workspace, "deploy.sh")
        if (!shFile.exists() || shFile.length() == 0L) {
            shFile.writeText(
                """#!/bin/bash
# Linq Studio - Shell Automation Script
echo "=== Linq Studio Bash Script ==="
echo "Running automated environment check..."
echo "Current directory:"
pwd
echo "System date and time:"
date
echo "Available files in workspace:"
ls
echo "Automation task completed successfully!"
"""
            )
        }

        // Kotlin Starter
        val ktFile = File(workspace, "Main.kt")
        if (!ktFile.exists() || ktFile.length() == 0L) {
            ktFile.writeText(
                """// Linq Studio - Kotlin Runner
fun main() {
    println("=== Linq Studio Kotlin Console ===")
    val greeting = "Welcome to Kotlin on Linq Studio!"
    println(greeting)

    val numbers = listOf(5, 12, 19, 26)
    println("Numbers: " + numbers.joinToString(", "))
    println("Execution finished cleanly.")
}
"""
            )
        }
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
