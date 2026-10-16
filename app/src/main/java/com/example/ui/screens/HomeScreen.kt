package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.UserAccount
import com.example.server.ServerStatus

@Composable
fun HomeScreen(
    theme: EditorTheme,
    recentFiles: List<CodeFile>,
    serverStatus: ServerStatus,
    userAccount: UserAccount? = null,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    onOpenRecent: (CodeFile) -> Unit,
    onNewFile: () -> Unit,
    onOpenWorkspace: () -> Unit,
    onOpenPlugins: () -> Unit = {},
    onOpenStarterFile: (String) -> Unit = {},
    onOpenWebDemo: () -> Unit,
    onStartDevServer: () -> Unit,
    onStopDevServer: () -> Unit,
    onUpdateProfile: (String, String, String, String?) -> Unit = { _, _, _, _ -> }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LINQ STUDIO HERO CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Branding Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.primaryColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.4f)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_linq_logo),
                                        contentDescription = "Linq Studio Logo",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Linq Studio",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor,
                                        maxLines = 1,
                                        modifier = Modifier.testTag("hero_app_title")
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.primaryColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "v2.0",
                                            color = theme.primaryColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Ultra-Fast Mobile IDE & Code Studio",
                                    fontSize = 11.sp,
                                    color = theme.primaryColor,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right actions: Full Screen toggle & Studio tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onToggleFullScreen,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_home_fullscreen")
                            ) {
                                Icon(
                                    imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullScreen) "Fit Screen" else "Full Screen",
                                    tint = if (isFullScreen) Color(0xFF10B981) else theme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E1B4B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "LinqStudiosLLC",
                                    color = Color(0xFF818CF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Code anywhere without compromise. High-performance multi-language execution (Python, JS, Bash, Kotlin, C), local HTTP dev server, and extensions ecosystem.",
                        fontSize = 12.sp,
                        color = theme.lineNumberColor,
                        lineHeight = 17.sp
                    )

                    // Quick Action Buttons (Optimized for all screen sizes)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onNewFile,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_hero_new_file")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New File", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onOpenWorkspace,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_hero_files")
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Files", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onOpenWebDemo,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("btn_hero_web_demo")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview", fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // FOUNDER & DEVELOPER SPOTLIGHT CARD
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("founder_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F766E).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF14B8A6)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Founder",
                                tint = Color(0xFF14B8A6),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ljhay",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor,
                                maxLines = 1
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = "Founder & Lead Developer",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sole creator, main founder, and lead developer of Linq Studio.",
                            fontSize = 11.sp,
                            color = theme.lineNumberColor
                        )
                    }
                }
            }

            // LOCAL DEV SERVER CONTROLS
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_dev_server")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = theme.primaryColor)
                            Text(
                                text = "Local Web Dev Server",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                        }

                        val isRunning = serverStatus.isRunning
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isRunning) Color(0xFF14532D) else Color(0xFF334155)
                        ) {
                            Text(
                                text = if (isRunning) "ACTIVE :${serverStatus.portOrNull ?: 8080}" else "STOPPED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hosts HTML, CSS, JavaScript, and dynamic files on internal HTTP port with live preview.",
                        fontSize = 11.sp,
                        color = theme.lineNumberColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!serverStatus.isRunning) {
                            Button(
                                onClick = onStartDevServer,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_start_server")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Server", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = onStopDevServer,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_stop_server")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop Server", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = onOpenWebDemo,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_launch_preview")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Preview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // EXTENSIONS & PLUGINS ECOSYSTEM CARD
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPlugins() }
                    .testTag("card_plugins_link")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFA855F7).copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Extension,
                                contentDescription = "Plugins",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Extensions Marketplace",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Prettier, ESLint, Git, Themes, and custom plugins engine.",
                            fontSize = 11.sp,
                            color = theme.lineNumberColor
                        )
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.lineNumberColor)
                }
            }

            // MULTI-LANGUAGE STARTER TEMPLATES
            Text(
                text = "Language Starters & Templates",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onSurfaceColor
            )

            val starters = listOf(
                Triple("main.py", "Python 3.11 Script", "Fibonacci, math, data structures & loops"),
                Triple("script.js", "JavaScript Algorithm App", "Console logging, array methods & computations"),
                Triple("deploy.sh", "Bash Automation Script", "Unix system automation, date & directory checks"),
                Triple("Main.kt", "Kotlin Console App", "Kotlin classes, functional programming & collections"),
                Triple("index.html", "Web Application Starter", "HTML5, CSS3, JavaScript and live dev server")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                starters.forEach { (fileName, title, subtitle) ->
                    val (badgeColor, icon) = when {
                        fileName.endsWith(".py") -> Pair(Color(0xFF3B82F6), Icons.Default.Terminal)
                        fileName.endsWith(".js") -> Pair(Color(0xFFF59E0B), Icons.Default.Code)
                        fileName.endsWith(".sh") -> Pair(Color(0xFF10B981), Icons.Default.Terminal)
                        fileName.endsWith(".kt") -> Pair(Color(0xFFA855F7), Icons.Default.DataObject)
                        else -> Pair(Color(0xFF14B8A6), Icons.Default.Html)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (fileName == "index.html") onOpenWebDemo()
                                else onOpenStarterFile(fileName)
                            }
                            .testTag("item_starter_${fileName.replace('.', '_')}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(shape = RoundedCornerShape(4.dp), color = badgeColor.copy(alpha = 0.15f)) {
                                        Text(
                                            text = fileName,
                                            color = badgeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    color = theme.lineNumberColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Recent Files if any
            if (recentFiles.isNotEmpty()) {
                Text(
                    text = "Open Editor Tabs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor
                )

                recentFiles.forEach { file ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = theme.surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenRecent(file) }
                            .testTag("recent_file_${file.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                            Text(
                                text = file.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.onSurfaceColor,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (file.isModified) {
                                Text(text = "Unsaved", fontSize = 11.sp, color = Color(0xFFF59E0B), maxLines = 1)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // FOOTER CREDIT
            Text(
                text = "Developed by Ljhay • LinqStudiosLLC",
                fontSize = 11.sp,
                color = theme.lineNumberColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
