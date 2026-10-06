package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EditorTheme

@Composable
fun AboutScreen(
    theme: EditorTheme,
    modifier: Modifier = Modifier
) {
    val companyName = stringResource(R.string.company_name)
    val footerText = stringResource(R.string.footer_credit, companyName)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .testTag("about_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Identity Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_app_logo_1791043580196),
                        contentDescription = "Linq Studio Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )

                    Text(
                        text = stringResource(R.string.tagline),
                        color = theme.primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Version 1.0.0 (API 36, Target 36, Min 24)",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Founders of the App Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_founders_section"),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.founders_heading),
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Founders & Creators of Linq Studio",
                                color = theme.lineNumberColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Founder Profile Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Founder 1: Ljhay
                        FounderProfileCard(
                            name = stringResource(R.string.founder_ljhay),
                            role = stringResource(R.string.founder_ljhay_role),
                            initials = "LJ",
                            badgeText = "Systems & Arch",
                            accentColor = theme.primaryColor,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )

                        // Founder 2: Mavic
                        FounderProfileCard(
                            name = stringResource(R.string.founder_mavic),
                            role = stringResource(R.string.founder_mavic_role),
                            initials = "MV",
                            badgeText = "Product & Vision",
                            accentColor = Color(0xFF10B981),
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Founders Mission Statement Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.editorBackground.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "“Founded with passion by Ljhay and Mavic to build a fast, uncompromising mobile IDE empowering creators and engineers anywhere.”",
                            color = theme.onSurfaceColor.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Architecture Specs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Engine Architecture",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 14.sp
                        )
                    }

                    SpecRow(label = "UI Framework", value = "Jetpack Compose + Material 3", theme = theme)
                    SpecRow(label = "Language & Toolchain", value = "Kotlin 2.2.10 • AGP 9.1.1", theme = theme)
                    SpecRow(label = "Target Android", value = "Android 15 (VanillaIceCream / API 36)", theme = theme)
                    SpecRow(label = "Min Android", value = "Android 7.0 / 8.0 (API 24/26+)", theme = theme)
                    SpecRow(label = "Memory Management", value = "Virtualized line rendering & async tokenizer", theme = theme)
                    SpecRow(label = "Liquid Glass Blur", value = "Hardware RenderEffect (Android 12+) with software fallback", theme = theme)
                }
            }
        }

        // Policy & Storage Strategy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Storage Access & Google Play Compliance",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "• Google Play Policy: Code editors qualify for MANAGE_EXTERNAL_STORAGE declaration under developer tools, with Tier 2 SAF fallback.\n• F-Droid / GitHub Releases: Full unfiltered direct filesystem POSIX access.\n• Android 11+ Restricted Folders: Direct access to Android/data and Android/obb is blocked by the OS; SAF folder grant or Shizuku bridge can be used for system paths.",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Mandatory Footer Credit
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_footer_credit"),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = footerText,
                        color = theme.lineNumberColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "All rights reserved • Professional Mobile IDE Platform",
                        color = theme.lineNumberColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, theme: EditorTheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = theme.lineNumberColor, fontSize = 12.sp)
        Text(text = value, color = theme.onSurfaceColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FounderProfileCard(
    name: String,
    role: String,
    initials: String,
    badgeText: String,
    accentColor: Color,
    theme: EditorTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = theme.editorBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Avatar badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.6f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                color = theme.onSurfaceColor,
                fontSize = 14.sp
            )

            Text(
                text = role,
                color = theme.lineNumberColor,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )

            Surface(
                color = accentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
