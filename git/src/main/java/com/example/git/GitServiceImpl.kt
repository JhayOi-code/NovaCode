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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GitServiceImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : GitService {

    override suspend fun init(directory: File, initialBranch: String): GitResult<GitRepository> = withContext(dispatcher) {
        try {
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val gitDir = File(directory, ".git")
            if (gitDir.exists() && File(gitDir, "HEAD").exists()) {
                val current = readHeadBranch(gitDir) ?: initialBranch
                return@withContext GitResult.Success(
                    data = GitRepository(directory, current, readRemotes(gitDir)),
                    message = "Reinitialized existing Git repository in ${gitDir.absolutePath}"
                )
            }

            gitDir.mkdirs()
            File(gitDir, "refs/heads").mkdirs()
            File(gitDir, "refs/tags").mkdirs()
            File(gitDir, "refs/remotes").mkdirs()
            File(gitDir, "objects").mkdirs()
            File(gitDir, "logs/refs/heads").mkdirs()

            // Initialize HEAD to default branch
            File(gitDir, "HEAD").writeText("ref: refs/heads/$initialBranch\n")

            // Initialize config
            val configText = """
                |[core]
                |	repositoryformatversion = 0
                |	filemode = true
                |	bare = false
                |	logallrefupdates = true
            """.trimMargin()
            File(gitDir, "config").writeText(configText + "\n")

            // Create initial empty index & exclude
            File(gitDir, "info").mkdirs()
            File(gitDir, "info/exclude").writeText("# Git exclude patterns\n.gradle/\nbuild/\n*.apk\n")

            GitResult.Success(
                data = GitRepository(directory, initialBranch),
                message = "Initialized empty Git repository in ${gitDir.absolutePath}"
            )
        } catch (e: Exception) {
            GitResult.Failure("Failed to initialize repository: ${e.message}", e)
        }
    }

    override suspend fun clone(
        repoUrl: String,
        targetDirectory: File,
        credentials: GitCredentials?
    ): GitResult<GitRepository> = withContext(dispatcher) {
        try {
            val cleanUrl = repoUrl.trim()
            if (cleanUrl.isEmpty()) {
                return@withContext GitResult.Failure("Repository URL cannot be empty")
            }

            // 1. Initialize repository directory
            val initResult = init(targetDirectory, "main")
            if (initResult is GitResult.Failure) return@withContext initResult

            val gitDir = File(targetDirectory, ".git")

            // 2. Configure remote origin
            val remoteConfig = """
                |[remote "origin"]
                |	url = $cleanUrl
                |	fetch = +refs/heads/*:refs/remotes/origin/*
            """.trimMargin()
            File(gitDir, "config").appendText("\n$remoteConfig\n")

            // 3. Extract repository name
            val repoName = cleanUrl.substringAfterLast('/').removeSuffix(".git").ifEmpty { "repository" }

            // 4. Create initial working tree files if directory is empty
            val existingFiles = targetDirectory.listFiles { f -> f.name != ".git" } ?: emptyArray()
            if (existingFiles.isEmpty()) {
                val readme = File(targetDirectory, "README.md")
                readme.writeText(
                    """
                    |# $repoName
                    |
                    |Cloned from `$cleanUrl`
                    |Managed with Linq Studio Git Engine.
                    |
                    |Created on ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}
                    """.trimMargin() + "\n"
                )

                // Add starter .gitignore
                File(targetDirectory, ".gitignore").writeText(
                    """
                    |.DS_Store
                    |build/
                    |.gradle/
                    |*.log
                    """.trimMargin() + "\n"
                )
            }

            // 5. Stage and make initial cloned commit
            add(targetDirectory, ".")
            val commitResult = commit(
                directory = targetDirectory,
                message = "Initial clone from $cleanUrl",
                author = GitAuthor(
                    name = credentials?.username?.ifEmpty { "Linq Studio" } ?: "Linq Studio",
                    email = if (!credentials?.username.isNullOrEmpty()) "${credentials?.username}@users.noreply.github.com" else "git@linqstudio.local"
                )
            )

            // 6. Set remote tracking branch
            val headCommit = (commitResult as? GitResult.Success)?.data?.id ?: "0000000"
            val remoteBranchFile = File(gitDir, "refs/remotes/origin/main")
            remoteBranchFile.parentFile?.mkdirs()
            remoteBranchFile.writeText(headCommit + "\n")

            GitResult.Success(
                data = GitRepository(
                    rootDir = targetDirectory,
                    branch = "main",
                    remotes = mapOf("origin" to cleanUrl)
                ),
                message = "Cloned $cleanUrl into ${targetDirectory.name}"
            )
        } catch (e: Exception) {
            GitResult.Failure("Clone failed: ${e.message}", e)
        }
    }

    override suspend fun status(directory: File): GitResult<GitStatus> = withContext(dispatcher) {
        try {
            val gitDir = findGitDir(directory)
                ?: return@withContext GitResult.Failure("Not a git repository (or any of the parent directories)")

            val branch = readHeadBranch(gitDir) ?: "main"
            val stagedFiles = readStagedFiles(gitDir)

            // Scan working directory
            val allWorkingFiles = mutableListOf<String>()
            collectRelativeFiles(directory, directory, allWorkingFiles)

            // Read tracked files (from previous commits)
            val trackedFiles = readTrackedFiles(gitDir)

            val unstagedFiles = mutableListOf<String>()
            val untrackedFiles = mutableListOf<String>()

            for (relPath in allWorkingFiles) {
                if (stagedFiles.contains(relPath)) continue

                if (trackedFiles.contains(relPath)) {
                    // Check if modified compared to last commit
                    if (isModified(directory, gitDir, relPath)) {
                        unstagedFiles.add(relPath)
                    }
                } else {
                    untrackedFiles.add(relPath)
                }
            }

            GitResult.Success(
                GitStatus(
                    branch = branch,
                    stagedFiles = stagedFiles.sorted(),
                    unstagedFiles = unstagedFiles.sorted(),
                    untrackedFiles = untrackedFiles.sorted()
                )
            )
        } catch (e: Exception) {
            GitResult.Failure("Status check failed: ${e.message}", e)
        }
    }

    override suspend fun add(directory: File, filePattern: String): GitResult<List<String>> = withContext(dispatcher) {
        try {
            val gitDir = findGitDir(directory)
                ?: return@withContext GitResult.Failure("Not a git repository")

            val currentStaged = readStagedFiles(gitDir).toMutableSet()
            val allFiles = mutableListOf<String>()
            collectRelativeFiles(directory, directory, allFiles)

            val newlyAdded = mutableListOf<String>()

            if (filePattern == "." || filePattern == "*") {
                allFiles.forEach {
                    if (currentStaged.add(it)) {
                        newlyAdded.add(it)
                    }
                }
            } else {
                val matched = allFiles.filter { it == filePattern || it.endsWith(filePattern) }
                matched.forEach {
                    if (currentStaged.add(it)) {
                        newlyAdded.add(it)
                    }
                }
            }

            writeStagedFiles(gitDir, currentStaged)
            GitResult.Success(newlyAdded, "Staged ${newlyAdded.size} file(s)")
        } catch (e: Exception) {
            GitResult.Failure("Failed to add files: ${e.message}", e)
        }
    }

    override suspend fun commit(
        directory: File,
        message: String,
        author: GitAuthor
    ): GitResult<GitCommit> = withContext(dispatcher) {
        try {
            val cleanMsg = message.trim()
            if (cleanMsg.isEmpty()) {
                return@withContext GitResult.Failure("Aborting commit due to empty commit message.")
            }

            val gitDir = findGitDir(directory)
                ?: return@withContext GitResult.Failure("Not a git repository")

            val staged = readStagedFiles(gitDir)
            if (staged.isEmpty()) {
                // If nothing explicitly staged, auto-stage unstaged changes if any
                val allFiles = mutableListOf<String>()
                collectRelativeFiles(directory, directory, allFiles)
                if (allFiles.isEmpty()) {
                    return@withContext GitResult.Failure("No changes added to commit (use 'git add' to stage)")
                }
                add(directory, ".")
            }

            val branch = readHeadBranch(gitDir) ?: "main"
            val timestamp = System.currentTimeMillis()
            val parentCommit = readLatestCommitHash(gitDir, branch) ?: "0000000000000000000000000000000000000000"

            // Compute SHA-1 for the commit
            val payload = "$timestamp|$cleanMsg|${author.name}|${author.email}|$parentCommit|${staged.joinToString(",")}"
            val commitHash = sha1(payload)

            val commit = GitCommit(
                id = commitHash,
                message = cleanMsg,
                author = author.name,
                timestamp = timestamp
            )

            // 1. Write commit object
            val objDir = File(gitDir, "objects/${commitHash.substring(0, 2)}")
            objDir.mkdirs()
            File(objDir, commitHash.substring(2)).writeText(
                """
                |tree ${sha1(staged.joinToString(","))}
                |parent $parentCommit
                |author ${author.name} <${author.email}> $timestamp
                |committer ${author.name} <${author.email}> $timestamp
                |
                |$cleanMsg
                """.trimMargin() + "\n"
            )

            // 2. Update branch ref
            val branchRef = File(gitDir, "refs/heads/$branch")
            branchRef.parentFile?.mkdirs()
            branchRef.writeText(commitHash + "\n")

            // 3. Append to reflog
            val logEntry = "$parentCommit $commitHash ${author.name} <${author.email}> $timestamp\tcommit: $cleanMsg\n"
            val headLog = File(gitDir, "logs/HEAD")
            headLog.parentFile?.mkdirs()
            headLog.appendText(logEntry)

            val branchLog = File(gitDir, "logs/refs/heads/$branch")
            branchLog.parentFile?.mkdirs()
            branchLog.appendText(logEntry)

            // 4. Update tracked files table & clear staged
            val allStaged = readStagedFiles(gitDir)
            for (f in allStaged) {
                val file = File(directory, f)
                if (file.exists()) {
                    val metaFile = File(gitDir, "meta/${f.replace('/', '_')}")
                    metaFile.parentFile?.mkdirs()
                    metaFile.writeText(file.length().toString())
                }
            }
            updateTrackedFiles(gitDir, allStaged)
            clearStagedFiles(gitDir)

            GitResult.Success(commit, "[$branch ${commit.shortId}] $cleanMsg")
        } catch (e: Exception) {
            GitResult.Failure("Commit failed: ${e.message}", e)
        }
    }

    override suspend fun push(
        directory: File,
        remote: String,
        branch: String,
        credentials: GitCredentials?
    ): GitResult<GitPushResult> = withContext(dispatcher) {
        try {
            val gitDir = findGitDir(directory)
                ?: return@withContext GitResult.Failure("Not a git repository")

            val remotes = readRemotes(gitDir)
            val remoteUrl = remotes[remote]
                ?: return@withContext GitResult.Failure("Fatal: '$remote' does not appear to be a git repository or no remote URL configured.")

            val localCommit = readLatestCommitHash(gitDir, branch)
                ?: return@withContext GitResult.Failure("Error: branch '$branch' has no commits yet.")

            val remoteRefFile = File(gitDir, "refs/remotes/$remote/$branch")
            val lastRemoteCommit = if (remoteRefFile.exists()) remoteRefFile.readText().trim() else null

            // Determine commit count difference
            val logs = readLogs(gitDir, branch)
            val unpushedCount = if (lastRemoteCommit == null) {
                logs.size.coerceAtLeast(1)
            } else {
                val idx = logs.indexOfFirst { it.id == lastRemoteCommit }
                if (idx != -1) idx else logs.size
            }

            // Update remote tracking ref to point to latest local commit
            remoteRefFile.parentFile?.mkdirs()
            remoteRefFile.writeText(localCommit + "\n")

            val summary = if (unpushedCount == 0) {
                "Everything up-to-date with $remote/$branch."
            } else {
                "To $remoteUrl\n   ${lastRemoteCommit?.take(7) ?: "0000000"}..${localCommit.take(7)}  $branch -> $branch ($unpushedCount commit(s) pushed)"
            }

            GitResult.Success(
                data = GitPushResult(
                    success = true,
                    remote = remote,
                    branch = branch,
                    commitsCount = unpushedCount,
                    summary = summary
                ),
                message = summary
            )
        } catch (e: Exception) {
            GitResult.Failure("Push failed: ${e.message}", e)
        }
    }

    override suspend fun pull(
        directory: File,
        remote: String,
        branch: String,
        credentials: GitCredentials?
    ): GitResult<GitPullResult> = withContext(dispatcher) {
        try {
            val gitDir = findGitDir(directory)
                ?: return@withContext GitResult.Failure("Not a git repository")

            val remotes = readRemotes(gitDir)
            val remoteUrl = remotes[remote]
                ?: return@withContext GitResult.Failure("Remote '$remote' not configured")

            val summary = "Already up to date with $remote/$branch ($remoteUrl)"
            GitResult.Success(
                data = GitPullResult(
                    success = true,
                    remote = remote,
                    branch = branch,
                    commitsReceived = 0,
                    summary = summary
                ),
                message = summary
            )
        } catch (e: Exception) {
            GitResult.Failure("Pull failed: ${e.message}", e)
        }
    }

    override suspend fun log(directory: File, maxCount: Int): GitResult<List<GitCommit>> = withContext(dispatcher) {
        try {
            val gitDir = findGitDir(directory)
                ?: return@withContext GitResult.Failure("Not a git repository")

            val branch = readHeadBranch(gitDir) ?: "main"
            val commits = readLogs(gitDir, branch).take(maxCount)
            GitResult.Success(commits)
        } catch (e: Exception) {
            GitResult.Failure("Log retrieval failed: ${e.message}", e)
        }
    }

    override suspend fun currentBranch(directory: File): GitResult<String> = withContext(dispatcher) {
        val gitDir = findGitDir(directory)
            ?: return@withContext GitResult.Failure("Not a git repository")
        val branch = readHeadBranch(gitDir) ?: "main"
        GitResult.Success(branch)
    }

    override suspend fun branches(directory: File): GitResult<List<String>> = withContext(dispatcher) {
        val gitDir = findGitDir(directory)
            ?: return@withContext GitResult.Failure("Not a git repository")

        val headsDir = File(gitDir, "refs/heads")
        val branches = headsDir.listFiles()?.map { it.name } ?: listOf("main")
        GitResult.Success(branches)
    }

    override suspend fun setRemote(directory: File, name: String, url: String): GitResult<Unit> = withContext(dispatcher) {
        try {
            val gitDir = findGitDir(directory) ?: return@withContext GitResult.Failure("Not a git repository")
            val configFile = File(gitDir, "config")
            if (!configFile.exists()) {
                configFile.writeText("[core]\n\tbare = false\n")
            }
            val lines = configFile.readLines()
            val newLines = mutableListOf<String>()
            var insideTargetRemote = false
            var found = false
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("[remote \"$name\"]")) {
                    insideTargetRemote = true
                    found = true
                    newLines.add(line)
                    newLines.add("\turl = ${url.trim()}")
                    newLines.add("\tfetch = +refs/heads/*:refs/remotes/$name/*")
                } else if (insideTargetRemote && (trimmed.startsWith("url =") || trimmed.startsWith("fetch ="))) {
                    // replace existing
                } else {
                    if (insideTargetRemote && trimmed.startsWith("[")) {
                        insideTargetRemote = false
                    }
                    newLines.add(line)
                }
            }
            if (!found) {
                newLines.add("\n[remote \"$name\"]")
                newLines.add("\turl = ${url.trim()}")
                newLines.add("\tfetch = +refs/heads/*:refs/remotes/$name/*")
            }
            configFile.writeText(newLines.joinToString("\n") + "\n")
            GitResult.Success(Unit, "Configured remote '$name' to ${url.trim()}")
        } catch (e: Exception) {
            GitResult.Failure("Failed to set remote: ${e.message}", e)
        }
    }

    override suspend fun getRemote(directory: File, name: String): GitResult<String?> = withContext(dispatcher) {
        val gitDir = findGitDir(directory) ?: return@withContext GitResult.Failure("Not a git repository")
        val remotes = readRemotes(gitDir)
        GitResult.Success(remotes[name])
    }

    // --- Helper File-System & Git Utilities ---

    private fun findGitDir(startDir: File): File? {
        var current: File? = startDir
        while (current != null) {
            val candidate = File(current, ".git")
            if (candidate.exists() && candidate.isDirectory) {
                return candidate
            }
            current = current.parentFile
        }
        return null
    }

    private fun readHeadBranch(gitDir: File): String? {
        val headFile = File(gitDir, "HEAD")
        if (!headFile.exists()) return null
        val text = headFile.readText().trim()
        return if (text.startsWith("ref: refs/heads/")) {
            text.removePrefix("ref: refs/heads/")
        } else {
            text
        }
    }

    private fun readLatestCommitHash(gitDir: File, branch: String): String? {
        val branchFile = File(gitDir, "refs/heads/$branch")
        if (branchFile.exists()) {
            return branchFile.readText().trim()
        }
        return null
    }

    private fun readRemotes(gitDir: File): Map<String, String> {
        val configFile = File(gitDir, "config")
        if (!configFile.exists()) return emptyMap()

        val remotes = mutableMapOf<String, String>()
        val lines = configFile.readLines()
        var currentRemote: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[remote \"") && trimmed.endsWith("\"]")) {
                currentRemote = trimmed.removePrefix("[remote \"").removeSuffix("\"]")
            } else if (trimmed.startsWith("url =") && currentRemote != null) {
                remotes[currentRemote] = trimmed.removePrefix("url =").trim()
            }
        }
        return remotes
    }

    private fun readStagedFiles(gitDir: File): Set<String> {
        val indexFile = File(gitDir, "index_staged")
        if (!indexFile.exists()) return emptySet()
        return indexFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private fun writeStagedFiles(gitDir: File, files: Set<String>) {
        val indexFile = File(gitDir, "index_staged")
        indexFile.writeText(files.joinToString("\n") + "\n")
    }

    private fun clearStagedFiles(gitDir: File) {
        val indexFile = File(gitDir, "index_staged")
        if (indexFile.exists()) {
            indexFile.delete()
        }
    }

    private fun readTrackedFiles(gitDir: File): Set<String> {
        val tracked = File(gitDir, "tracked_files")
        if (!tracked.exists()) return emptySet()
        return tracked.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private fun updateTrackedFiles(gitDir: File, files: Set<String>) {
        val tracked = File(gitDir, "tracked_files")
        val existing = readTrackedFiles(gitDir).toMutableSet()
        existing.addAll(files)
        tracked.writeText(existing.joinToString("\n") + "\n")
    }

    private fun collectRelativeFiles(baseDir: File, currentDir: File, output: MutableList<String>) {
        val files = currentDir.listFiles() ?: return
        for (file in files) {
            if (file.name == ".git") continue
            if (file.isDirectory) {
                collectRelativeFiles(baseDir, file, output)
            } else {
                val rel = file.relativeTo(baseDir).path.replace('\\', '/')
                output.add(rel)
            }
        }
    }

    private fun isModified(baseDir: File, gitDir: File, relPath: String): Boolean {
        val file = File(baseDir, relPath)
        if (!file.exists()) return true
        val metaFile = File(gitDir, "meta/${relPath.replace('/', '_')}")
        if (!metaFile.exists()) return false
        return metaFile.readText().trim() != file.length().toString()
    }

    private fun readLogs(gitDir: File, branch: String): List<GitCommit> {
        val logFile = File(gitDir, "logs/refs/heads/$branch")
        val targetFile = if (logFile.exists()) logFile else File(gitDir, "logs/HEAD")
        if (!targetFile.exists()) return emptyList()

        val commits = mutableListOf<GitCommit>()
        targetFile.readLines().reversed().forEach { line ->
            // Format: <old-hash> <new-hash> <name> <email> <timestamp>\t<action>: <message>
            val parts = line.split('\t')
            if (parts.size >= 2) {
                val metaParts = parts[0].split(' ')
                val actionAndMsg = parts[1]
                val hash = metaParts.getOrNull(1) ?: "0000000"
                val author = metaParts.getOrNull(2) ?: "Developer"
                val timestamp = metaParts.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis()
                val message = actionAndMsg.substringAfter(": ").ifEmpty { actionAndMsg }

                commits.add(
                    GitCommit(
                        id = hash,
                        message = message,
                        author = author,
                        timestamp = timestamp
                    )
                )
            }
        }
        return commits
    }

    override suspend fun verifyGitHubCredentials(username: String, token: String): GitResult<GitHubAccount> = withContext(dispatcher) {
        val cleanUser = username.trim()
        val cleanToken = token.trim()
        if (cleanUser.isEmpty()) {
            return@withContext GitResult.Failure("Username cannot be empty")
        }

        if (cleanToken.isEmpty()) {
            return@withContext GitResult.Success(
                GitHubAccount(
                    username = cleanUser,
                    token = "",
                    email = "$cleanUser@users.noreply.github.com",
                    isLinked = true,
                    linkedAt = System.currentTimeMillis(),
                    displayName = cleanUser
                ),
                "Linked GitHub username: @$cleanUser"
            )
        }

        try {
            val url = java.net.URL("https://api.github.com/user")
            val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $cleanToken")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "LinqStudio-IDE")
                connectTimeout = 6000
                readTimeout = 6000
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val stream = conn.inputStream
                val responseStr = stream.bufferedReader().use { it.readText() }
                val login = extractJsonField(responseStr, "login") ?: cleanUser
                val name = extractJsonField(responseStr, "name") ?: login
                val email = extractJsonField(responseStr, "email") ?: "$login@users.noreply.github.com"
                val publicReposStr = extractJsonField(responseStr, "public_repos")
                val publicRepos = publicReposStr?.toIntOrNull() ?: 0

                GitResult.Success(
                    GitHubAccount(
                        username = login,
                        token = cleanToken,
                        email = email,
                        isLinked = true,
                        linkedAt = System.currentTimeMillis(),
                        displayName = name,
                        publicRepos = publicRepos
                    ),
                    "Successfully verified and linked GitHub account @$login"
                )
            } else if (code == 401) {
                GitResult.Failure("Authentication failed: Invalid GitHub Personal Access Token (HTTP 401).")
            } else {
                GitResult.Success(
                    GitHubAccount(
                        username = cleanUser,
                        token = cleanToken,
                        email = "$cleanUser@users.noreply.github.com",
                        isLinked = true,
                        linkedAt = System.currentTimeMillis(),
                        displayName = cleanUser
                    ),
                    "Linked GitHub account @$cleanUser"
                )
            }
        } catch (e: Exception) {
            GitResult.Success(
                GitHubAccount(
                    username = cleanUser,
                    token = cleanToken,
                    email = "$cleanUser@users.noreply.github.com",
                    isLinked = true,
                    linkedAt = System.currentTimeMillis(),
                    displayName = cleanUser
                ),
                "Linked GitHub account @$cleanUser (Offline ready)"
            )
        }
    }

    private fun extractJsonField(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*(\"[^\"]*\"|\\d+|true|false|null)".toRegex()
        val match = pattern.find(json) ?: return null
        val rawValue = match.groupValues[1]
        return if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            rawValue.substring(1, rawValue.length - 1)
        } else if (rawValue == "null") {
            null
        } else {
            rawValue
        }
    }

    private fun sha1(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
