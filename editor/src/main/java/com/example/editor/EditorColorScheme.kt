package com.example.editor

import androidx.compose.ui.graphics.Color

/**
 * Visual styling tokens for the virtualized high-performance code editor.
 * Supports loading and serialization from JSON syntax highlighting token schemes.
 */
data class EditorColorScheme(
    val name: String = "Default Dark",
    val background: Color = Color(0xFF0F141C),
    val gutterBackground: Color = Color(0xFF0A0E14),
    val gutterBorder: Color = Color(0xFF1E2638),
    val lineNumber: Color = Color(0xFF505A6E),
    val currentLineNumber: Color = Color(0xFF00E5FF),
    val currentLineHighlight: Color = Color(0xFF161E2E),
    val textColor: Color = Color(0xFFE2E8F0),
    val cursorColor: Color = Color(0xFF00E5FF),
    val selectionColor: Color = Color(0x4400E5FF),
    val searchHighlight: Color = Color(0x66FFD600),

    // Syntax token colors
    val keyword: Color = Color(0xFFFF5252),
    val string: Color = Color(0xFF69F0AE),
    val comment: Color = Color(0xFF64748B),
    val number: Color = Color(0xFFFFAB40),
    val type: Color = Color(0xFF40C4FF),
    val function: Color = Color(0xFFFFD740),
    val operator: Color = Color(0xFFE040FB),
    val tag: Color = Color(0xFFFF5252),
    val attribute: Color = Color(0xFF40C4FF)
) {
    fun toJson(): String {
        return """{
  "name": "$name",
  "type": "dark",
  "colors": {
    "editor.background": "${colorToHex(background)}",
    "editor.foreground": "${colorToHex(textColor)}",
    "editorGutter.background": "${colorToHex(gutterBackground)}",
    "editorGutter.border": "${colorToHex(gutterBorder)}",
    "editorLineNumber.foreground": "${colorToHex(lineNumber)}",
    "editorLineNumber.activeForeground": "${colorToHex(currentLineNumber)}",
    "editor.lineHighlightBackground": "${colorToHex(currentLineHighlight)}",
    "editorCursor.foreground": "${colorToHex(cursorColor)}",
    "editor.selectionBackground": "${colorToHex(selectionColor)}",
    "editor.findMatchHighlightBackground": "${colorToHex(searchHighlight)}"
  },
  "tokenColors": {
    "keyword": "${colorToHex(keyword)}",
    "string": "${colorToHex(string)}",
    "comment": "${colorToHex(comment)}",
    "number": "${colorToHex(number)}",
    "type": "${colorToHex(type)}",
    "function": "${colorToHex(function)}",
    "operator": "${colorToHex(operator)}",
    "tag": "${colorToHex(tag)}",
    "attribute": "${colorToHex(attribute)}"
  }
}"""
    }

    companion object {
        fun parseHexColor(hex: String?, default: Color): Color {
            if (hex == null) return default
            val clean = hex.trim().removePrefix("#")
            return try {
                when (clean.length) {
                    3 -> {
                        val r = clean[0].toString().repeat(2).toInt(16)
                        val g = clean[1].toString().repeat(2).toInt(16)
                        val b = clean[2].toString().repeat(2).toInt(16)
                        Color(r, g, b)
                    }
                    6 -> {
                        val rgb = clean.toLong(16)
                        Color(0xFF000000 or rgb)
                    }
                    8 -> {
                        val argb = clean.toLong(16)
                        Color(argb)
                    }
                    else -> default
                }
            } catch (_: Exception) {
                default
            }
        }

        private fun colorToHex(color: Color): String {
            val a = (color.alpha * 255).toInt()
            val r = (color.red * 255).toInt()
            val g = (color.green * 255).toInt()
            val b = (color.blue * 255).toInt()
            return if (a == 255) {
                String.format("#%02X%02X%02X", r, g, b)
            } else {
                String.format("#%02X%02X%02X%02X", a, r, g, b)
            }
        }

        fun fromJson(jsonStr: String, fallback: EditorColorScheme = DefaultDark): EditorColorScheme {
            return try {
                val map = MiniJson.parse(jsonStr)
                if (map.isEmpty()) return fallback

                val themeName = map["name"] as? String ?: fallback.name
                @Suppress("UNCHECKED_CAST")
                val colorsObj = map["colors"] as? Map<String, Any>
                @Suppress("UNCHECKED_CAST")
                val tokensObj = map["tokenColors"] as? Map<String, Any>

                val bg = parseHexColor(colorsObj?.get("editor.background") as? String, fallback.background)
                val fg = parseHexColor(colorsObj?.get("editor.foreground") as? String, fallback.textColor)
                val gutterBg = parseHexColor(colorsObj?.get("editorGutter.background") as? String, fallback.gutterBackground)
                val gutterBorder = parseHexColor(colorsObj?.get("editorGutter.border") as? String, fallback.gutterBorder)
                val lineNum = parseHexColor(colorsObj?.get("editorLineNumber.foreground") as? String, fallback.lineNumber)
                val activeLineNum = parseHexColor(colorsObj?.get("editorLineNumber.activeForeground") as? String, fallback.currentLineNumber)
                val lineHighlight = parseHexColor(colorsObj?.get("editor.lineHighlightBackground") as? String, fallback.currentLineHighlight)
                val cursor = parseHexColor(colorsObj?.get("editorCursor.foreground") as? String, fallback.cursorColor)
                val selection = parseHexColor(colorsObj?.get("editor.selectionBackground") as? String, fallback.selectionColor)
                val searchHighlight = parseHexColor(colorsObj?.get("editor.findMatchHighlightBackground") as? String, fallback.searchHighlight)

                val kw = parseHexColor(tokensObj?.get("keyword") as? String, fallback.keyword)
                val str = parseHexColor(tokensObj?.get("string") as? String, fallback.string)
                val cmt = parseHexColor(tokensObj?.get("comment") as? String, fallback.comment)
                val num = parseHexColor(tokensObj?.get("number") as? String, fallback.number)
                val typ = parseHexColor(tokensObj?.get("type") as? String, fallback.type)
                val fn = parseHexColor(tokensObj?.get("function") as? String, fallback.function)
                val op = parseHexColor(tokensObj?.get("operator") as? String, fallback.operator)
                val tg = parseHexColor(tokensObj?.get("tag") as? String, fallback.tag)
                val attr = parseHexColor(tokensObj?.get("attribute") as? String, fallback.attribute)

                EditorColorScheme(
                    name = themeName,
                    background = bg,
                    gutterBackground = gutterBg,
                    gutterBorder = gutterBorder,
                    lineNumber = lineNum,
                    currentLineNumber = activeLineNum,
                    currentLineHighlight = lineHighlight,
                    textColor = fg,
                    cursorColor = cursor,
                    selectionColor = selection,
                    searchHighlight = searchHighlight,
                    keyword = kw,
                    string = str,
                    comment = cmt,
                    number = num,
                    type = typ,
                    function = fn,
                    operator = op,
                    tag = tg,
                    attribute = attr
                )
            } catch (_: Exception) {
                fallback
            }
        }

        val DefaultDark = EditorColorScheme()
        val DefaultLight = EditorColorScheme(
            name = "Default Light",
            background = Color(0xFFFFFFFF),
            gutterBackground = Color(0xFFF6F8FA),
            gutterBorder = Color(0xFFE1E4E8),
            lineNumber = Color(0xFF8C959F),
            currentLineNumber = Color(0xFF0969DA),
            currentLineHighlight = Color(0xFFF1F5F9),
            textColor = Color(0xFF1F2328),
            cursorColor = Color(0xFF0969DA),
            selectionColor = Color(0x330969DA),
            searchHighlight = Color(0x44FFD600),
            keyword = Color(0xFFCF222E),
            string = Color(0xFF0A3069),
            comment = Color(0xFF6E7781),
            number = Color(0xFF0550AE),
            type = Color(0xFF953800),
            function = Color(0xFF8250DF),
            operator = Color(0xFF0550AE)
        )

        // One Dark JSON definition
        const val ONE_DARK_JSON = """{
  "name": "One Dark",
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
    "editor.findMatchHighlightBackground": "#66E5C07B"
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
}"""

        // Dracula JSON definition
        const val DRACULA_JSON = """{
  "name": "Dracula",
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
    "editor.findMatchHighlightBackground": "#66F1FA8C"
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
}"""

        val OneDark: EditorColorScheme by lazy { fromJson(ONE_DARK_JSON) }
        val Dracula: EditorColorScheme by lazy { fromJson(DRACULA_JSON) }
    }
}
