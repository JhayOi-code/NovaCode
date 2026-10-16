package com.example

import com.example.linter.CodeLinter
import com.example.linter.LintSeverity
import com.example.runner.CodeExecutionEngine
import com.example.templates.CodeTemplates
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LinterAndTemplatesTest {

    @Test
    fun testJavaScriptLinterDetectsUnmatchedBrackets() {
        val brokenJs = """
            function calculate() {
                const x = (10 + 20;
                return x;
            }
        """.trimIndent()

        val issues = CodeLinter.lint(brokenJs, "JavaScript")
        assertTrue(issues.isNotEmpty())
        assertTrue(issues.any { it.message.contains("bracket", ignoreCase = true) || it.message.contains("closing", ignoreCase = true) })
    }

    @Test
    fun testJavaScriptLinterDetectsUnclosedString() {
        val brokenJs = """
            const greeting = "Hello World;
            console.log(greeting);
        """.trimIndent()

        val issues = CodeLinter.lint(brokenJs, "JavaScript")
        assertTrue(issues.isNotEmpty())
        assertTrue(issues.any { it.message.contains("Unterminated", ignoreCase = true) })
    }

    @Test
    fun testCssLinterDetectsMissingColonAndUnclosedBlock() {
        val brokenCss = """
            .card {
                background red;
                padding: 12px;
        """.trimIndent()

        val issues = CodeLinter.lint(brokenCss, "CSS")
        assertTrue(issues.isNotEmpty())
        assertTrue(issues.any { it.message.contains("colon", ignoreCase = true) || it.message.contains("Unclosed", ignoreCase = true) })
    }

    @Test
    fun testJsonLinterDetectsUnquotedKeys() {
        val badJson = """
            {
               name: "John"
            }
        """.trimIndent()

        val issues = CodeLinter.lint(badJson, "JSON")
        assertTrue(issues.isNotEmpty())
        assertTrue(issues.any { it.message.contains("double quotes", ignoreCase = true) })
    }

    @Test
    fun testTemplatesRepositoryContainsRequiredBoilerplates() {
        val templates = CodeTemplates.allTemplates
        assertTrue(templates.isNotEmpty())

        val html5 = templates.find { it.id == "html5_starter" }
        assertNotNull(html5)
        assertTrue(html5!!.snippet.contains("<!DOCTYPE html>"))

        val react = templates.find { it.id == "react_cdn" }
        assertNotNull(react)
        assertTrue(react!!.snippet.contains("React"))

        val tailwind = templates.find { it.id == "tailwind_starter" }
        assertNotNull(tailwind)
        assertTrue(tailwind!!.snippet.contains("tailwindcss"))

        val categories = CodeTemplates.categories
        assertTrue(categories.contains("Web & Frontend"))
        assertTrue(categories.contains("JavaScript"))
        assertTrue(categories.contains("Python"))
    }

    @Test
    fun testMultiLanguageExecutionEngine() {
        // Rust execution
        val rustCode = """
            fn main() {
                println!("Hello from Rust runner!");
            }
        """.trimIndent()
        val rustResult = CodeExecutionEngine.execute(rustCode, "main.rs")
        assertTrue(rustResult.isSuccess)
        assertTrue(rustResult.stdout.contains("Hello from Rust runner!"))

        // Go execution
        val goCode = """
            package main
            import "fmt"
            func main() {
                fmt.Println("Hello from Go runtime!")
            }
        """.trimIndent()
        val goResult = CodeExecutionEngine.execute(goCode, "main.go")
        assertTrue(goResult.isSuccess)
        assertTrue(goResult.stdout.contains("Hello from Go runtime!"))

        // SQL execution
        val sqlCode = """
            CREATE TABLE items (id INT, name TEXT);
            SELECT * FROM items;
        """.trimIndent()
        val sqlResult = CodeExecutionEngine.execute(sqlCode, "query.sql")
        assertTrue(sqlResult.isSuccess)
        assertTrue(sqlResult.stdout.contains("Table created: items"))
        assertTrue(sqlResult.stdout.contains("Query: SELECT * FROM items"))

        // PHP execution
        val phpCode = """
            echo "Hello from PHP CLI!";
        """.trimIndent()
        val phpResult = CodeExecutionEngine.execute(phpCode, "index.php")
        assertTrue(phpResult.isSuccess)
        assertTrue(phpResult.stdout.contains("Hello from PHP CLI!"))

        // C++ execution
        val cppCode = """
            #include <iostream>
            int main() {
                std::cout << "C++ Output Line" << std::endl;
                return 0;
            }
        """.trimIndent()
        val cppResult = CodeExecutionEngine.execute(cppCode, "main.cpp")
        assertTrue(cppResult.isSuccess)
        assertTrue(cppResult.stdout.contains("C++ Output Line"))
    }
}
