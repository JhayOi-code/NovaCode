package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.layout.ContentScale
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Logo centered in the middle with a dedicated frame
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.dp, theme.borderColor, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_linq_logo),
                            contentDescription = "Linq Studio Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.tagline),
                        color = theme.primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Version 2.0.0 (Build 2 • API 36, Min 24)",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Main Founder & Developer Section (Ljhay)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_founder_section"),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = theme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.founder_heading),
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Creator & Sole Developer of Linq Studio",
                                color = theme.lineNumberColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Main Founder Profile Showcase Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.editorBackground,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar Badge
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(theme.primaryColor, Color(0xFF6366F1))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "LJ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.founder_name),
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor,
                                        fontSize = 17.sp
                                    )

                                    Surface(
                                        color = theme.primaryColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "MAIN FOUNDER",
                                            color = theme.primaryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.founder_role),
                                    color = theme.primaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = stringResource(R.string.founder_bio),
                                    color = theme.lineNumberColor,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Developer Mission & Craft Note
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = theme.editorBackground.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "“Engineered from concept to execution solely by Ljhay — dedicated to creating the most efficient, fluid mobile coding studio on Android.”",
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

        // Quick Links & Community Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("about_links_section"),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Resources & Links",
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor,
                        fontSize = 14.sp
                    )

                    val context = androidx.compose.ui.platform.LocalContext.current

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://github.com")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "GitHub Repository", color = theme.primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "github.com ›", color = theme.lineNumberColor, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://prismjs.com")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Prism.js Syntax Engine", color = theme.primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "prismjs.com ›", color = theme.lineNumberColor, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://nanohttpd.github.io")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "NanoHTTPD Embedded Server", color = theme.primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "nanohttpd.org ›", color = theme.lineNumberColor, fontSize = 12.sp)
                    }
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
