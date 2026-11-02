package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.model.EditorTheme

@Composable
fun LinqStudioTheme(
    theme: EditorTheme = EditorTheme.LinqLight,
    content: @Composable () -> Unit
) {
    val colorScheme = if (theme.isLight) {
        lightColorScheme(
            primary = theme.primaryColor,
            secondary = theme.secondaryColor,
            background = theme.editorBackground,
            surface = theme.surfaceColor,
            surfaceVariant = theme.surfaceVariant,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = theme.onSurfaceColor,
            onSurface = theme.onSurfaceColor,
            outline = theme.borderColor
        )
    } else {
        darkColorScheme(
            primary = theme.primaryColor,
            secondary = theme.secondaryColor,
            background = theme.editorBackground,
            surface = theme.surfaceColor,
            surfaceVariant = theme.surfaceVariant,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = theme.onSurfaceColor,
            onSurface = theme.onSurfaceColor,
            outline = theme.borderColor
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = theme.editorBackground.toArgb()
            window.navigationBarColor = theme.surfaceColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = theme.isLight
            controller.isAppearanceLightNavigationBars = theme.isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
