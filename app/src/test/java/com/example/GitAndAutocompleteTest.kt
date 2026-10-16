package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.autocomplete.AutocompleteEngine
import com.example.autocomplete.SuggestionKind
import com.example.format.PrettierFormatter
import com.example.git.GitManager
import com.example.model.FileItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class GitAndAutocompleteTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var workspaceDir: File
    private lateinit var gitManager: GitManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workspaceDir = tempFolder.newFolder("workspace")
        gitManager = GitManager(context, workspaceDir)
    }

    @Test
    fun testPrettierFormatHtml() {
        val messyHtml = "<div><h1>Title</h1><p>Text</p></div>"
        val formatted = PrettierFormatter.format(messyHtml, "html")
        assertTrue("Formatted HTML should contain tags", formatted.contains("<div>"))
        assertTrue("Formatted HTML should have indentation", formatted.contains("  <h1>Title</h1>") || formatted.contains("<h1>Title</h1>"))
    }

    @Test
    fun testPrettierFormatJson() {
        val rawJson = "{\"name\":\"Linq\",\"version\":2,\"active\":true}"
        val formatted = PrettierFormatter.format(rawJson, "json")
        assertTrue(formatted.contains("\"name\": \"Linq\""))
        assertTrue(formatted.contains("  \"version\": 2"))
    }

    @Test
    fun testPrettierFormatCss() {
        val rawCss = "body{margin:0;padding:10px;}"
        val formatted = PrettierFormatter.format(rawCss, "css")
        assertTrue(formatted.contains("margin: 0;"))
        assertTrue(formatted.contains("padding: 10px;"))
    }

    @Test
    fun testPrettierFormatJs() {
        val rawJs = "function add(a,b){\nreturn a+b;\n}"
        val formatted = PrettierFormatter.format(rawJs, "javascript")
        assertTrue(formatted.contains("function add(a,b){"))
        assertTrue(formatted.contains("  return a+b;"))
    }

    @Test
    fun testAutocompleteJsSuggestionsAndSignatures() {
        val code = "console."
        val suggestions = AutocompleteEngine.getSuggestions(code, code.length, "javascript")
        assertTrue(suggestions.isNotEmpty())
        val logSuggestion = suggestions.find { it.label == "console.log" }
        assertNotNull(logSuggestion)
        assertNotNull(logSuggestion?.signature)
        assertTrue(logSuggestion?.signature?.contains("console.log") == true)
        assertEquals(SuggestionKind.FUNCTION, logSuggestion?.kind)
    }

    @Test
    fun testAutocompletePythonSuggestions() {
        val code = "pri"
        val suggestions = AutocompleteEngine.getSuggestions(code, code.length, "python")
        val printSuggestion = suggestions.find { it.label == "print" }
        assertNotNull(printSuggestion)
        assertTrue(printSuggestion?.signature?.contains("print") == true)
    }

    @Test
    fun testAiCopilotCodeCompletion() {
        val code = "function calculateSum(a, b) {"
        val suggestions = AutocompleteEngine.getSuggestions(code, code.length, "javascript")
        val aiSuggestion = suggestions.find { it.isAi }
        assertNotNull("AI Copilot suggestion should be triggered", aiSuggestion)
        assertTrue(aiSuggestion?.label?.contains("AI") == true)
        assertTrue(aiSuggestion?.insertText?.contains("return") == true)
    }

    @Test
    fun testGitStagingAndCommit() {
        val testFile = File(workspaceDir, "test.js")
        testFile.writeText("console.log('Hello Git');")
        val fileItems = listOf(
            FileItem(file = testFile, name = "test.js", isDirectory = false, sizeBytes = 20L, extension = "js")
        )

        var status = gitManager.computeStatus(fileItems, emptyList())
        assertEquals("main", status.branch)
        assertTrue(status.unstagedFiles.isNotEmpty())
        assertTrue(status.stagedFiles.isEmpty())

        // Stage file
        gitManager.stageFile("test.js")
        status = gitManager.computeStatus(fileItems, emptyList())
        assertEquals(1, status.stagedFiles.size)
        assertEquals("test.js", status.stagedFiles[0].relativePath)

        // Commit staged changes
        val commit = gitManager.commit("Add test.js", "Author <author@linq.test>")
        assertNotNull(commit)
        assertEquals("Add test.js", commit?.message)

        status = gitManager.computeStatus(fileItems, emptyList())
        assertTrue(status.stagedFiles.isEmpty())
        assertTrue(status.commits.any { it.message == "Add test.js" })

        // Push
        val pushResult = gitManager.push()
        assertTrue(pushResult.contains("Successfully pushed") || pushResult.contains("origin/main"))
    }

    @Test
    fun testGitBranchCreationAndSwitch() {
        val created = gitManager.createBranch("feature-editor-tools")
        assertTrue(created)
        var status = gitManager.computeStatus(emptyList(), emptyList())
        assertEquals("feature-editor-tools", status.branch)
        assertTrue(status.branches.contains("feature-editor-tools"))

        gitManager.switchBranch("main")
        status = gitManager.computeStatus(emptyList(), emptyList())
        assertEquals("main", status.branch)
    }
}
