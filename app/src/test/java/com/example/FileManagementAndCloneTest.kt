package com.example

import com.example.git.GitServiceImpl
import com.example.git.model.GitCredentials
import com.example.git.model.GitResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileManagementAndCloneTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testMoveFileOperation() {
        val rootDir = tempFolder.newFolder("workspace")
        val sourceFile = File(rootDir, "source.txt").apply { writeText("Hello Linq Studio") }
        val targetDir = File(rootDir, "subfolder").apply { mkdirs() }

        assertTrue(sourceFile.exists())
        val destFile = File(targetDir, "source.txt")

        val moved = sourceFile.renameTo(destFile)
        assertTrue(moved)
        assertFalse(sourceFile.exists())
        assertTrue(destFile.exists())
        assertEquals("Hello Linq Studio", destFile.readText())
    }

    @Test
    fun testMoveDirectoryOperation() {
        val rootDir = tempFolder.newFolder("workspace_dir")
        val sourceDir = File(rootDir, "source_dir").apply { mkdirs() }
        File(sourceDir, "child.txt").writeText("Child Content")
        val targetParent = File(rootDir, "target_parent").apply { mkdirs() }

        val destDir = File(targetParent, "source_dir")
        val moved = sourceDir.renameTo(destDir)
        assertTrue(moved)
        assertFalse(sourceDir.exists())
        assertTrue(destDir.exists())
        assertTrue(File(destDir, "child.txt").exists())
        assertEquals("Child Content", File(destDir, "child.txt").readText())
    }

    @Test
    fun testGitCloneOfflineFallbackInitialization() = runBlocking {
        val gitService = GitServiceImpl()
        val targetDir = File(tempFolder.newFolder(), "ClonedRepo")

        // Test with a repository URL
        val result = gitService.clone(
            repoUrl = "https://github.com/example/test-repo.git",
            targetDirectory = targetDir,
            credentials = GitCredentials(username = "testuser", tokenOrPassword = "ghp_mocktoken"),
            branch = "main"
        )

        assertTrue(result is GitResult.Success)
        val repo = (result as GitResult.Success).data
        assertEquals("main", repo.branch)
        assertTrue(File(targetDir, ".git").exists())
        assertTrue(File(targetDir, ".git/config").exists())
        assertTrue(File(targetDir, ".git/config").readText().contains("https://github.com/example/test-repo.git"))
        assertTrue(File(targetDir, "README.md").exists())
    }

    @Test
    fun testGitCloneCustomCodeProviderSupport() = runBlocking {
        val gitService = GitServiceImpl()
        val targetDir = File(tempFolder.newFolder(), "GitLabRepo")

        // Test with a GitLab repository URL
        val result = gitService.clone(
            repoUrl = "https://gitlab.com/group/project.git",
            targetDirectory = targetDir,
            branch = "main"
        )

        assertTrue(result is GitResult.Success)
        val configText = File(targetDir, ".git/config").readText()
        assertTrue(configText.contains("https://gitlab.com/group/project.git"))
    }
}
