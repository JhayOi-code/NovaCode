package com.example.model

import androidx.compose.ui.graphics.Color

data class EditorTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val isLiquidGlass: Boolean = false,
    
    // Editor UI Colors
    val editorBackground: Color,
    val gutterBackground: Color,
    val lineNumberColor: Color,
    val currentLineNumberColor: Color,
    val currentLineBackground: Color,
    val selectionColor: Color,
    val cursorColor: Color,
    val searchHighlightColor: Color,

    // UI Container Colors
    val surfaceColor: Color,
    val onSurfaceColor: Color,
    val primaryColor: Color,
    val onPrimaryColor: Color,
    val secondaryColor: Color,
    val cardBackgroundColor: Color,
    val borderColor: Color,

    // Syntax Highlight Colors
    val keywordColor: Color,
    val stringColor: Color,
    val commentColor: Color,
    val numberColor: Color,
    val typeColor: Color,
    val functionColor: Color,
    val operatorColor: Color,
    val tagColor: Color,
    val attributeColor: Color
) {
    companion object {
        // 1. Nova Dark (Default)
        val NovaDark = EditorTheme(
            id = "nova_dark",
            name = "Nova Dark",
            isDark = true,
            editorBackground = Color(0xFF0F141C),
            gutterBackground = Color(0xFF0A0E14),
            lineNumberColor = Color(0xFF4A5568),
            currentLineNumberColor = Color(0xFF00E5FF),
            currentLineBackground = Color(0xFF161E2E),
            selectionColor = Color(0x4400E5FF),
            cursorColor = Color(0xFF00E5FF),
            searchHighlightColor = Color(0x66FFD600),
            surfaceColor = Color(0xFF131A26),
            onSurfaceColor = Color(0xFFE2E8F0),
            primaryColor = Color(0xFF00E5FF),
            onPrimaryColor = Color(0xFF09101D),
            secondaryColor = Color(0xFF7C4DFF),
            cardBackgroundColor = Color(0xFF182232),
            borderColor = Color(0xFF233044),
            keywordColor = Color(0xFFFF5252),
            stringColor = Color(0xFF69F0AE),
            commentColor = Color(0xFF64748B),
            numberColor = Color(0xFFFFAB40),
            typeColor = Color(0xFF40C4FF),
            functionColor = Color(0xFFFFD740),
            operatorColor = Color(0xFFE040FB),
            tagColor = Color(0xFFFF5252),
            attributeColor = Color(0xFF40C4FF)
        )

        // 2. Liquid Glass (Translucent frosted acrylic with specular borders)
        val LiquidGlass = EditorTheme(
            id = "liquid_glass",
            name = "Liquid Glass",
            isDark = true,
            isLiquidGlass = true,
            editorBackground = Color(0x990B0F19),
            gutterBackground = Color(0x77070A12),
            lineNumberColor = Color(0xFF8899B0),
            currentLineNumberColor = Color(0xFF38BDF8),
            currentLineBackground = Color(0x4038BDF8),
            selectionColor = Color(0x5538BDF8),
            cursorColor = Color(0xFF38BDF8),
            searchHighlightColor = Color(0x77FACC15),
            surfaceColor = Color(0x80111827),
            onSurfaceColor = Color(0xFFF1F5F9),
            primaryColor = Color(0xFF38BDF8),
            onPrimaryColor = Color(0xFF030712),
            secondaryColor = Color(0xFFA855F7),
            cardBackgroundColor = Color(0x701F2937),
            borderColor = Color(0x55E2E8F0),
            keywordColor = Color(0xFFF472B6),
            stringColor = Color(0xFF4ADE80),
            commentColor = Color(0xFF94A3B8),
            numberColor = Color(0xFFFBBF24),
            typeColor = Color(0xFF60A5FA),
            functionColor = Color(0xFF38BDF8),
            operatorColor = Color(0xFFC084FC),
            tagColor = Color(0xFFF472B6),
            attributeColor = Color(0xFF60A5FA)
        )

        // 3. Dracula
        val Dracula = EditorTheme(
            id = "dracula",
            name = "Dracula",
            isDark = true,
            editorBackground = Color(0xFF282A36),
            gutterBackground = Color(0xFF21222C),
            lineNumberColor = Color(0xFF6272A4),
            currentLineNumberColor = Color(0xFFF8F8F2),
            currentLineBackground = Color(0xFF343746),
            selectionColor = Color(0x55BD93F9),
            cursorColor = Color(0xFFF8F8F0),
            searchHighlightColor = Color(0x66F1FA8C),
            surfaceColor = Color(0xFF21222C),
            onSurfaceColor = Color(0xFFF8F8F2),
            primaryColor = Color(0xFFBD93F9),
            onPrimaryColor = Color(0xFF282A36),
            secondaryColor = Color(0xFFFF79C6),
            cardBackgroundColor = Color(0xFF343746),
            borderColor = Color(0xFF44475A),
            keywordColor = Color(0xFFFF79C6),
            stringColor = Color(0xFFF1FA8C),
            commentColor = Color(0xFF6272A4),
            numberColor = Color(0xFFBD93F9),
            typeColor = Color(0xFF8BE9FD),
            functionColor = Color(0xFF50FA7B),
            operatorColor = Color(0xFFFF79C6),
            tagColor = Color(0xFFFF79C6),
            attributeColor = Color(0xFF50FA7B)
        )

        // 4. One Dark
        val OneDark = EditorTheme(
            id = "one_dark",
            name = "One Dark",
            isDark = true,
            editorBackground = Color(0xFF282C34),
            gutterBackground = Color(0xFF21252B),
            lineNumberColor = Color(0xFF5C6370),
            currentLineNumberColor = Color(0xFFABB2BF),
            currentLineBackground = Color(0xFF2C313A),
            selectionColor = Color(0x4461AFEF),
            cursorColor = Color(0xFF528BFF),
            searchHighlightColor = Color(0x66E5C07B),
            surfaceColor = Color(0xFF21252B),
            onSurfaceColor = Color(0xFFABB2BF),
            primaryColor = Color(0xFF61AFEF),
            onPrimaryColor = Color(0xFF1E2227),
            secondaryColor = Color(0xFF98C379),
            cardBackgroundColor = Color(0xFF2C313A),
            borderColor = Color(0xFF3E4451),
            keywordColor = Color(0xFFC678DD),
            stringColor = Color(0xFF98C379),
            commentColor = Color(0xFF5C6370),
            numberColor = Color(0xFFD19A66),
            typeColor = Color(0xFFE5C07B),
            functionColor = Color(0xFF61AFEF),
            operatorColor = Color(0xFF56B6C2),
            tagColor = Color(0xFFE06C75),
            attributeColor = Color(0xFFD19A66)
        )

        // 5. Monokai
        val Monokai = EditorTheme(
            id = "monokai",
            name = "Monokai",
            isDark = true,
            editorBackground = Color(0xFF272822),
            gutterBackground = Color(0xFF1E1F1C),
            lineNumberColor = Color(0xFF75715E),
            currentLineNumberColor = Color(0xFFF8F8F2),
            currentLineBackground = Color(0xFF3E3D32),
            selectionColor = Color(0x5549483E),
            cursorColor = Color(0xFFF8F8F0),
            searchHighlightColor = Color(0x66FFE792),
            surfaceColor = Color(0xFF1E1F1C),
            onSurfaceColor = Color(0xFFF8F8F2),
            primaryColor = Color(0xFFA6E22E),
            onPrimaryColor = Color(0xFF272822),
            secondaryColor = Color(0xFFF92672),
            cardBackgroundColor = Color(0xFF3E3D32),
            borderColor = Color(0xFF49483E),
            keywordColor = Color(0xFFF92672),
            stringColor = Color(0xFFE6DB74),
            commentColor = Color(0xFF75715E),
            numberColor = Color(0xFFAE81FF),
            typeColor = Color(0xFF66D9EF),
            functionColor = Color(0xFFA6E22E),
            operatorColor = Color(0xFFF92672),
            tagColor = Color(0xFFF92672),
            attributeColor = Color(0xFFA6E22E)
        )

        // 6. Nord
        val Nord = EditorTheme(
            id = "nord",
            name = "Nord",
            isDark = true,
            editorBackground = Color(0xFF2E3440),
            gutterBackground = Color(0xFF242933),
            lineNumberColor = Color(0xFF4C566A),
            currentLineNumberColor = Color(0xFFD8DEE9),
            currentLineBackground = Color(0xFF3B4252),
            selectionColor = Color(0x4488C0D0),
            cursorColor = Color(0xFFD8DEE9),
            searchHighlightColor = Color(0x66EBCB8B),
            surfaceColor = Color(0xFF242933),
            onSurfaceColor = Color(0xFFECEFF4),
            primaryColor = Color(0xFF88C0D0),
            onPrimaryColor = Color(0xFF2E3440),
            secondaryColor = Color(0xFF81A1C1),
            cardBackgroundColor = Color(0xFF3B4252),
            borderColor = Color(0xFF434C5E),
            keywordColor = Color(0xFF81A1C1),
            stringColor = Color(0xFFA3BE8C),
            commentColor = Color(0xFF616E88),
            numberColor = Color(0xFFB48EAD),
            typeColor = Color(0xFF8FBCBB),
            functionColor = Color(0xFF88C0D0),
            operatorColor = Color(0xFF81A1C1),
            tagColor = Color(0xFF81A1C1),
            attributeColor = Color(0xFF8FBCBB)
        )

        // 7. Solarized Dark
        val SolarizedDark = EditorTheme(
            id = "solarized_dark",
            name = "Solarized Dark",
            isDark = true,
            editorBackground = Color(0xFF002B36),
            gutterBackground = Color(0xFF073642),
            lineNumberColor = Color(0xFF586E75),
            currentLineNumberColor = Color(0xFF93A1A1),
            currentLineBackground = Color(0xFF073642),
            selectionColor = Color(0x442AA198),
            cursorColor = Color(0xFF268BD2),
            searchHighlightColor = Color(0x66B58900),
            surfaceColor = Color(0xFF073642),
            onSurfaceColor = Color(0xFF839496),
            primaryColor = Color(0xFF268BD2),
            onPrimaryColor = Color(0xFF002B36),
            secondaryColor = Color(0xFF2AA198),
            cardBackgroundColor = Color(0xFF0D4350),
            borderColor = Color(0xFF144D5C),
            keywordColor = Color(0xFF859900),
            stringColor = Color(0xFF2AA198),
            commentColor = Color(0xFF586E75),
            numberColor = Color(0xFFD33682),
            typeColor = Color(0xFFB58900),
            functionColor = Color(0xFF268BD2),
            operatorColor = Color(0xFF719E07),
            tagColor = Color(0xFF268BD2),
            attributeColor = Color(0xFFB58900)
        )

        // 8. AMOLED Black
        val AmoledBlack = EditorTheme(
            id = "amoled_black",
            name = "AMOLED Black",
            isDark = true,
            editorBackground = Color(0xFF000000),
            gutterBackground = Color(0xFF000000),
            lineNumberColor = Color(0xFF3E3E3E),
            currentLineNumberColor = Color(0xFF00E5FF),
            currentLineBackground = Color(0xFF141414),
            selectionColor = Color(0x5500E5FF),
            cursorColor = Color(0xFF00E5FF),
            searchHighlightColor = Color(0x66FFD600),
            surfaceColor = Color(0xFF0A0A0A),
            onSurfaceColor = Color(0xFFEEEEEE),
            primaryColor = Color(0xFF00E5FF),
            onPrimaryColor = Color(0xFF000000),
            secondaryColor = Color(0xFF7C4DFF),
            cardBackgroundColor = Color(0xFF121212),
            borderColor = Color(0xFF222222),
            keywordColor = Color(0xFFFF5252),
            stringColor = Color(0xFF00E676),
            commentColor = Color(0xFF757575),
            numberColor = Color(0xFFFF9100),
            typeColor = Color(0xFF40C4FF),
            functionColor = Color(0xFFFFD600),
            operatorColor = Color(0xFFE040FB),
            tagColor = Color(0xFFFF5252),
            attributeColor = Color(0xFF40C4FF)
        )

        // 9. GitHub Dark
        val GitHubDark = EditorTheme(
            id = "github_dark",
            name = "GitHub Dark",
            isDark = true,
            editorBackground = Color(0xFF0D1117),
            gutterBackground = Color(0xFF090D13),
            lineNumberColor = Color(0xFF484F58),
            currentLineNumberColor = Color(0xFFC9D1D9),
            currentLineBackground = Color(0xFF161B22),
            selectionColor = Color(0x4458A6FF),
            cursorColor = Color(0xFF58A6FF),
            searchHighlightColor = Color(0x66D29922),
            surfaceColor = Color(0xFF161B22),
            onSurfaceColor = Color(0xFFC9D1D9),
            primaryColor = Color(0xFF58A6FF),
            onPrimaryColor = Color(0xFF0D1117),
            secondaryColor = Color(0xFF238636),
            cardBackgroundColor = Color(0xFF21262D),
            borderColor = Color(0xFF30363D),
            keywordColor = Color(0xFFFF7B72),
            stringColor = Color(0xFFA5D6FF),
            commentColor = Color(0xFF8B949E),
            numberColor = Color(0xFF79C0FF),
            typeColor = Color(0xFFFFA657),
            functionColor = Color(0xFFD2A8FF),
            operatorColor = Color(0xFFFF7B72),
            tagColor = Color(0xFF7EE787),
            attributeColor = Color(0xFF79C0FF)
        )

        // 10. Light (Solar Clean)
        val Light = EditorTheme(
            id = "light",
            name = "Solar Light",
            isDark = false,
            editorBackground = Color(0xFFFFFFFF),
            gutterBackground = Color(0xFFF6F8FA),
            lineNumberColor = Color(0xFF8C959F),
            currentLineNumberColor = Color(0xFF0969DA),
            currentLineBackground = Color(0xFFEAEFF5),
            selectionColor = Color(0x330969DA),
            cursorColor = Color(0xFF0969DA),
            searchHighlightColor = Color(0x44FFD600),
            surfaceColor = Color(0xFFF6F8FA),
            onSurfaceColor = Color(0xFF1F2328),
            primaryColor = Color(0xFF0969DA),
            onPrimaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFF1A7F37),
            cardBackgroundColor = Color(0xFFFFFFFF),
            borderColor = Color(0xFFD0D7DE),
            keywordColor = Color(0xFFCF222E),
            stringColor = Color(0xFF0A3069),
            commentColor = Color(0xFF6E7781),
            numberColor = Color(0xFF0550AE),
            typeColor = Color(0xFF953800),
            functionColor = Color(0xFF8250DF),
            operatorColor = Color(0xFF0550AE),
            tagColor = Color(0xFF116329),
            attributeColor = Color(0xFF0550AE)
        )

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

        fun fromJson(jsonStr: String, fallback: EditorTheme = NovaDark): EditorTheme {
            return try {
                val map = com.example.editor.MiniJson.parse(jsonStr)
                if (map.isEmpty()) return fallback

                val id = map["id"] as? String ?: fallback.id
                val name = map["name"] as? String ?: fallback.name
                val typeStr = map["type"] as? String ?: if (fallback.isDark) "dark" else "light"
                val isDark = typeStr == "dark"

                @Suppress("UNCHECKED_CAST")
                val colorsObj = map["colors"] as? Map<String, Any>
                @Suppress("UNCHECKED_CAST")
                val tokensObj = map["tokenColors"] as? Map<String, Any>

                val editorBg = parseHexColor(colorsObj?.get("editor.background") as? String, fallback.editorBackground)
                val gutterBg = parseHexColor(colorsObj?.get("editorGutter.background") as? String, fallback.gutterBackground)
                val lineNum = parseHexColor(colorsObj?.get("editorLineNumber.foreground") as? String, fallback.lineNumberColor)
                val activeLineNum = parseHexColor(colorsObj?.get("editorLineNumber.activeForeground") as? String, fallback.currentLineNumberColor)
                val lineHl = parseHexColor(colorsObj?.get("editor.lineHighlightBackground") as? String, fallback.currentLineBackground)
                val cursor = parseHexColor(colorsObj?.get("editorCursor.foreground") as? String, fallback.cursorColor)
                val sel = parseHexColor(colorsObj?.get("editor.selectionBackground") as? String, fallback.selectionColor)
                val searchHl = parseHexColor(colorsObj?.get("editor.findMatchHighlightBackground") as? String, fallback.searchHighlightColor)

                val surface = parseHexColor(colorsObj?.get("surface") as? String, fallback.surfaceColor)
                val onSurface = parseHexColor(colorsObj?.get("editor.foreground") as? String, fallback.onSurfaceColor)
                val primary = parseHexColor(colorsObj?.get("primary") as? String, fallback.primaryColor)
                val secondary = parseHexColor(colorsObj?.get("secondary") as? String, fallback.secondaryColor)
                val border = parseHexColor(colorsObj?.get("border") as? String, fallback.borderColor)
                val cardBg = parseHexColor(colorsObj?.get("cardBackground") as? String, fallback.cardBackgroundColor)

                val kw = parseHexColor(tokensObj?.get("keyword") as? String, fallback.keywordColor)
                val str = parseHexColor(tokensObj?.get("string") as? String, fallback.stringColor)
                val cmt = parseHexColor(tokensObj?.get("comment") as? String, fallback.commentColor)
                val num = parseHexColor(tokensObj?.get("number") as? String, fallback.numberColor)
                val typ = parseHexColor(tokensObj?.get("type") as? String, fallback.typeColor)
                val fn = parseHexColor(tokensObj?.get("function") as? String, fallback.functionColor)
                val op = parseHexColor(tokensObj?.get("operator") as? String, fallback.operatorColor)
                val tg = parseHexColor(tokensObj?.get("tag") as? String, fallback.tagColor)
                val attr = parseHexColor(tokensObj?.get("attribute") as? String, fallback.attributeColor)

                EditorTheme(
                    id = id,
                    name = name,
                    isDark = isDark,
                    editorBackground = editorBg,
                    gutterBackground = gutterBg,
                    lineNumberColor = lineNum,
                    currentLineNumberColor = activeLineNum,
                    currentLineBackground = lineHl,
                    selectionColor = sel,
                    cursorColor = cursor,
                    searchHighlightColor = searchHl,
                    surfaceColor = surface,
                    onSurfaceColor = onSurface,
                    primaryColor = primary,
                    onPrimaryColor = if (isDark) Color(0xFF09101D) else Color(0xFFFFFFFF),
                    secondaryColor = secondary,
                    cardBackgroundColor = cardBg,
                    borderColor = border,
                    keywordColor = kw,
                    stringColor = str,
                    commentColor = cmt,
                    numberColor = num,
                    typeColor = typ,
                    functionColor = fn,
                    operatorColor = op,
                    tagColor = tg,
                    attributeColor = attr
                )
            } catch (_: Exception) {
                fallback
            }
        }

        val AllThemes = listOf(
            NovaDark,
            LiquidGlass,
            Dracula,
            OneDark,
            Monokai,
            Nord,
            SolarizedDark,
            AmoledBlack,
            GitHubDark,
            Light
        )

        fun getById(id: String): EditorTheme = AllThemes.firstOrNull { it.id == id } ?: NovaDark
    }

    fun toJson(): String {
        return """{
  "id": "$id",
  "name": "$name",
  "type": "${if (isDark) "dark" else "light"}",
  "colors": {
    "editor.background": "${colorToHex(editorBackground)}",
    "editor.foreground": "${colorToHex(onSurfaceColor)}",
    "editorGutter.background": "${colorToHex(gutterBackground)}",
    "editorLineNumber.foreground": "${colorToHex(lineNumberColor)}",
    "editorLineNumber.activeForeground": "${colorToHex(currentLineNumberColor)}",
    "editor.lineHighlightBackground": "${colorToHex(currentLineBackground)}",
    "editorCursor.foreground": "${colorToHex(cursorColor)}",
    "editor.selectionBackground": "${colorToHex(selectionColor)}",
    "editor.findMatchHighlightBackground": "${colorToHex(searchHighlightColor)}",
    "surface": "${colorToHex(surfaceColor)}",
    "primary": "${colorToHex(primaryColor)}",
    "secondary": "${colorToHex(secondaryColor)}",
    "border": "${colorToHex(borderColor)}",
    "cardBackground": "${colorToHex(cardBackgroundColor)}"
  },
  "tokenColors": {
    "keyword": "${colorToHex(keywordColor)}",
    "string": "${colorToHex(stringColor)}",
    "comment": "${colorToHex(commentColor)}",
    "number": "${colorToHex(numberColor)}",
    "type": "${colorToHex(typeColor)}",
    "function": "${colorToHex(functionColor)}",
    "operator": "${colorToHex(operatorColor)}",
    "tag": "${colorToHex(tagColor)}",
    "attribute": "${colorToHex(attributeColor)}"
  }
}"""
    }
}
