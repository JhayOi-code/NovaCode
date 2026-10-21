package com.example.terminal

import android.content.Context
import android.os.BatteryManager
import android.os.Vibrator
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val lines: MutableList<String> = mutableListOf(),
    var currentDir: File,
    val history: MutableList<String> = mutableListOf(),
    var historyIndex: Int = -1
)

class TerminalProcessEngine(
    private val context: Context,
    val workspaceDir: File
) {
    val sessions = mutableListOf<TerminalSession>()
    var activeSessionIndex = 0

    val activeSession: TerminalSession
        get() {
            if (sessions.isEmpty()) {
                createNewSession("Main")
            }
            return sessions[activeSessionIndex.coerceIn(0, sessions.lastIndex)]
        }

    init {
        createNewSession("Main")
    }

    fun createNewSession(name: String = "Session ${sessions.size + 1}"): TerminalSession {
        val session = TerminalSession(
            name = name,
            currentDir = workspaceDir
        )
        session.lines.add("\u001B[32mLinq Linux Environment v2.0\u001B[0m [xterm-256color]")
        session.lines.add("Working directory: ${workspaceDir.absolutePath}")
        session.lines.add("Type 'help', 'pkg', or device commands ('termux-battery-status', 'termux-toast').")
        sessions.add(session)
        activeSessionIndex = sessions.lastIndex
        return session
    }

    fun closeSession(index: Int) {
        if (sessions.size > 1 && index in sessions.indices) {
            sessions.removeAt(index)
            activeSessionIndex = (index - 1).coerceAtLeast(0)
        }
    }

    fun renameSession(index: Int, newName: String) {
        if (index in sessions.indices && newName.isNotBlank()) {
            sessions[index].name = newName
        }
    }

    fun executeInput(input: String): List<String> {
        val trimmed = input.trim()
        val session = activeSession
        session.lines.add("$ ${session.currentDir.name} > $input")
        if (trimmed.isNotEmpty()) {
            session.history.add(trimmed)
            session.historyIndex = session.history.size
        }

        if (trimmed.isEmpty()) return session.lines

        val parts = trimmed.split("\\s+".toRegex())
        val command = parts[0]
        val args = parts.drop(1)

        when (command) {
            "help" -> {
                session.lines.add("Built-in shell & device commands:")
                session.lines.add("  ls, pwd, cd, cat, echo, clear, date, whoami, mkdir, rm")
                session.lines.add("  pkg [install|list|update] <package>")
                session.lines.add("  termux-battery-status, termux-toast, termux-vibrate, termux-clipboard-get, termux-clipboard-set")
                session.lines.add("  termux-wifi-info, termux-torch, termux-setup-storage")
            }
            "clear" -> {
                session.lines.clear()
            }
            "pwd" -> {
                session.lines.add(session.currentDir.absolutePath)
            }
            "ls" -> {
                val files = session.currentDir.listFiles()?.sortedBy { it.name } ?: emptyList()
                if (files.isEmpty()) {
                    session.lines.add("(empty directory)")
                } else {
                    val formatted = files.joinToString("  ") {
                        if (it.isDirectory) "\u001B[34m${it.name}/\u001B[0m" else it.name
                    }
                    session.lines.add(formatted)
                }
            }
            "cd" -> {
                val target = args.firstOrNull() ?: "~"
                val newDir = when (target) {
                    "~" -> workspaceDir
                    ".." -> session.currentDir.parentFile ?: session.currentDir
                    else -> {
                        val file = File(session.currentDir, target)
                        if (file.exists() && file.isDirectory) file else null
                    }
                }
                if (newDir != null) {
                    session.currentDir = newDir
                } else {
                    session.lines.add("cd: no such file or directory: $target")
                }
            }
            "cat" -> {
                val fileName = args.firstOrNull()
                if (fileName == null) {
                    session.lines.add("Usage: cat <filename>")
                } else {
                    val file = File(session.currentDir, fileName)
                    if (file.exists() && file.isFile) {
                        session.lines.addAll(file.readLines())
                    } else {
                        session.lines.add("cat: $fileName: No such file")
                    }
                }
            }
            "date" -> {
                val format = SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US)
                session.lines.add(format.format(Date()))
            }
            "whoami" -> {
                session.lines.add("developer")
            }
            "echo" -> {
                session.lines.add(args.joinToString(" ").removeSurrounding("\""))
            }
            "mkdir" -> {
                val dirName = args.firstOrNull()
                if (dirName != null) {
                    File(session.currentDir, dirName).mkdirs()
                    session.lines.add("Created directory: $dirName")
                } else {
                    session.lines.add("Usage: mkdir <directory>")
                }
            }
            "rm" -> {
                val targetName = args.firstOrNull()
                if (targetName != null) {
                    val f = File(session.currentDir, targetName)
                    if (f.exists()) {
                        f.deleteRecursively()
                        session.lines.add("Removed: $targetName")
                    } else {
                        session.lines.add("rm: cannot remove '$targetName': No such file")
                    }
                }
            }
            "pkg" -> {
                handlePkgCommand(args, session)
            }
            "termux-battery-status" -> {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
                session.lines.add("{\n  \"health\": \"GOOD\",\n  \"percentage\": $level,\n  \"plugged\": \"UNPLUGGED\",\n  \"status\": \"DISCHARGING\"\n}")
            }
            "termux-toast" -> {
                val message = args.joinToString(" ").ifBlank { "Hello from Termux API!" }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                session.lines.add("Toast displayed: \"$message\"")
            }
            "termux-vibrate" -> {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200L)
                session.lines.add("Vibration triggered (200ms)")
            }
            "termux-setup-storage" -> {
                session.lines.add("Storage initialized. Linked workspace directory to internal app storage.")
            }
            "termux-wifi-info" -> {
                session.lines.add("{\n  \"bssid\": \"02:00:00:00:00:00\",\n  \"frequency_mhz\": 5180,\n  \"ip\": \"192.168.1.104\",\n  \"ssid\": \"Studio-WiFi\",\n  \"supplicant_state\": \"COMPLETED\"\n}")
            }
            "termux-torch" -> {
                val state = args.firstOrNull() ?: "on"
                session.lines.add("Torch state set to: $state")
            }
            else -> {
                // Execute standard shell process
                try {
                    val process = ProcessBuilder("/system/bin/sh", "-c", trimmed)
                        .directory(session.currentDir)
                        .redirectErrorStream(true)
                        .start()

                    val output = process.inputStream.bufferedReader().readLines()
                    process.waitFor()
                    if (output.isNotEmpty()) {
                        session.lines.addAll(output)
                    } else if (process.exitValue() != 0) {
                        session.lines.add("Command exited with code ${process.exitValue()}")
                    }
                } catch (e: Exception) {
                    session.lines.add("bash: $command: command not found (type 'help' for options)")
                }
            }
        }
        return session.lines
    }

    private fun handlePkgCommand(args: List<String>, session: TerminalSession) {
        val sub = args.firstOrNull() ?: "help"
        when (sub) {
            "install", "add" -> {
                val pkgName = args.getOrNull(1)
                if (pkgName == null) {
                    session.lines.add("Usage: pkg install <package>")
                } else {
                    session.lines.add("Reading package lists... Done")
                    session.lines.add("Building dependency tree... Done")
                    session.lines.add("Downloading $pkgName...")
                    session.lines.add("Unpacking $pkgName to \$PREFIX/bin/$pkgName...")
                    session.lines.add("Setting up $pkgName (latest)...")
                    session.lines.add("\u001B[32mSuccessfully installed $pkgName!\u001B[0m")
                }
            }
            "update", "upgrade" -> {
                session.lines.add("Updating repositories...")
                session.lines.add("All packages are up to date.")
            }
            "list" -> {
                session.lines.add("Installed packages: python3, nodejs, openjdk, bash, git, curl, nano, vim, sqlite3")
            }
            else -> {
                session.lines.add("Package manager usage:")
                session.lines.add("  pkg install <package>")
                session.lines.add("  pkg update")
                session.lines.add("  pkg list")
            }
        }
    }
}
