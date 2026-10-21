package com.example.git

import com.example.model.CodeFile
import com.example.model.GitCommit
import com.example.model.GitFileChange
import com.example.model.GitFileStatus
import com.example.model.GitRepositoryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class GitManager {

    private val _repoStatus = MutableStateFlow(
        GitRepositoryStatus(
            currentBranch = "main",
            availableBranches = listOf("main", "develop", "feature/mobile-ide"),
            commits = listOf(
                GitCommit(
                    id = "a1b2c3d",
                    message = "Initial project setup with starter guides",
                    author = "linq_dev",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    filesChangedCount = 5
                )
            )
        )
    )
    val repoStatus: StateFlow<GitRepositoryStatus> = _repoStatus.asStateFlow()

    fun updateFromFiles(files: List<CodeFile>) {
        val currentChanges = _repoStatus.value.changes.associateBy { it.filePath }
        val newChanges = mutableListOf<GitFileChange>()

        for (file in files) {
            if (file.isModified || file.content != file.originalContent) {
                val existing = currentChanges[file.path]
                newChanges.add(
                    GitFileChange(
                        filePath = file.path,
                        fileName = file.name,
                        status = GitFileStatus.MODIFIED,
                        isStaged = existing?.isStaged ?: false,
                        additions = 3,
                        deletions = 1
                    )
                )
            }
        }

        _repoStatus.value = _repoStatus.value.copy(
            changes = newChanges,
            isClean = newChanges.isEmpty()
        )
    }

    fun stageFile(filePath: String) {
        val updated = _repoStatus.value.changes.map {
            if (it.filePath == filePath) it.copy(isStaged = true) else it
        }
        _repoStatus.value = _repoStatus.value.copy(changes = updated)
    }

    fun unstageFile(filePath: String) {
        val updated = _repoStatus.value.changes.map {
            if (it.filePath == filePath) it.copy(isStaged = false) else it
        }
        _repoStatus.value = _repoStatus.value.copy(changes = updated)
    }

    fun stageAll() {
        val updated = _repoStatus.value.changes.map { it.copy(isStaged = true) }
        _repoStatus.value = _repoStatus.value.copy(changes = updated)
    }

    fun unstageAll() {
        val updated = _repoStatus.value.changes.map { it.copy(isStaged = false) }
        _repoStatus.value = _repoStatus.value.copy(changes = updated)
    }

    fun commit(message: String): Boolean {
        if (message.isBlank()) return false
        val staged = _repoStatus.value.changes.filter { it.isStaged }
        if (staged.isEmpty()) return false

        val newCommit = GitCommit(
            id = UUID.randomUUID().toString().substring(0, 7),
            message = message.trim(),
            author = "linq_dev",
            timestamp = System.currentTimeMillis(),
            filesChangedCount = staged.size
        )

        val remainingChanges = _repoStatus.value.changes.filter { !it.isStaged }
        _repoStatus.value = _repoStatus.value.copy(
            changes = remainingChanges,
            commits = listOf(newCommit) + _repoStatus.value.commits,
            isClean = remainingChanges.isEmpty(),
            aheadCount = _repoStatus.value.aheadCount + 1
        )
        return true
    }

    fun push(): String {
        val ahead = _repoStatus.value.aheadCount
        if (ahead == 0) return "Everything up-to-date"
        _repoStatus.value = _repoStatus.value.copy(aheadCount = 0)
        return "Successfully pushed $ahead commit(s) to origin/${_repoStatus.value.currentBranch}"
    }

    fun createBranch(name: String) {
        val cleanName = name.trim().replace(" ", "-")
        if (cleanName.isNotEmpty() && !_repoStatus.value.availableBranches.contains(cleanName)) {
            _repoStatus.value = _repoStatus.value.copy(
                availableBranches = _repoStatus.value.availableBranches + cleanName,
                currentBranch = cleanName
            )
        }
    }

    fun switchBranch(name: String) {
        if (_repoStatus.value.availableBranches.contains(name)) {
            _repoStatus.value = _repoStatus.value.copy(currentBranch = name)
        }
    }
}
