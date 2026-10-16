package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CapturedLog(
    val id: String = UUID.randomUUID().toString(),
    val level: String = "LOG", // LOG, INFO, WARN, ERROR
    val message: String,
    val source: String? = null,
    val lineNumber: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val isError: Boolean
        get() = level.equals("ERROR", ignoreCase = true)

    val isWarning: Boolean
        get() = level.equals("WARN", ignoreCase = true) || level.equals("WARNING", ignoreCase = true)
}
