package com.example.model

import java.io.File

enum class GitFileStatus {
    MODIFIED,
    UNTRACKED,
    STAGED,
    DELETED
}

data class GitFileItem(
    val file: File,
    val relativePath: String,
    val status: GitFileStatus
)

data class GitCommit(
    val id: String,
    val message: String,
    val author: String,
    val timestamp: Long,
    val fileCount: Int,
    val branch: String
)

data class GitStatus(
    val branch: String = "main",
    val branches: List<String> = listOf("main", "feature/workspace", "dev"),
    val stagedFiles: List<GitFileItem> = emptyList(),
    val unstagedFiles: List<GitFileItem> = emptyList(),
    val commits: List<GitCommit> = emptyList(),
    val isClean: Boolean = stagedFiles.isEmpty() && unstagedFiles.isEmpty(),
    val lastPushStatus: String? = null
)
