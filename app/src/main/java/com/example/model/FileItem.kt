package com.example.model

import java.io.File

data class FileItem(
    val file: File
) {
    val name: String get() = file.name
    val path: String get() = file.absolutePath
    val isDirectory: Boolean get() = file.isDirectory
    val length: Long get() = file.length()
    val lastModified: Long get() = file.lastModified()
    val extension: String get() = file.extension
}
