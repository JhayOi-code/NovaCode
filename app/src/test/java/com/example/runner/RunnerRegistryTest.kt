package com.example.runner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunnerRegistryTest {

    @Test
    fun testErrorLineParser_pythonTraceback() {
        val line = "  File \"main.py\", line 14, in <module>"
        val target = ErrorLineParser.parseLineTarget(line)
        assertNotNull(target)
        assertEquals("main.py", target?.first)
        assertEquals(14, target?.second)
    }

    @Test
    fun testErrorLineParser_gccClangError() {
        val line = "main.c:23:5: error: expected ';' before 'return'"
        val target = ErrorLineParser.parseLineTarget(line)
        assertNotNull(target)
        assertEquals("main.c", target?.first)
        assertEquals(23, target?.second)
    }

    @Test
    fun testErrorLineParser_javaStackTrace() {
        val line = "    at Example.main(Example.java:8)"
        val target = ErrorLineParser.parseLineTarget(line)
        assertNotNull(target)
        assertEquals("Example.java", target?.first)
        assertEquals(8, target?.second)
    }

    @Test
    fun testErrorLineParser_rustCompileError() {
        val line = " --> src/main.rs:12:9"
        val target = ErrorLineParser.parseLineTarget(line)
        assertNotNull(target)
        assertEquals("main.rs", target?.first)
        assertEquals(12, target?.second)
    }

    @Test
    fun testErrorLineParser_noMatch() {
        val line = "Welcome to the Linq Studio runner!"
        val target = ErrorLineParser.parseLineTarget(line)
        assertNull(target)
    }

    @Test
    fun testLanguageValidation_javaClassNameMismatchAllowed() {
        val code = "public class Calculator { public static void main(String[] args) {} }"
        val result = LanguageValidationRules.validateCode("java", "Example.java", code)
        // Auto-corrects silently without throwing mismatch error
        assertTrue(result.isValid)
    }

    @Test
    fun testLanguageValidation_javaClassNameMatching() {
        val code = "public class Example { public static void main(String[] args) { System.out.println(1); } }"
        val result = LanguageValidationRules.validateCode("java", "Example.java", code)
        assertTrue(result.isValid)
    }

    @Test
    fun testLanguageValidation_javaMissingMain() {
        val code = "public class Example { public void helper() {} }"
        val result = LanguageValidationRules.validateCode("java", "Example.java", code)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("main(String[] args)") == true)
    }

    @Test
    fun testLanguageValidation_cMissingMain() {
        val code = "#include <stdio.h>\nvoid helper() {}"
        val result = LanguageValidationRules.validateCode("c", "main.c", code)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("main") == true)
    }

    @Test
    fun testLanguageValidation_cValidMain() {
        val code = "#include <stdio.h>\nint main(void) { printf(\"hi\"); return 0; }"
        val result = LanguageValidationRules.validateCode("c", "main.c", code)
        assertTrue(result.isValid)
    }

    @Test
    fun testLanguageValidation_rustValidMain() {
        val code = "fn main() { println!(\"hello\"); }"
        val result = LanguageValidationRules.validateCode("rust", "main.rs", code)
        assertTrue(result.isValid)
    }

    @Test
    fun testLanguageValidation_goValidMain() {
        val code = "package main\nimport \"fmt\"\nfunc main() { fmt.Println(\"ok\") }"
        val result = LanguageValidationRules.validateCode("go", "main.go", code)
        assertTrue(result.isValid)
    }

    @Test
    fun testRemoteLanguageConfig_extensionResolution() {
        assertEquals("python", RemoteLanguageConfig.resolveLanguageForFile("script.py"))
        assertEquals("javascript", RemoteLanguageConfig.resolveLanguageForFile("app.js"))
        assertEquals("typescript", RemoteLanguageConfig.resolveLanguageForFile("index.ts"))
        assertEquals("java", RemoteLanguageConfig.resolveLanguageForFile("Main.java"))
        assertEquals("kotlin", RemoteLanguageConfig.resolveLanguageForFile("Hello.kt"))
        assertEquals("c", RemoteLanguageConfig.resolveLanguageForFile("algo.c"))
        assertEquals("c++", RemoteLanguageConfig.resolveLanguageForFile("algo.cpp"))
        assertEquals("c#", RemoteLanguageConfig.resolveLanguageForFile("Program.cs"))
        assertEquals("rust", RemoteLanguageConfig.resolveLanguageForFile("lib.rs"))
        assertEquals("go", RemoteLanguageConfig.resolveLanguageForFile("server.go"))
        assertEquals("php", RemoteLanguageConfig.resolveLanguageForFile("index.php"))
        assertEquals("ruby", RemoteLanguageConfig.resolveLanguageForFile("app.rb"))
        assertEquals("perl", RemoteLanguageConfig.resolveLanguageForFile("script.pl"))
        assertEquals("lua", RemoteLanguageConfig.resolveLanguageForFile("game.lua"))
        assertEquals("dart", RemoteLanguageConfig.resolveLanguageForFile("main.dart"))
        assertEquals("swift", RemoteLanguageConfig.resolveLanguageForFile("main.swift"))
        assertEquals("r", RemoteLanguageConfig.resolveLanguageForFile("analysis.R"))
        assertEquals("bash", RemoteLanguageConfig.resolveLanguageForFile("deploy.sh"))
        assertEquals("sql", RemoteLanguageConfig.resolveLanguageForFile("schema.sql"))
        assertEquals("scala", RemoteLanguageConfig.resolveLanguageForFile("Main.scala"))
        assertEquals("haskell", RemoteLanguageConfig.resolveLanguageForFile("Main.hs"))
        assertEquals("elixir", RemoteLanguageConfig.resolveLanguageForFile("main.ex"))
        assertEquals("julia", RemoteLanguageConfig.resolveLanguageForFile("sim.jl"))
        assertEquals("fortran", RemoteLanguageConfig.resolveLanguageForFile("math.f90"))
        assertEquals("pascal", RemoteLanguageConfig.resolveLanguageForFile("prog.pas"))
        assertEquals("groovy", RemoteLanguageConfig.resolveLanguageForFile("build.groovy"))
    }

    @Test
    fun testRemoteLanguageConfig_shebangResolution() {
        val pythonShebang = "#!/usr/bin/env python3\nprint('Hello')"
        assertEquals("python", RemoteLanguageConfig.resolveLanguageForFile("script_without_ext", pythonShebang))

        val bashShebang = "#!/bin/bash\necho 'Hello'"
        assertEquals("bash", RemoteLanguageConfig.resolveLanguageForFile("run_script", bashShebang))

        val nodeShebang = "#!/usr/bin/env node\nconsole.log(1);"
        assertEquals("javascript", RemoteLanguageConfig.resolveLanguageForFile("exec_node", nodeShebang))
    }
}
