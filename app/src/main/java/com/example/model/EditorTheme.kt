package com.example.model

import androidx.compose.ui.graphics.Color

data class EditorTheme(
    val id: String,
    val name: String,
    val category: String = "Dark",
    val editorBackground: Color,
    val surfaceColor: Color,
    val surfaceVariant: Color,
    val onSurfaceColor: Color,
    val primaryColor: Color,
    val secondaryColor: Color,
    val borderColor: Color,
    val lineNumberColor: Color,
    val activeLineNumberColor: Color,
    val selectionColor: Color,
    val keywordColor: Color,
    val stringColor: Color,
    val commentColor: Color,
    val numberColor: Color,
    val functionColor: Color,
    val variableColor: Color,
    val errorColor: Color = Color(0xFFEF4444),
    val warningColor: Color = Color(0xFFF59E0B),
    val infoColor: Color = Color(0xFF3B82F6)
) {
    companion object {
        val LinqDark = EditorTheme(
            id = "linq-dark",
            name = "Linq Dark (Default)",
            category = "Cyber",
            editorBackground = Color(0xFF0B132B),
            surfaceColor = Color(0xFF1C2541),
            surfaceVariant = Color(0xFF2E3B5E),
            onSurfaceColor = Color(0xFFF8FAFC),
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFA78BFA),
            borderColor = Color(0xFF2E3B5E),
            lineNumberColor = Color(0xFF64748B),
            activeLineNumberColor = Color(0xFF00E5FF),
            selectionColor = Color(0x3300E5FF),
            keywordColor = Color(0xFFC084FC),
            stringColor = Color(0xFF34D399),
            commentColor = Color(0xFF64748B),
            numberColor = Color(0xFFFBBF24),
            functionColor = Color(0xFF60A5FA),
            variableColor = Color(0xFFE2E8F0)
        )

        val CyberpunkNeon = EditorTheme(
            id = "cyberpunk-neon",
            name = "Cyberpunk Neon",
            category = "High Contrast",
            editorBackground = Color(0xFF070714),
            surfaceColor = Color(0xFF120E2E),
            surfaceVariant = Color(0xFF261D58),
            onSurfaceColor = Color(0xFFFDF4FF),
            primaryColor = Color(0xFFFF007F),
            secondaryColor = Color(0xFF00F5D4),
            borderColor = Color(0xFF3B2D7D),
            lineNumberColor = Color(0xFF7A6B9C),
            activeLineNumberColor = Color(0xFFFF007F),
            selectionColor = Color(0x40FF007F),
            keywordColor = Color(0xFFFF007F),
            stringColor = Color(0xFF00F5D4),
            commentColor = Color(0xFF6B6289),
            numberColor = Color(0xFFFFE600),
            functionColor = Color(0xFF7B2CBF),
            variableColor = Color(0xFFE0AAFF)
        )

        val MonokaiPro = EditorTheme(
            id = "monokai-pro",
            name = "Monokai Pro",
            category = "Warm",
            editorBackground = Color(0xFF222225),
            surfaceColor = Color(0xFF2D2A2E),
            surfaceVariant = Color(0xFF403E41),
            onSurfaceColor = Color(0xFFFCFCFA),
            primaryColor = Color(0xFFFFD866),
            secondaryColor = Color(0xFFFF6188),
            borderColor = Color(0xFF49464E),
            lineNumberColor = Color(0xFF727072),
            activeLineNumberColor = Color(0xFFFFD866),
            selectionColor = Color(0x33FFD866),
            keywordColor = Color(0xFFFF6188),
            stringColor = Color(0xFFA9DC76),
            commentColor = Color(0xFF727072),
            numberColor = Color(0xFFAB9DF2),
            functionColor = Color(0xFF78DCE8),
            variableColor = Color(0xFFFCFCFA)
        )

        val NordFrost = EditorTheme(
            id = "nord-frost",
            name = "Nord Frost",
            category = "Arctic",
            editorBackground = Color(0xFF242933),
            surfaceColor = Color(0xFF2E3440),
            surfaceVariant = Color(0xFF3B4252),
            onSurfaceColor = Color(0xFFECEFF4),
            primaryColor = Color(0xFF88C0D0),
            secondaryColor = Color(0xFF81A1C1),
            borderColor = Color(0xFF4C566A),
            lineNumberColor = Color(0xFF616E88),
            activeLineNumberColor = Color(0xFF88C0D0),
            selectionColor = Color(0x3388C0D0),
            keywordColor = Color(0xFF81A1C1),
            stringColor = Color(0xFFA3BE8C),
            commentColor = Color(0xFF616E88),
            numberColor = Color(0xFFB48EAD),
            functionColor = Color(0xFF8FBCBB),
            variableColor = Color(0xFFD8DEE9)
        )

        val TokyoNight = EditorTheme(
            id = "tokyo-night",
            name = "Tokyo Night",
            category = "Pastel",
            editorBackground = Color(0xFF16161E),
            surfaceColor = Color(0xFF1A1B26),
            surfaceVariant = Color(0xFF24283B),
            onSurfaceColor = Color(0xFFA9B1D6),
            primaryColor = Color(0xFF7AA2F7),
            secondaryColor = Color(0xFFBB9AF7),
            borderColor = Color(0xFF292E42),
            lineNumberColor = Color(0xFF565F89),
            activeLineNumberColor = Color(0xFF7AA2F7),
            selectionColor = Color(0x337AA2F7),
            keywordColor = Color(0xFFBB9AF7),
            stringColor = Color(0xFF9ECE6A),
            commentColor = Color(0xFF565F89),
            numberColor = Color(0xFFFF9E64),
            functionColor = Color(0xFF7AA2F7),
            variableColor = Color(0xFFC0CAF5)
        )

        val GitHubDark = EditorTheme(
            id = "github-dark",
            name = "GitHub Dark",
            category = "Clean",
            editorBackground = Color(0xFF0D1117),
            surfaceColor = Color(0xFF161B22),
            surfaceVariant = Color(0xFF21262D),
            onSurfaceColor = Color(0xFFC9D1D9),
            primaryColor = Color(0xFF58A6FF),
            secondaryColor = Color(0xFFBC8CFF),
            borderColor = Color(0xFF30363D),
            lineNumberColor = Color(0xFF6E7681),
            activeLineNumberColor = Color(0xFF58A6FF),
            selectionColor = Color(0x3358A6FF),
            keywordColor = Color(0xFFFF7B72),
            stringColor = Color(0xFFA5D6FF),
            commentColor = Color(0xFF8B949E),
            numberColor = Color(0xFF79C0FF),
            functionColor = Color(0xFFD2A8FF),
            variableColor = Color(0xFFFFA657)
        )

        val SolarizedDark = EditorTheme(
            id = "solarized-dark",
            name = "Solarized Dark",
            category = "Classic",
            editorBackground = Color(0xFF002B36),
            surfaceColor = Color(0xFF073642),
            surfaceVariant = Color(0xFF0D4654),
            onSurfaceColor = Color(0xFF93A1A1),
            primaryColor = Color(0xFF268BD2),
            secondaryColor = Color(0xFFB58900),
            borderColor = Color(0xFF0E5263),
            lineNumberColor = Color(0xFF586E75),
            activeLineNumberColor = Color(0xFF2AA198),
            selectionColor = Color(0x33268BD2),
            keywordColor = Color(0xFF859900),
            stringColor = Color(0xFF2AA198),
            commentColor = Color(0xFF586E75),
            numberColor = Color(0xFFD33682),
            functionColor = Color(0xFF268BD2),
            variableColor = Color(0xFFB58900)
        )

        val Dracula = EditorTheme(
            id = "dracula",
            name = "Dracula",
            category = "Vibrant",
            editorBackground = Color(0xFF282A36),
            surfaceColor = Color(0xFF21222C),
            surfaceVariant = Color(0xFF343746),
            onSurfaceColor = Color(0xFFF8F8F2),
            primaryColor = Color(0xFFBD93F9),
            secondaryColor = Color(0xFFFF79C6),
            borderColor = Color(0xFF44475A),
            lineNumberColor = Color(0xFF6272A4),
            activeLineNumberColor = Color(0xFF50FA7B),
            selectionColor = Color(0x33BD93F9),
            keywordColor = Color(0xFFFF79C6),
            stringColor = Color(0xFFF1FA8C),
            commentColor = Color(0xFF6272A4),
            numberColor = Color(0xFFBD93F9),
            functionColor = Color(0xFF50FA7B),
            variableColor = Color(0xFF8BE9FD)
        )

        val OneDark = EditorTheme(
            id = "one-dark",
            name = "One Dark Pro",
            category = "Atom",
            editorBackground = Color(0xFF282C34),
            surfaceColor = Color(0xFF21252B),
            surfaceVariant = Color(0xFF2C313C),
            onSurfaceColor = Color(0xFFABB2BF),
            primaryColor = Color(0xFF61AFEF),
            secondaryColor = Color(0xFFC678DD),
            borderColor = Color(0xFF3E4451),
            lineNumberColor = Color(0xFF5C6370),
            activeLineNumberColor = Color(0xFF61AFEF),
            selectionColor = Color(0x3361AFEF),
            keywordColor = Color(0xFFC678DD),
            stringColor = Color(0xFF98C379),
            commentColor = Color(0xFF5C6370),
            numberColor = Color(0xFFD19A66),
            functionColor = Color(0xFF61AFEF),
            variableColor = Color(0xFFE06C75)
        )

        val allThemes: List<EditorTheme> = listOf(
            LinqDark,
            CyberpunkNeon,
            TokyoNight,
            NordFrost,
            MonokaiPro,
            GitHubDark,
            SolarizedDark,
            Dracula,
            OneDark
        )

        fun findById(id: String?): EditorTheme {
            return allThemes.find { it.id == id } ?: LinqDark
        }
    }
}
