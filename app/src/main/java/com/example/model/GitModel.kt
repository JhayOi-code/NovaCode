package com.example.model

enum class GitFileStatus {
    MODIFIED, ADDED, DELETED, UNTRACKED
}

data class GitFileChange(
    val filePath: String,
    val fileName: String,
    val status: GitFileStatus,
    val isStaged: Boolean = false,
    val additions: Int = 0,
    val deletions: Int = 0
)

data class GitCommit(
    val id: String,
    val message: String,
    val author: String,
    val timestamp: Long,
    val filesChangedCount: Int
)

data class GitRepositoryStatus(
    val currentBranch: String = "main",
    val availableBranches: List<String> = listOf("main", "develop", "feature/editor-speed"),
    val changes: List<GitFileChange> = emptyList(),
    val commits: List<GitCommit> = emptyList(),
    val isClean: Boolean = true,
    val aheadCount: Int = 0,
    val behindCount: Int = 0
)
