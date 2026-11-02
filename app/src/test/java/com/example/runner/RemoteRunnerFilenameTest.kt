package com.example.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteRunnerFilenameTest {

    @Test
    fun testJavaSourceParser_matchingPublicClass() {
        val code = """
            package demo;
            public class MyProgram {
                public static void main(String[] args) {
                    System.out.println("Hello");
                }
            }
        """.trimIndent()
        val info = JavaSourceParser.resolveJavaFileInfo(code, "MyProgram.java")
        assertEquals("MyProgram.java", info.targetFileName)
        assertEquals("MyProgram", info.className)
    }

    @Test
    fun testJavaSourceParser_mismatchingPublicClass() {
        val code = """
            public class SolutionAlgorithm {
                public static void main(String[] args) {
                    System.out.println("Result: 42");
                }
            }
        """.trimIndent()
        // User's tab is named 'random_file_123.java'
        val info = JavaSourceParser.resolveJavaFileInfo(code, "random_file_123.java")
        assertEquals("SolutionAlgorithm.java", info.targetFileName)
        assertEquals("SolutionAlgorithm", info.className)
    }

    @Test
    fun testJavaSourceParser_publicRecord() {
        val code = """
            public record StudentScore(String student, int score) {
                public static void main(String[] args) {
                    System.out.println(new StudentScore("Alice", 100));
                }
            }
        """.trimIndent()
        val info = JavaSourceParser.resolveJavaFileInfo(code, "test.java")
        assertEquals("StudentScore.java", info.targetFileName)
        assertEquals("StudentScore", info.className)
    }

    @Test
    fun testJavaSourceParser_publicEnum() {
        val code = """
            public enum Compass {
                NORTH, SOUTH, EAST, WEST;
                public static void main(String[] args) {
                    System.out.println(NORTH);
                }
            }
        """.trimIndent()
        val info = JavaSourceParser.resolveJavaFileInfo(code, "nav.java")
        assertEquals("Compass.java", info.targetFileName)
        assertEquals("Compass", info.className)
    }

    @Test
    fun testJavaSourceParser_noPublicClass_findsClassWithMain() {
        val code = """
            class QuickSorter {
                public static void main(String[] args) {
                    System.out.println("Sorting done");
                }
            }
        """.trimIndent()
        val info = JavaSourceParser.resolveJavaFileInfo(code, "custom_name.java")
        assertEquals("QuickSorter.java", info.targetFileName)
        assertEquals("QuickSorter", info.className)
    }

    @Test
    fun testJavaSourceParser_noClass_defaultsToMainOrSanitized() {
        val code = "System.out.println(\"snippet\");"
        val info = JavaSourceParser.resolveJavaFileInfo(code, "Main.java")
        assertEquals("Main.java", info.targetFileName)
        assertEquals("Main", info.className)
    }

    @Test
    fun testSandboxFileNameSanitizer_preservesRealExtensionsForAnyLanguage() {
        // Python with spaces and numbers
        val py = JavaSourceParser.sanitizeFileNameForSandbox("my python script #1.py", "main.py")
        assertEquals("my_python_script__1.py", py)

        // JavaScript
        val js = JavaSourceParser.sanitizeFileNameForSandbox("frontend test (v2).js", "index.js")
        assertEquals("frontend_test__v2_.js", js)

        // C++
        val cpp = JavaSourceParser.sanitizeFileNameForSandbox("binary search 101.cpp", "main.cpp")
        assertEquals("binary_search_101.cpp", cpp)

        // Go
        val go = JavaSourceParser.sanitizeFileNameForSandbox("web server.go", "main.go")
        assertEquals("web_server.go", go)

        // Rust
        val rs = JavaSourceParser.sanitizeFileNameForSandbox("rust demo.rs", "main.rs")
        assertEquals("rust_demo.rs", rs)
    }

    @Test
    fun testErrorLineParser_customArbitraryFilenames() {
        val pythonErr = "  File \"my custom algorithm.py\", line 42, in <module>"
        val target = ErrorLineParser.parseLineTarget(pythonErr)
        assertNotNull(target)
        assertEquals("my custom algorithm.py", target?.first)
        assertEquals(42, target?.second)

        val cppErr = "algo 1.cpp:18:2: error: expected ';' before '}'"
        val cppTarget = ErrorLineParser.parseLineTarget(cppErr)
        assertNotNull(cppTarget)
        assertEquals("algo 1.cpp", cppTarget?.first)
        assertEquals(18, cppTarget?.second)
    }
}
