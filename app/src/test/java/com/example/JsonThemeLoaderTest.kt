package com.example

import com.example.model.EditorTheme
import com.example.theme.JsonThemeLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonThemeLoaderTest {

    @Test
    fun testOneDarkJsonLoadsCorrectTokens() {
        val theme = JsonThemeLoader.loadOneDark()
        assertEquals("one_dark", theme.id)
        assertEquals("One Dark", theme.name)
        assertTrue(theme.isDark)

        // Verify One Dark tokens: keyword #C678DD, string #98C379, function #61AFEF
        val expectedKeyword = EditorTheme.parseHexColor("#C678DD", theme.keywordColor)
        val expectedString = EditorTheme.parseHexColor("#98C379", theme.stringColor)
        val expectedFunction = EditorTheme.parseHexColor("#61AFEF", theme.functionColor)

        assertEquals(expectedKeyword, theme.keywordColor)
        assertEquals(expectedString, theme.stringColor)
        assertEquals(expectedFunction, theme.functionColor)
    }

    @Test
    fun testDraculaJsonLoadsCorrectTokens() {
        val theme = JsonThemeLoader.loadDracula()
        assertEquals("dracula", theme.id)
        assertEquals("Dracula", theme.name)
        assertTrue(theme.isDark)

        // Verify Dracula tokens: keyword #FF79C6, string #F1FA8C, function #50FA7B
        val expectedKeyword = EditorTheme.parseHexColor("#FF79C6", theme.keywordColor)
        val expectedString = EditorTheme.parseHexColor("#F1FA8C", theme.stringColor)
        val expectedFunction = EditorTheme.parseHexColor("#50FA7B", theme.functionColor)

        assertEquals(expectedKeyword, theme.keywordColor)
        assertEquals(expectedString, theme.stringColor)
        assertEquals(expectedFunction, theme.functionColor)
    }

    @Test
    fun testThemeJsonExportAndImport() {
        val original = JsonThemeLoader.loadOneDark()
        val jsonString = original.toJson()
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("tokenColors"))

        val reImported = EditorTheme.fromJson(jsonString)
        assertEquals(original.keywordColor, reImported.keywordColor)
        assertEquals(original.stringColor, reImported.stringColor)
        assertEquals(original.name, reImported.name)
    }
}
