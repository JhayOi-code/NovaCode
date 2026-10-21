package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CapturedLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO",
    val message: String,
    val source: String? = null,
    val lineNumber: Int? = null
) {
    val isError: Boolean get() = level.equals("ERROR", ignoreCase = true) || level.equals("FATAL", ignoreCase = true)
    val isWarning: Boolean get() = level.equals("WARN", ignoreCase = true) || level.equals("WARNING", ignoreCase = true)
    val formattedTime: String get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
