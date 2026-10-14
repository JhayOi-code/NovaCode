package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun AboutScreen(theme: EditorTheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.primaryColor.copy(alpha = 0.2f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Code, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(40.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Linq Studio",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = theme.onSurfaceColor
        )

        Text(
            text = "Version 2.0 • Ultra-Fast Mobile IDE",
            fontSize = 13.sp,
            color = theme.lineNumberColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LinqStudiosLLC",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor
                )
                Text(
                    text = "Sole creator, main founder, and lead developer: Ljhay",
                    fontSize = 13.sp,
                    color = theme.primaryColor
                )
                Text(
                    text = "Engineered with Jetpack Compose, Kotlin Symbol Processing compatibility, local HTTP development server, and extensible plugin architecture.",
                    fontSize = 12.sp,
                    color = theme.lineNumberColor,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
