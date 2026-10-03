package com.example.model

enum class TerminalLineType {
    COMMAND,
    STDOUT,
    STDERR,
    INFO,
    SUCCESS,
    SYSTEM
}

data class TerminalLine(
    val text: String,
    val type: TerminalLineType = TerminalLineType.STDOUT,
    val timestamp: Long = System.currentTimeMillis()
)
