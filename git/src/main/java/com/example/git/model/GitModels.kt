package com.example.git.model

import java.io.File

sealed class GitResult<out T> {
    data class Success<out T>(val data: T, val message: String = "") : GitResult<T>()
    data class Failure(val error: String, val cause: Throwable? = null) : GitResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data
}

data class GitRepository(
    val rootDir: File,
    val branch: String = "main",
    val remotes: Map<String, String> = emptyMap()
)

data class GitStatus(
    val branch: String,
    val stagedFiles: List<String> = emptyList(),
    val unstagedFiles: List<String> = emptyList(),
    val untrackedFiles: List<String> = emptyList()
) {
    val isClean: Boolean get() = stagedFiles.isEmpty() && unstagedFiles.isEmpty() && untrackedFiles.isEmpty()
    val totalChanges: Int get() = stagedFiles.size + unstagedFiles.size + untrackedFiles.size
}

data class GitCommit(
    val id: String,
    val shortId: String = if (id.length >= 7) id.substring(0, 7) else id,
    val message: String,
    val author: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GitAuthor(
    val name: String = "Linq Studio Developer",
    val email: String = "dev@linqstudio.local"
)

data class GitCredentials(
    val username: String = "",
    val tokenOrPassword: String = ""
) {
    val token: String get() = tokenOrPassword
    val password: String get() = tokenOrPassword
}

data class GitPushResult(
    val success: Boolean,
    val remote: String,
    val branch: String,
    val commitsCount: Int,
    val summary: String
)

data class GitPullResult(
    val success: Boolean,
    val remote: String,
    val branch: String,
    val commitsReceived: Int,
    val summary: String
)

data class GitHubAccount(
    val username: String = "",
    val token: String = "",
    val email: String = "",
    val isLinked: Boolean = false,
    val linkedAt: Long = 0L,
    val displayName: String = "",
    val publicRepos: Int = 0
) {
    val maskedToken: String
        get() = if (token.length > 8) "${token.take(4)}••••••••${token.takeLast(4)}" else "••••••••"
}
