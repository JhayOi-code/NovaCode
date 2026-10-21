package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EditorTheme

@Composable
fun AboutScreen(theme: EditorTheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("about_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // App Logo
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = theme.primaryColor.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(2.dp, theme.primaryColor),
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_linq_user_logo),
                    contentDescription = "Linq Studios Logo",
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Linq Studios",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onSurfaceColor
            )
            Text(
                text = "Professional Mobile IDE • v2.4.0",
                fontSize = 13.sp,
                color = theme.lineNumberColor,
                fontFamily = FontFamily.Monospace
            )
        }

        // Developer Attribution Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor),
            modifier = Modifier.fillMaxWidth().testTag("developer_attribution_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(theme.primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Ljhay",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = theme.onSurfaceColor
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Creator",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Main Developer & Creator",
                            fontSize = 11.sp,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Ljhay is the main developer who built the app itself.",
                    color = theme.onSurfaceColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
        }

        // Mission Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "High-Performance Mobile Coding",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = theme.primaryColor
                )
                Text(
                    text = "Linq Studios brings desktop-grade developer tooling into your pocket. Built 100% natively with Jetpack Compose, featuring bracket auto-closing, smart indent Enter, incremental syntax highlighting, 3-tier live lint markers, non-overlapping layout, Git panel, and custom BYOK AI Assistant with diff previews.",
                    color = theme.onSurfaceColor.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // Key Features List
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Core Capabilities",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = theme.onSurfaceColor
                )

                FeatureItem(Icons.Default.Code, "Multi-tab Editor", "Tab switcher, dirty state, bracket auto-pairs, smart Enter", theme)
                FeatureItem(Icons.Default.Palette, "9 Developer Themes", "Linq Dark, Cyberpunk, Monokai, Nord, Tokyo Night, Dracula, etc.", theme)
                FeatureItem(Icons.Default.AutoAwesome, "BYOK AI Assistant", "Gemini, OpenAI, Anthropic support with inline diff reviews", theme)
                FeatureItem(Icons.Default.Folder, "Git Source Control", "Stage, unstage, commit, and push simulation", theme)
                FeatureItem(Icons.Default.Terminal, "Integrated Terminal & Dev Server", "Interactive shell and local preview server", theme)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, subtitle: String, theme: EditorTheme) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = theme.primaryColor.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
            }
        }
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = theme.onSurfaceColor)
            Text(subtitle, fontSize = 11.sp, color = theme.lineNumberColor)
        }
    }
}
