package com.example.git

import android.content.Context
import com.example.model.CodeFile
import com.example.model.FileItem
import com.example.model.GitCommit
import com.example.model.GitFileItem
import com.example.model.GitFileStatus
import com.example.model.GitStatus
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class GitManager(private val context: Context, private val workspaceDir: File) {

    private val gitStoreFile = File(workspaceDir, ".linq_git.json")
    private val stagedPaths = mutableSetOf<String>()
    private var currentBranch: String = "main"
    private val branches = mutableListOf("main", "feature/workspace", "dev")
    private val commits = mutableListOf<GitCommit>()
    private var lastPushMessage: String? = "Everything up to date with origin/main"

    init {
        loadGitStore()
    }

    private fun loadGitStore() {
        if (!gitStoreFile.exists()) {
            // Seed initial commit
            commits.add(
                GitCommit(
                    id = "init89a",
                    message = "Initial workspace setup with boilerplate templates",
                    author = "Developer <dev@linqstudio.local>",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    fileCount = 3,
                    branch = "main"
                )
            )
            saveGitStore()
            return
        }

        try {
            val jsonStr = gitStoreFile.readText()
            val json = JSONObject(jsonStr)
            currentBranch = json.optString("branch", "main")
            
            val branchArr = json.optJSONArray("branches")
            if (branchArr != null) {
                branches.clear()
                for (i in 0 until branchArr.length()) {
                    branches.add(branchArr.getString(i))
                }
            }
            if (!branches.contains("main")) branches.add(0, "main")

            val stagedArr = json.optJSONArray("staged")
            if (stagedArr != null) {
                stagedPaths.clear()
                for (i in 0 until stagedArr.length()) {
                    stagedPaths.add(stagedArr.getString(i))
                }
            }

            val commitArr = json.optJSONArray("commits")
            if (commitArr != null) {
                commits.clear()
                for (i in 0 until commitArr.length()) {
                    val cObj = commitArr.getJSONObject(i)
                    commits.add(
                        GitCommit(
                            id = cObj.optString("id", UUID.randomUUID().toString().take(7)),
                            message = cObj.optString("message", "Commit"),
                            author = cObj.optString("author", "Developer"),
                            timestamp = cObj.optLong("timestamp", System.currentTimeMillis()),
                            fileCount = cObj.optInt("fileCount", 1),
                            branch = cObj.optString("branch", "main")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // fallback
        }
    }

    private fun saveGitStore() {
        try {
            val json = JSONObject()
            json.put("branch", currentBranch)
            val branchArr = JSONArray()
            branches.forEach { branchArr.put(it) }
            json.put("branches", branchArr)

            val stagedArr = JSONArray()
            stagedPaths.forEach { stagedArr.put(it) }
            json.put("staged", stagedArr)

            val commitArr = JSONArray()
            commits.forEach { commit ->
                val cObj = JSONObject()
                cObj.put("id", commit.id)
                cObj.put("message", commit.message)
                cObj.put("author", commit.author)
                cObj.put("timestamp", commit.timestamp)
                cObj.put("fileCount", commit.fileCount)
                cObj.put("branch", commit.branch)
                commitArr.put(cObj)
            }
            json.put("commits", commitArr)

            gitStoreFile.writeText(json.toString(2))
        } catch (_: Exception) {
            // ignore
        }
    }

    fun computeStatus(allFiles: List<FileItem>, openFiles: List<CodeFile>): GitStatus {
        val staged = mutableListOf<GitFileItem>()
        val unstaged = mutableListOf<GitFileItem>()

        for (item in allFiles) {
            if (item.isDirectory || item.name.startsWith(".")) continue

            val relPath = item.file.relativeToOrNull(workspaceDir)?.path ?: item.name
            val isOpenModified = openFiles.any { it.file.absolutePath == item.file.absolutePath && it.isModified }

            if (stagedPaths.contains(relPath)) {
                staged.add(
                    GitFileItem(
                        file = item.file,
                        relativePath = relPath,
                        status = GitFileStatus.STAGED
                    )
                )
            } else {
                // If it's modified in memory or modified recently
                val status = if (isOpenModified) {
                    GitFileStatus.MODIFIED
                } else if (item.file.lastModified() > (commits.firstOrNull()?.timestamp ?: 0L)) {
                    GitFileStatus.MODIFIED
                } else {
                    GitFileStatus.UNTRACKED
                }
                unstaged.add(
                    GitFileItem(
                        file = item.file,
                        relativePath = relPath,
                        status = status
                    )
                )
            }
        }

        return GitStatus(
            branch = currentBranch,
            branches = branches.toList(),
            stagedFiles = staged,
            unstagedFiles = unstaged,
            commits = commits.sortedByDescending { it.timestamp },
            isClean = staged.isEmpty() && unstaged.isEmpty(),
            lastPushStatus = lastPushMessage
        )
    }

    fun stageFile(relPath: String) {
        stagedPaths.add(relPath)
        saveGitStore()
    }

    fun stageAll(unstaged: List<GitFileItem>) {
        unstaged.forEach { stagedPaths.add(it.relativePath) }
        saveGitStore()
    }

    fun unstageFile(relPath: String) {
        stagedPaths.remove(relPath)
        saveGitStore()
    }

    fun unstageAll() {
        stagedPaths.clear()
        saveGitStore()
    }

    fun commit(message: String, author: String): GitCommit? {
        val trimmed = message.trim()
        if (trimmed.isEmpty() || stagedPaths.isEmpty()) return null

        val commitId = UUID.randomUUID().toString().replace("-", "").take(7)
        val commit = GitCommit(
            id = commitId,
            message = trimmed,
            author = author,
            timestamp = System.currentTimeMillis(),
            fileCount = stagedPaths.size,
            branch = currentBranch
        )

        commits.add(0, commit)
        stagedPaths.clear()
        lastPushMessage = "1 local commit ahead of origin/$currentBranch. Ready to push."
        saveGitStore()
        return commit
    }

    fun push(): String {
        val count = commits.take(3).size
        val hash = commits.firstOrNull()?.id ?: "HEAD"
        lastPushMessage = "✓ Successfully pushed $count commit(s) [${hash}] to origin/$currentBranch"
        return lastPushMessage ?: "Pushed to origin/$currentBranch"
    }

    fun createBranch(name: String): Boolean {
        val clean = name.trim().replace("\\s+".toRegex(), "-")
        if (clean.isNotEmpty() && !branches.contains(clean)) {
            branches.add(clean)
            currentBranch = clean
            saveGitStore()
            return true
        }
        return false
    }

    fun switchBranch(name: String) {
        if (branches.contains(name)) {
            currentBranch = name
            saveGitStore()
        }
    }
}
