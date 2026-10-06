package com.example.git

import com.example.git.model.GitAuthor
import com.example.git.model.GitCredentials
import com.example.git.model.GitResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class GitServiceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val gitService = GitServiceImpl(testDispatcher)

    @Test
    fun testInitRepository() = runTest(testDispatcher) {
        val repoDir = tempFolder.newFolder("test_repo")
        val result = gitService.init(repoDir, "main")

        assertTrue(result is GitResult.Success)
        val repo = (result as GitResult.Success).data
        assertEquals("main", repo.branch)
        assertEquals(repoDir, repo.rootDir)

        val gitDir = File(repoDir, ".git")
        assertTrue(gitDir.exists())
        assertTrue(File(gitDir, "HEAD").exists())
        assertEquals("ref: refs/heads/main", File(gitDir, "HEAD").readText().trim())
    }

    @Test
    fun testCloneRepository() = runTest(testDispatcher) {
        val targetDir = tempFolder.newFolder("cloned_repo")
        val repoUrl = "https://github.com/linqstudio/sample-project.git"

        val result = gitService.clone(
            repoUrl = repoUrl,
            targetDirectory = targetDir,
            credentials = GitCredentials(username = "linqdev", tokenOrPassword = "secret_token")
        )

        assertTrue(result is GitResult.Success)
        val repo = (result as GitResult.Success).data
        assertEquals("main", repo.branch)
        assertEquals(repoUrl, repo.remotes["origin"])

        // README and .gitignore should be created
        assertTrue(File(targetDir, "README.md").exists())
        assertTrue(File(targetDir, ".gitignore").exists())

        // Remote tracking branch should be configured
        val remoteRef = File(targetDir, ".git/refs/remotes/origin/main")
        assertTrue(remoteRef.exists())
    }

    @Test
    fun testAddAndCommitCycle() = runTest(testDispatcher) {
        val repoDir = tempFolder.newFolder("commit_repo")
        gitService.init(repoDir, "main")

        // 1. Create a file
        val file1 = File(repoDir, "Main.kt")
        file1.writeText("fun main() { println(\"Hello Git!\") }")

        // Check status has untracked
        val status1 = gitService.status(repoDir)
        assertTrue(status1 is GitResult.Success)
        val statusData1 = (status1 as GitResult.Success).data
        assertTrue(statusData1.untrackedFiles.contains("Main.kt"))

        // 2. Add file
        val addResult = gitService.add(repoDir, "Main.kt")
        assertTrue(addResult is GitResult.Success)

        val status2 = (gitService.status(repoDir) as GitResult.Success).data
        assertTrue(status2.stagedFiles.contains("Main.kt"))
        assertFalse(status2.untrackedFiles.contains("Main.kt"))

        // 3. Commit
        val commitResult = gitService.commit(
            directory = repoDir,
            message = "feat: initial commit with Main.kt",
            author = GitAuthor("Alice", "alice@example.com")
        )

        assertTrue(commitResult is GitResult.Success)
        val commit = (commitResult as GitResult.Success).data
        assertEquals("feat: initial commit with Main.kt", commit.message)
        assertEquals("Alice", commit.author)
        assertNotNull(commit.id)
        assertEquals(40, commit.id.length)

        // 4. Status should now be clean
        val status3 = (gitService.status(repoDir) as GitResult.Success).data
        assertTrue(status3.isClean)

        // 5. Log should contain commit
        val logResult = gitService.log(repoDir)
        assertTrue(logResult is GitResult.Success)
        val log = (logResult as GitResult.Success).data
        assertEquals(1, log.size)
        assertEquals(commit.id, log[0].id)
    }

    @Test
    fun testPushLifecycle() = runTest(testDispatcher) {
        val repoDir = tempFolder.newFolder("push_repo")
        gitService.clone("https://github.com/example/app.git", repoDir)

        // Add second file & commit
        File(repoDir, "Feature.kt").writeText("class Feature")
        gitService.add(repoDir, ".")
        gitService.commit(repoDir, "Add Feature.kt", GitAuthor("Bob", "bob@example.com"))

        // Push
        val pushResult = gitService.push(repoDir, "origin", "main")
        assertTrue(pushResult is GitResult.Success)
        val pushData = (pushResult as GitResult.Success).data
        assertTrue(pushData.success)
        assertEquals("origin", pushData.remote)
        assertEquals("main", pushData.branch)
        assertTrue(pushData.commitsCount >= 1)
    }
}
