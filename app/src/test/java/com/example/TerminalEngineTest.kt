package com.example

import com.example.model.CodeFile
import com.example.model.SupportedLanguage
import com.example.model.TerminalLineType
import com.example.terminal.TerminalEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TerminalEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testPythonScriptExecution() {
        val workspace = tempFolder.newFolder("workspace")
        val engine = TerminalEngine(workspace)

        val pyFile = CodeFile(
            name = "calc.py",
            path = File(workspace, "calc.py").absolutePath,
            content = "print(\"Hello Terminal!\")\nprint(\"Answer: 42\")",
            language = SupportedLanguage.PYTHON
        )

        val output = engine.executeCode(pyFile)
        assertTrue(output.isNotEmpty())
        assertEquals(TerminalLineType.COMMAND, output.first().type)
        assertTrue(output.any { it.text == "Hello Terminal!" })
        assertTrue(output.any { it.text == "Answer: 42" })
        assertEquals(TerminalLineType.SUCCESS, output.last().type)
    }

    @Test
    fun testBuiltinCommands() = runTest {
        val workspace = tempFolder.newFolder("workspace")
        File(workspace, "hello.txt").writeText("Sample content")
        val engine = TerminalEngine(workspace)

        val helpOutput = engine.executeCommand("help", workspace)
        assertTrue(helpOutput.any { it.text.contains("built-in commands") })

        val pwdOutput = engine.executeCommand("pwd", workspace)
        assertTrue(pwdOutput.any { it.text == workspace.absolutePath })

        val catOutput = engine.executeCommand("cat hello.txt", workspace)
        assertTrue(catOutput.any { it.text == "Sample content" })
    }

    @Test
    fun testGitCommandsInTerminal() = runTest {
        val workspace = tempFolder.newFolder("git_terminal_repo")
        val engine = TerminalEngine(workspace)

        val initOut = engine.executeCommand("git init", workspace)
        assertTrue(initOut.any { it.text.contains("Initialized empty Git repository") })

        val statusOut = engine.executeCommand("git status", workspace)
        assertTrue(statusOut.any { it.text.contains("On branch") })

        File(workspace, "code.py").writeText("print(123)")
        engine.executeCommand("git add .", workspace)
        val commitOut = engine.executeCommand("git commit -m \"first\"", workspace)
        assertTrue(commitOut.any { it.text.contains("first") })
    }
}
