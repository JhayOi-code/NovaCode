package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val isWritable: Boolean = true,
    val uriString: String? = null,
    val isExpanded: Boolean = false,
    val children: List<FileItem> = emptyList()
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")

    val language: SupportedLanguage
        get() = SupportedLanguage.fromFilename(name)

    val formattedSize: String
        get() {
            if (isDirectory) return "--"
            if (size < 1024) return "$size B"
            val kb = size / 1024.0
            if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
            val mb = kb / 1024.0
            return String.format(Locale.US, "%.1f MB", mb)
        }

    val formattedDate: String
        get() {
            if (lastModified == 0L) return ""
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}
