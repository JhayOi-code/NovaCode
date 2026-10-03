package com.example.storage

import com.example.model.FileItem
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface that abstracts file system operations across:
 * 1. App-internal private workspace (POSIX File)
 * 2. External shared device storage (/sdcard, Documents, Downloads)
 * 3. User-granted SAF (Storage Access Framework) directory trees (content:// URIs)
 *
 * All operations are guaranteed to execute on Dispatchers.IO via Kotlin Flows and Coroutines.
 */
interface FileRepository {

    val internalWorkspaceDir: File

    /**
     * Emits the currently accessible storage roots (Internal Workspace, Device Shared Storage, and SAF Grants)
     */
    fun getStorageRoots(): Flow<List<FileItem>>

    /**
     * Emits the list of child files and folders for a given path or SAF content URI.
     */
    fun listFiles(targetPathOrUri: String): Flow<List<FileItem>>

    /**
     * Reads the entire contents of a file as a string Flow.
     */
    fun readFile(targetPathOrUri: String): Flow<String>

    /**
     * Writes text content to the specified path or SAF content URI.
     */
    suspend fun writeFile(targetPathOrUri: String, content: String): Result<Unit>

    /**
     * Creates a new file or directory inside the specified parent container.
     */
    suspend fun createFile(parentPathOrUri: String, name: String, isFolder: Boolean): Result<FileItem>

    /**
     * Permanently deletes a file or directory at the given path or URI.
     */
    suspend fun deleteFile(targetPathOrUri: String): Result<Boolean>

    /**
     * Renames a file or directory.
     */
    suspend fun renameFile(targetPathOrUri: String, newName: String): Result<FileItem>

    /**
     * Recursively searches for files matching a query string (in name or file content).
     */
    fun searchFiles(rootPathOrUri: String, query: String, searchContent: Boolean): Flow<List<FileItem>>

    /**
     * Compresses a directory into a ZIP archive.
     */
    suspend fun zipDirectory(sourcePathOrUri: String, targetZipFile: File): Result<Boolean>
}
