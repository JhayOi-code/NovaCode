package com.example.theme

import android.content.Context
import com.example.model.EditorTheme
import java.io.BufferedReader
import java.io.InputStreamReader

object JsonThemeLoader {

    val ONE_DARK_JSON = """
    {
      "id": "one_dark",
      "name": "One Dark",
      "type": "dark",
      "colors": {
        "editor.background": "#282C34",
        "editor.foreground": "#ABB2BF",
        "editorGutter.background": "#21252B",
        "editorGutter.border": "#3E4451",
        "editorLineNumber.foreground": "#5C6370",
        "editorLineNumber.activeForeground": "#ABB2BF",
        "editor.lineHighlightBackground": "#2C313A",
        "editorCursor.foreground": "#528BFF",
        "editor.selectionBackground": "#4461AFEF",
        "editor.findMatchHighlightBackground": "#66E5C07B",
        "surface": "#21252B",
        "primary": "#61AFEF",
        "secondary": "#98C379",
        "border": "#3E4451",
        "cardBackground": "#2C313A"
      },
      "tokenColors": {
        "keyword": "#C678DD",
        "string": "#98C379",
        "comment": "#5C6370",
        "number": "#D19A66",
        "type": "#E5C07B",
        "function": "#61AFEF",
        "operator": "#56B6C2",
        "tag": "#E06C75",
        "attribute": "#D19A66"
      }
    }
    """.trimIndent()

    val DRACULA_JSON = """
    {
      "id": "dracula",
      "name": "Dracula",
      "type": "dark",
      "colors": {
        "editor.background": "#282A36",
        "editor.foreground": "#F8F8F2",
        "editorGutter.background": "#21222C",
        "editorGutter.border": "#44475A",
        "editorLineNumber.foreground": "#6272A4",
        "editorLineNumber.activeForeground": "#F8F8F2",
        "editor.lineHighlightBackground": "#343746",
        "editorCursor.foreground": "#F8F8F0",
        "editor.selectionBackground": "#55BD93F9",
        "editor.findMatchHighlightBackground": "#66F1FA8C",
        "surface": "#21222C",
        "primary": "#BD93F9",
        "secondary": "#FF79C6",
        "border": "#44475A",
        "cardBackground": "#343746"
      },
      "tokenColors": {
        "keyword": "#FF79C6",
        "string": "#F1FA8C",
        "comment": "#6272A4",
        "number": "#BD93F9",
        "type": "#8BE9FD",
        "function": "#50FA7B",
        "operator": "#FF79C6",
        "tag": "#FF79C6",
        "attribute": "#50FA7B"
      }
    }
    """.trimIndent()

    fun loadFromAssets(context: Context, fileName: String): Result<EditorTheme> {
        return try {
            val assetPath = if (fileName.startsWith("themes/")) fileName else "themes/$fileName"
            val inputStream = context.assets.open(assetPath)
            val json = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            Result.success(EditorTheme.fromJson(json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadOneDark(): EditorTheme = EditorTheme.fromJson(ONE_DARK_JSON)

    fun loadDracula(): EditorTheme = EditorTheme.fromJson(DRACULA_JSON)
}
