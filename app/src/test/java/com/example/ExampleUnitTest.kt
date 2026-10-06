package com.example

import com.example.model.EditorTheme
import com.example.model.SupportedLanguage
import com.example.syntax.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLanguageDetection() {
        assertEquals(SupportedLanguage.PYTHON, SupportedLanguage.fromFilename("script.py"))
        assertEquals(SupportedLanguage.KOTLIN, SupportedLanguage.fromFilename("MainActivity.kt"))
        assertEquals(SupportedLanguage.JAVASCRIPT, SupportedLanguage.fromFilename("bundle.js"))
        assertEquals(SupportedLanguage.HTML, SupportedLanguage.fromFilename("index.html"))
        assertEquals(SupportedLanguage.PLAINTEXT, SupportedLanguage.fromFilename("notes.txt"))
    }

    @Test
    fun testSyntaxHighlighter() {
        val code = "fun main() {\n    val greeting = \"Hello World\"\n    println(greeting)\n}"
        val result = SyntaxHighlighter.highlight(
            code = code,
            language = SupportedLanguage.KOTLIN,
            theme = EditorTheme.LinqDark
        )
        assertNotNull(result)
        assertEquals(code, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun testThemePresets() {
        assertEquals(10, EditorTheme.AllThemes.size)
        val liquidGlass = EditorTheme.getById("liquid_glass")
        assertTrue(liquidGlass.isLiquidGlass)
    }

    @Test
    fun testViewModelConstructorReflection() {
        val constructor = com.example.viewmodel.EditorViewModel::class.java.getConstructor(android.app.Application::class.java)
        assertNotNull(constructor)
    }
}
