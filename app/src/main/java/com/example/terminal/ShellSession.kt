package com.example.terminal

import java.io.File

data class ShellSession(
    val id: String,
    val name: String,
    val workingDir: File,
    val lines: List<com.example.model.TerminalLine> = emptyList(),
    val commandHistory: List<String> = emptyList(),
    val historyIndex: Int = -1,
    val isRunning: Boolean = false,
    val installedPackages: List<String> = listOf("coreutils", "busybox", "curl", "wget", "git", "python3")
)
