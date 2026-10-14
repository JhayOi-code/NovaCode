package com.example.storage

import com.example.model.FileItem
import java.io.File

interface FileRepository {
    val internalWorkspaceDir: File
    fun listFiles(dir: File): List<FileItem>
    fun readFile(file: File): String
    fun writeFile(file: File, content: String): Boolean
    fun createFile(parent: File, name: String): File?
    fun createFolder(parent: File, name: String): File?
    fun delete(file: File): Boolean
    fun rename(file: File, newName: String): File?
    fun duplicate(file: File): File?
}
