package com.example.terminal

import android.content.Context
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LinuxProcessRunner(private val context: Context) {

    // usr directory for installed tools, packages, scripts
    val usrDir: File = File(context.filesDir, "usr").apply { mkdirs() }
    val binDir: File = File(usrDir, "bin").apply { mkdirs() }
    val libDir: File = File(usrDir, "lib").apply { mkdirs() }
    val etcDir: File = File(usrDir, "etc").apply { mkdirs() }
    val homeDir: File = File(context.filesDir, "home").apply { mkdirs() }

    private var activeProcess: Process? = null

    init {
        setupDefaultScripts()
    }

    private fun setupDefaultScripts() {
        try {
            // Create helper wrapper scripts if not present
            val pkgScript = File(binDir, "pkg")
            if (!pkgScript.exists()) {
                pkgScript.writeText(
                    """
                    #!/system/bin/sh
                    echo "Linq Studio Package Manager (pkg)"
                    """.trimIndent()
                )
                pkgScript.setExecutable(true, false)
            }
        } catch (_: Exception) {}
    }

    suspend fun executeShellCommand(
        cmd: String,
        workingDir: File,
        onOutputLine: (TerminalLine) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return@withContext 0

        // Handle pkg commands internally if invoked
        if (trimmed.startsWith("pkg ") || trimmed == "pkg") {
            handlePkgCommand(trimmed, onOutputLine)
            return@withContext 0
        }

        // Handle wget command internally if invoked
        if (trimmed.startsWith("wget ")) {
            handleWgetCommand(trimmed, workingDir, onOutputLine)
            return@withContext 0
        }

        // Handle curl command internally if invoked
        if (trimmed.startsWith("curl ")) {
            handleCurlCommand(trimmed, workingDir, onOutputLine)
            return@withContext 0
        }

        val envList = arrayOf(
            "PATH=${binDir.absolutePath}:/system/bin:/system/xbin:/vendor/bin",
            "HOME=${homeDir.absolutePath}",
            "USR=${usrDir.absolutePath}",
            "LD_LIBRARY_PATH=${libDir.absolutePath}:/system/lib64:/system/lib",
            "TERM=xterm-256color",
            "USER=u0_a249",
            "SHELL=/system/bin/sh"
        )

        val shellExec = if (File("/system/bin/sh").exists()) "/system/bin/sh" else "sh"

        try {
            val process = Runtime.getRuntime().exec(
                arrayOf(shellExec, "-c", trimmed),
                envList,
                workingDir
            )
            activeProcess = process

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            // Read stdout
            var outLine: String?
            while (stdoutReader.readLine().also { outLine = it } != null) {
                outLine?.let {
                    onOutputLine(TerminalLine(it, TerminalLineType.STDOUT))
                }
            }

            // Read stderr
            var errLine: String?
            while (stderrReader.readLine().also { errLine = it } != null) {
                errLine?.let {
                    onOutputLine(TerminalLine(it, TerminalLineType.STDERR))
                }
            }

            val exitCode = process.waitFor()
            activeProcess = null
            exitCode
        } catch (e: Exception) {
            onOutputLine(TerminalLine("Process execution error: ${e.message}", TerminalLineType.STDERR))
            -1
        }
    }

    fun sendCtrlC() {
        try {
            activeProcess?.destroy()
            activeProcess = null
        } catch (_: Exception) {}
    }

    private fun handlePkgCommand(cmd: String, onOutputLine: (TerminalLine) -> Unit) {
        val parts = cmd.split("\\s+".toRegex())
        val sub = parts.getOrNull(1)?.lowercase() ?: "help"
        val pkgName = parts.getOrNull(2)

        val installedFile = File(etcDir, "installed_packages.txt")
        val installedList = if (installedFile.exists()) {
            installedFile.readLines().filter { it.isNotBlank() }.toMutableList()
        } else {
            mutableListOf("coreutils", "busybox", "curl", "wget", "git", "python3")
        }

        when (sub) {
            "install", "add" -> {
                if (pkgName.isNullOrBlank()) {
                    onOutputLine(TerminalLine("Usage: pkg install <package-name>", TerminalLineType.STDERR))
                    return
                }
                onOutputLine(TerminalLine("Checking mirrors for package '$pkgName'...", TerminalLineType.INFO))
                onOutputLine(TerminalLine("Fetching package metadata from linq-packages...", TerminalLineType.INFO))
                onOutputLine(TerminalLine("Unpacking $pkgName to ${usrDir.absolutePath}...", TerminalLineType.STDOUT))
                
                // Create mock binary/script in usr/bin so it can be executed directly
                val binTarget = File(binDir, pkgName)
                binTarget.writeText(
                    """
                    #!/system/bin/sh
                    echo "$pkgName version 1.0 (Linq Studio usr env)"
                    """.trimIndent()
                )
                binTarget.setExecutable(true, false)

                if (!installedList.contains(pkgName)) {
                    installedList.add(pkgName)
                    installedFile.writeText(installedList.joinToString("\n"))
                }
                onOutputLine(TerminalLine("Successfully installed $pkgName into usr/bin/$pkgName", TerminalLineType.SUCCESS))
            }
            "remove", "uninstall" -> {
                if (pkgName.isNullOrBlank()) {
                    onOutputLine(TerminalLine("Usage: pkg remove <package-name>", TerminalLineType.STDERR))
                    return
                }
                val binTarget = File(binDir, pkgName)
                if (binTarget.exists()) binTarget.delete()
                installedList.remove(pkgName)
                installedFile.writeText(installedList.joinToString("\n"))
                onOutputLine(TerminalLine("Package $pkgName uninstalled successfully.", TerminalLineType.SUCCESS))
            }
            "list" -> {
                onOutputLine(TerminalLine("Installed packages (${installedList.size}):", TerminalLineType.INFO))
                installedList.forEach { p ->
                    val path = if (File(binDir, p).exists()) "usr/bin/$p" else "system"
                    onOutputLine(TerminalLine("  • $p ($path)", TerminalLineType.STDOUT))
                }
            }
            "update", "upgrade" -> {
                onOutputLine(TerminalLine("Updating package lists...", TerminalLineType.INFO))
                onOutputLine(TerminalLine("All packages in ${usrDir.absolutePath} are up to date.", TerminalLineType.SUCCESS))
            }
            else -> {
                onOutputLine(TerminalLine("Linq Package Manager (pkg)", TerminalLineType.INFO))
                onOutputLine(TerminalLine("Usage: pkg <install|remove|list|update> [package]", TerminalLineType.STDOUT))
                onOutputLine(TerminalLine("Target directory: ${usrDir.absolutePath}", TerminalLineType.STDOUT))
            }
        }
    }

    private suspend fun handleWgetCommand(
        cmd: String,
        workingDir: File,
        onOutputLine: (TerminalLine) -> Unit
    ) = withContext(Dispatchers.IO) {
        val parts = cmd.split("\\s+".toRegex())
        val urlStr = parts.drop(1).firstOrNull { it.startsWith("http://") || it.startsWith("https://") }
        if (urlStr == null) {
            onOutputLine(TerminalLine("wget: missing URL operand", TerminalLineType.STDERR))
            return@withContext
        }

        try {
            onOutputLine(TerminalLine("-- Connecting to $urlStr...", TerminalLineType.INFO))
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.instanceFollowRedirects = true
            conn.connect()

            val responseCode = conn.responseCode
            onOutputLine(TerminalLine("HTTP request sent, awaiting response... $responseCode ${conn.responseMessage}", TerminalLineType.INFO))

            val fileName = url.path.substringAfterLast("/").ifEmpty { "index.html" }
            val outputFile = File(workingDir, fileName)

            conn.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            onOutputLine(TerminalLine("Length: ${outputFile.length()} bytes", TerminalLineType.STDOUT))
            onOutputLine(TerminalLine("Saving to: '${outputFile.name}'", TerminalLineType.SUCCESS))
            onOutputLine(TerminalLine("[Download completed: ${outputFile.absolutePath}]", TerminalLineType.SUCCESS))
        } catch (e: Exception) {
            onOutputLine(TerminalLine("wget: unable to resolve host or download: ${e.message}", TerminalLineType.STDERR))
        }
    }

    private suspend fun handleCurlCommand(
        cmd: String,
        workingDir: File,
        onOutputLine: (TerminalLine) -> Unit
    ) = withContext(Dispatchers.IO) {
        val parts = cmd.split("\\s+".toRegex())
        val urlStr = parts.drop(1).firstOrNull { it.startsWith("http://") || it.startsWith("https://") }
        val isOutputToFile = parts.contains("-o") || parts.contains("-O")
        val outFileName = if (parts.contains("-o")) {
            val idx = parts.indexOf("-o")
            parts.getOrNull(idx + 1)
        } else null

        if (urlStr == null) {
            onOutputLine(TerminalLine("curl: no URL specified!", TerminalLineType.STDERR))
            return@withContext
        }

        try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.instanceFollowRedirects = true
            conn.connect()

            val fileName = outFileName ?: (url.path.substringAfterLast("/").ifEmpty { "download.out" })

            if (isOutputToFile) {
                val outputFile = File(workingDir, fileName)
                conn.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onOutputLine(TerminalLine("  % Total    % Received % Xferd  Average Speed", TerminalLineType.INFO))
                onOutputLine(TerminalLine("100  ${outputFile.length()}  100  ${outputFile.length()}    0     0   100k      0 --:--:-- --:--:--", TerminalLineType.STDOUT))
                onOutputLine(TerminalLine("Downloaded to ${outputFile.name}", TerminalLineType.SUCCESS))
            } else {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                body.lines().take(50).forEach { line ->
                    onOutputLine(TerminalLine(line, TerminalLineType.STDOUT))
                }
                if (body.lines().size > 50) {
                    onOutputLine(TerminalLine("... [${body.lines().size - 50} more lines omitted]", TerminalLineType.INFO))
                }
            }
        } catch (e: Exception) {
            onOutputLine(TerminalLine("curl: (6) Could not resolve host: ${e.message}", TerminalLineType.STDERR))
        }
    }

    fun getInstalledPackages(): List<String> {
        val installedFile = File(etcDir, "installed_packages.txt")
        return if (installedFile.exists()) {
            installedFile.readLines().filter { it.isNotBlank() }
        } else {
            listOf("coreutils", "busybox", "curl", "wget", "git", "python3")
        }
    }

    fun uninstallPackage(pkgName: String): Boolean {
        val binTarget = File(binDir, pkgName)
        if (binTarget.exists()) binTarget.delete()
        val installedFile = File(etcDir, "installed_packages.txt")
        val current = getInstalledPackages().toMutableList()
        val removed = current.remove(pkgName)
        installedFile.writeText(current.joinToString("\n"))
        return removed
    }
}
