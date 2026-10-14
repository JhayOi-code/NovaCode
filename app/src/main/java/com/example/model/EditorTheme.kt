package com.example.model

import androidx.compose.ui.graphics.Color

data class EditorTheme(
    val id: String,
    val name: String,
    val editorBackground: Color,
    val surfaceColor: Color,
    val primaryColor: Color,
    val onPrimaryColor: Color = Color.White,
    val onSurfaceColor: Color,
    val lineNumberColor: Color,
    val borderColor: Color
) {
    companion object {
        val LinqDark = EditorTheme(
            id = "linq_dark",
            name = "Linq Dark",
            editorBackground = Color(0xFF0B0F17),
            surfaceColor = Color(0xFF1E293B),
            primaryColor = Color(0xFF6366F1),
            onSurfaceColor = Color(0xFFF8FAFC),
            lineNumberColor = Color(0xFF94A3B8),
            borderColor = Color(0xFF334155)
        )
        val OneDark = EditorTheme(
            id = "one_dark",
            name = "One Dark Pro",
            editorBackground = Color(0xFF282C34),
            surfaceColor = Color(0xFF21252B),
            primaryColor = Color(0xFF61AFEF),
            onSurfaceColor = Color(0xFFABB2BF),
            lineNumberColor = Color(0xFF5C6370),
            borderColor = Color(0xFF181A1F)
        )
        val Dracula = EditorTheme(
            id = "dracula",
            name = "Dracula",
            editorBackground = Color(0xFF282A36),
            surfaceColor = Color(0xFF44475A),
            primaryColor = Color(0xFFBD93F9),
            onSurfaceColor = Color(0xFFF8F8F2),
            lineNumberColor = Color(0xFF6272A4),
            borderColor = Color(0xFF6272A4)
        )
    }
}
