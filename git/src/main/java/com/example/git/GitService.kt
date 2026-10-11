package com.example.git

import com.example.git.model.GitAuthor
import com.example.git.model.GitCommit
import com.example.git.model.GitCredentials
import com.example.git.model.GitHubAccount
import com.example.git.model.GitPullResult
import com.example.git.model.GitPushResult
import com.example.git.model.GitRepository
import com.example.git.model.GitResult
import com.example.git.model.GitStatus
import java.io.File

/**
 * Asynchronous Git lifecycle service using Kotlin Coroutines.
 * Handles core VCS operations including clone, commit, push, pull, status, and logging.
 */
interface GitService {

    suspend fun init(directory: File, initialBranch: String = "main"): GitResult<GitRepository>

    suspend fun clone(
        repoUrl: String,
        targetDirectory: File,
        credentials: GitCredentials? = null,
        branch: String = "main"
    ): GitResult<GitRepository>

    suspend fun status(directory: File): GitResult<GitStatus>

    suspend fun add(directory: File, filePattern: String = "."): GitResult<List<String>>

    suspend fun commit(
        directory: File,
        message: String,
        author: GitAuthor = GitAuthor()
    ): GitResult<GitCommit>

    suspend fun push(
        directory: File,
        remote: String = "origin",
        branch: String = "main",
        credentials: GitCredentials? = null
    ): GitResult<GitPushResult>

    suspend fun pull(
        directory: File,
        remote: String = "origin",
        branch: String = "main",
        credentials: GitCredentials? = null
    ): GitResult<GitPullResult>

    suspend fun log(directory: File, maxCount: Int = 20): GitResult<List<GitCommit>>

    suspend fun currentBranch(directory: File): GitResult<String>

    suspend fun branches(directory: File): GitResult<List<String>>

    suspend fun setRemote(directory: File, name: String = "origin", url: String): GitResult<Unit>

    suspend fun getRemote(directory: File, name: String = "origin"): GitResult<String?>

    suspend fun verifyGitHubCredentials(username: String, token: String): GitResult<GitHubAccount>
}
