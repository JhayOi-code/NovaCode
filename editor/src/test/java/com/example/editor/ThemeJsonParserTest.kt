package com.example.editor

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeJsonParserTest {

    @Test
    fun testOneDarkJsonParsing() {
        val scheme = EditorColorScheme.OneDark
        assertEquals("One Dark", scheme.name)

        // Verify One Dark tokens: keyword #C678DD, string #98C379
        val expectedKeyword = EditorColorScheme.parseHexColor("#C678DD", Color.Unspecified)
        val expectedString = EditorColorScheme.parseHexColor("#98C379", Color.Unspecified)
        assertEquals(expectedKeyword, scheme.keyword)
        assertEquals(expectedString, scheme.string)
    }

    @Test
    fun testDraculaJsonParsing() {
        val scheme = EditorColorScheme.Dracula
        assertEquals("Dracula", scheme.name)

        // Verify Dracula tokens: keyword #FF79C6, string #F1FA8C, number #BD93F9
        val expectedKeyword = EditorColorScheme.parseHexColor("#FF79C6", Color.Unspecified)
        val expectedString = EditorColorScheme.parseHexColor("#F1FA8C", Color.Unspecified)
        val expectedNumber = EditorColorScheme.parseHexColor("#BD93F9", Color.Unspecified)
        assertEquals(expectedKeyword, scheme.keyword)
        assertEquals(expectedString, scheme.string)
        assertEquals(expectedNumber, scheme.number)
    }

    @Test
    fun testCustomJsonTokenSerializationCycle() {
        val customJson = """
        {
            "name": "Custom Cyber",
            "colors": {
                "editor.background": "#050510",
                "editor.foreground": "#00FFCC"
            },
            "tokenColors": {
                "keyword": "#FF0077",
                "string": "#00FF88",
                "comment": "#445566"
            }
        }
        """.trimIndent()

        val parsed = EditorColorScheme.fromJson(customJson)
        assertEquals("Custom Cyber", parsed.name)
        val serialized = parsed.toJson()
        val reParsed = EditorColorScheme.fromJson(serialized)
        assertEquals(parsed.keyword, reParsed.keyword)
        assertEquals(parsed.string, reParsed.string)
    }
}
