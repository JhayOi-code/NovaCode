package com.example

import com.example.runner.CodeExecutionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CodeExecutionEngineTest {

    @Test
    fun testDetectLanguage() {
        assertEquals("Python", CodeExecutionEngine.detectLanguage("main.py"))
        assertEquals("JavaScript", CodeExecutionEngine.detectLanguage("app.js"))
        assertEquals("TypeScript", CodeExecutionEngine.detectLanguage("index.ts"))
        assertEquals("Shell / Bash", CodeExecutionEngine.detectLanguage("deploy.sh"))
        assertEquals("Kotlin", CodeExecutionEngine.detectLanguage("Main.kt"))
        assertEquals("HTML / Web", CodeExecutionEngine.detectLanguage("index.html"))
        assertEquals("JSON", CodeExecutionEngine.detectLanguage("package.json"))
    }

    @Test
    fun testPythonExecution() {
        val pythonCode = """
x = 10
y = 20
total = x + y
print("Total is:", total)
for i in range(3):
    print("Step", i)
"""
        val result = CodeExecutionEngine.execute(pythonCode, "test.py")
        assertTrue(result.isSuccess)
        assertEquals(0, result.exitCode)
        assertTrue(result.stdout.contains("Total is: 30"))
        assertTrue(result.stdout.contains("Step 0"))
        assertTrue(result.stdout.contains("Step 1"))
        assertTrue(result.stdout.contains("Step 2"))
    }

    @Test
    fun testPythonFunctionsAndConditions() {
        val pythonCode = """
def greet(name):
    print("Welcome", name)

greet("Developer")
val = 42
if val > 40:
    print("Value is high")
else:
    print("Value is low")
"""
        val result = CodeExecutionEngine.execute(pythonCode, "func_test.py")
        assertTrue(result.isSuccess)
        assertTrue(result.stdout.contains("Welcome Developer"))
        assertTrue(result.stdout.contains("Value is high"))
    }

    @Test
    fun testJavaScriptExecution() {
        val jsCode = """
console.log("Starting JavaScript runner...");
const a = 15;
const b = 5;
console.log("Sum: " + (a + b));
"""
        val result = CodeExecutionEngine.execute(jsCode, "test.js")
        assertTrue(result.isSuccess)
        assertEquals(0, result.exitCode)
        assertTrue(result.stdout.contains("Starting JavaScript runner..."))
        assertTrue(result.stdout.contains("Sum: 20"))
    }

    @Test
    fun testShellExecution() {
        val shellCode = """
echo "Running automation script"
echo "Hello from shell"
"""
        val result = CodeExecutionEngine.execute(shellCode, "test.sh")
        assertTrue(result.isSuccess)
        assertEquals(0, result.exitCode)
        assertTrue(result.stdout.contains("Running automation script"))
        assertTrue(result.stdout.contains("Hello from shell"))
    }

    @Test
    fun testJsonValidation() {
        val validJson = """{"name": "Linq Studio", "version": 2.0}"""
        val result = CodeExecutionEngine.execute(validJson, "data.json")
        assertTrue(result.isSuccess)
        assertTrue(result.stdout.contains("JSON Valid"))

        val invalidJson = """{broken json}"""
        val badResult = CodeExecutionEngine.execute(invalidJson, "bad.json")
        assertFalse(badResult.isSuccess)
    }
}
