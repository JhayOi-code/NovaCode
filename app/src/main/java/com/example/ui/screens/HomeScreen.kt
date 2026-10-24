package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import com.example.util.rememberHardwareFps
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CodeFile
import com.example.model.EditorTheme
import com.example.model.UserAccount
import com.example.ui.components.DebugInspectorDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.GoogleAuthDialog
import com.example.ui.components.UserProfileAvatarView

data class LanguageGuideData(
    val language: String,
    val iconVector: ImageVector,
    val description: String,
    val badgeColor: Color,
    val fileExtension: String,
    val overview: String,
    val starterCode: String,
    val tips: List<String>
)

object LanguageGuides {
    val guides = listOf(
        LanguageGuideData(
            language = "Java",
            iconVector = Icons.Default.Coffee,
            description = "Object-oriented, class-based, robust backend and Android language.",
            badgeColor = Color(0xFFEF4444),
            fileExtension = ".java",
            overview = "Java is a popular strongly typed object-oriented language. Linq Studio supports full syntax highlighting, bracket auto-pairing, smart Enter, and starter code templates for Java files.",
            starterCode = "public class Example {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from Java on Linq Studio!\");\n    }\n}",
            tips = listOf(
                "Class name must match the .java file name",
                "Every statement ends with a semicolon ';'",
                "Use System.out.println() for stdout printing"
            )
        ),
        LanguageGuideData(
            language = "Python",
            iconVector = Icons.Default.Terminal,
            description = "Clean, expressive scripting, AI logic, and data analysis.",
            badgeColor = Color(0xFF3B82F6),
            fileExtension = ".py",
            overview = "Python is renowned for clear syntax and rapid development. Use the built-in terminal or execution engine to test Python logic instantly.",
            starterCode = "def greet(name):\n    return f\"Hello, {name}!\"\n\nif __name__ == \"__main__\":\n    print(greet(\"Linq Developer\"))\n",
            tips = listOf(
                "Indentation determines code blocks (use 4 spaces)",
                "Lines do not require semicolons",
                "Define entry points using __name__ == '__main__'"
            )
        ),
        LanguageGuideData(
            language = "JavaScript",
            iconVector = Icons.Default.Bolt,
            description = "Dynamic web scripting, algorithms, and node applications.",
            badgeColor = Color(0xFFFBBF24),
            fileExtension = ".js",
            overview = "JavaScript powers the modern web. Test DOM logic, write algorithms, and run web preview scripts in Linq Studio.",
            starterCode = "function calculateFibonacci(n) {\n    if (n <= 1) return n;\n    return calculateFibonacci(n - 1) + calculateFibonacci(n - 2);\n}\n\nconsole.log('Fibonacci(7):', calculateFibonacci(7));\n",
            tips = listOf(
                "Use 'const' and 'let' rather than 'var'",
                "Format code with Prettier in one tap",
                "Preview HTML/JS directly in the browser preview"
            )
        ),
        LanguageGuideData(
            language = "Kotlin",
            iconVector = Icons.Default.DataObject,
            description = "Concise, modern multiplatform and Android language.",
            badgeColor = Color(0xFFA78BFA),
            fileExtension = ".kt",
            overview = "Kotlin provides concise syntax, null-safety, and seamless Java interop. Ideal for modern software development.",
            starterCode = "fun main() {\n    val items = listOf(\"Compose\", \"Coroutines\", \"Flow\")\n    items.forEach { println(\"Learning \$it\") }\n}\n",
            tips = listOf(
                "Null-safety is enforced at compile time (? vs !!)",
                "val is read-only, var is mutable",
                "Top-level functions like fun main() require no enclosing class"
            )
        ),
        LanguageGuideData(
            language = "Bash",
            iconVector = Icons.Default.Terminal,
            description = "Automation scripts, file processing, and command line tools.",
            badgeColor = Color(0xFF10B981),
            fileExtension = ".sh",
            overview = "Write shell scripts to automate build tasks, file transforms, and directory operations in the integrated terminal.",
            starterCode = "#!/bin/bash\necho \"Starting Linq Studio automation...\"\ndate\nuname -a\necho \"Done!\"\n",
            tips = listOf(
                "Always start with #!/bin/bash shebang",
                "Execute directly using the Terminal tab",
                "Variables are referenced with $ (e.g. \$VARIABLE)"
            )
        ),
        LanguageGuideData(
            language = "Web (HTML, CSS, JS)",
            iconVector = Icons.Default.Language,
            description = "Front-end interfaces with live local dev server preview.",
            badgeColor = Color(0xFF00E5FF),
            fileExtension = ".html",
            overview = "Build responsive web applications with HTML5, modern CSS, and JavaScript. Linq Studio includes a built-in dev server and web preview inspector.",
            starterCode = "<!DOCTYPE html>\n<html>\n<head>\n    <style>\n        body { font-family: sans-serif; background: #0f172a; color: #fff; text-align: center; padding: 40px; }\n        h1 { color: #00e5ff; }\n    </style>\n</head>\n<body>\n    <h1>Hello from Linq Studio Web</h1>\n    <p>Live preview active!</p>\n</body>\n</html>\n",
            tips = listOf(
                "Tap 'Web Preview' in the editor to inspect live",
                "Includes local dev server capabilities",
                "Supports CSS Flexbox and modern JS APIs"
            )
        )
    )
}

@Composable
fun HomeScreen(
    theme: EditorTheme,
    recentFiles: List<CodeFile>,
    serverStatus: String,
    userAccount: UserAccount,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {},
    onOpenRecent: (CodeFile) -> Unit,
    onNewFile: () -> Unit,
    onOpenWorkspace: () -> Unit,
    onOpenLanguageGuide: (LanguageGuideData) -> Unit,
    onOpenWebDemo: () -> Unit,
    onStartDevServer: () -> Unit,
    onStopDevServer: () -> Unit,
    onOpenWebPreview: (String?) -> Unit = {},
    onUpdateProfile: (String, String, String, String?, Long, String, String, String?) -> Unit,
    onSignInWithGoogle: (String, String) -> Unit = { _, _ -> },
    onSignOut: () -> Unit = {},
    onResetWorkspace: () -> Unit = {}
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }

    val fpsState = rememberHardwareFps()

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val avatarPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("home_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header with Linq Studio logo & Debug button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.primaryColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.4f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_linq_user_logo),
                                    contentDescription = "Linq Studios",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Linq Studios",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Pro Mobile IDE • ${fpsState.currentFps} FPS (${fpsState.targetRefreshRate}Hz)",
                                color = theme.lineNumberColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                // Quick Debug Inspector Trigger
                IconButton(
                    onClick = { showDebugDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceVariant)
                        .testTag("home_debug_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Debug Inspector",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Hero / Profile Banner with Google Sign-In & Permanent Status
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                modifier = Modifier.fillMaxWidth().testTag("home_profile_banner")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val avatarColor = Color(userAccount.avatarColorHex)
                            UserProfileAvatarView(
                                account = userAccount,
                                size = 52.dp,
                                borderColor = avatarColor,
                                modifier = Modifier
                                    .scale(avatarPulse)
                                    .clickable { showEditProfileDialog = true }
                                    .testTag("home_profile_avatar")
                            )

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userAccount.username,
                                        color = theme.onSurfaceColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (userAccount.isSignedIn) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = "Verified",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = userAccount.title,
                                    color = theme.lineNumberColor,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (userAccount.isSignedIn) "Signed In (${userAccount.authProvider})" else "Guest Mode • Tap Edit to customize",
                                    color = if (userAccount.isSignedIn) Color(0xFF10B981) else theme.lineNumberColor.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = if (userAccount.isSignedIn) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }

                        // Edit / Sign In Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.surfaceVariant)
                                    .testTag("home_edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { showGoogleAuthDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.surfaceVariant)
                                    .testTag("home_google_auth_button")
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google Sign In",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Direct Google Sign In Prompt if not signed in
                    if (!userAccount.isSignedIn) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGoogleAuthDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Tap to Sign In with Google permanently",
                                        color = theme.onSurfaceColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = theme.lineNumberColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNewFile,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("home_new_file_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create File", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onOpenWorkspace,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("home_open_workspace_button")
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Workspace", fontSize = 13.sp)
                }
            }
        }

        // Language Guides Section (Clean vector icons - No emojis!)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Language Guides",
                            color = theme.onSurfaceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Java • Python • JS • Kotlin • Bash • Web",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp
                    )
                }

                // Grid of Language Guide Cards
                LanguageGuides.guides.forEach { guide ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLanguageGuide(guide) }
                            .testTag("guide_card_${guide.language.lowercase().substringBefore(' ')}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = guide.badgeColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, guide.badgeColor.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = guide.iconVector,
                                            contentDescription = guide.language,
                                            tint = guide.badgeColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = guide.language,
                                            color = theme.onSurfaceColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = guide.fileExtension,
                                            color = guide.badgeColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = guide.description,
                                        color = theme.lineNumberColor,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Guide",
                                tint = theme.lineNumberColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Recent Files Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Recent Files",
                    color = theme.onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (recentFiles.isEmpty()) {
                    Text(
                        text = "No open files yet. Tap 'New File' or choose a Language Guide above.",
                        color = theme.lineNumberColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recentFiles.take(5).forEach { file ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
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
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        color = theme.onSurfaceColor,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${file.content.lines().size} lines • ${file.language}",
                                        color = theme.lineNumberColor,
                                        fontSize = 11.sp
                                    )
                                }
                                if (file.isModified) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFFF59E0B), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dev Server Status Card
        // Dev Server Status Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                modifier = Modifier.fillMaxWidth().testTag("home_dev_server_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (serverStatus.startsWith("Running")) Color(0xFF10B981) else theme.lineNumberColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Local Dev Server",
                                    color = theme.onSurfaceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = serverStatus,
                                    color = if (serverStatus.startsWith("Running")) Color(0xFF34D399) else theme.lineNumberColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    if (serverStatus.startsWith("Running")) onStopDevServer() else onStartDevServer()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (serverStatus.startsWith("Running")) Color(0xFFEF4444) else theme.surfaceVariant,
                                    contentColor = if (serverStatus.startsWith("Running")) Color.White else theme.onSurfaceColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp).testTag("toggle_dev_server_button")
                            ) {
                                Text(if (serverStatus.startsWith("Running")) "Stop" else "Start", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onOpenWebPreview(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp).testTag("home_open_web_preview_button")
                            ) {
                                Text("Preview Web", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Website Files quick testing row
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Test file:", fontSize = 10.sp, color = theme.lineNumberColor, fontWeight = FontWeight.Bold)
                        listOf("index.html", "about.html", "style.css", "app.js").forEach { fileName ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.editorBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                modifier = Modifier.clickable { onOpenWebPreview(fileName) }.testTag("quick_test_$fileName")
                            ) {
                                Text(
                                    text = fileName,
                                    color = if (fileName.endsWith(".html")) theme.primaryColor else theme.secondaryColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showEditProfileDialog) {
        EditProfileDialog(
            theme = theme,
            userAccount = userAccount,
            onSave = onUpdateProfile,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showGoogleAuthDialog) {
        GoogleAuthDialog(
            theme = theme,
            userAccount = userAccount,
            onSignInSuccess = onSignInWithGoogle,
            onSignOut = onSignOut,
            onDismiss = { showGoogleAuthDialog = false }
        )
    }

    if (showDebugDialog) {
        DebugInspectorDialog(
            theme = theme,
            openFilesCount = recentFiles.size,
            activeFileName = recentFiles.firstOrNull()?.name ?: "",
            lintErrorsCount = 0,
            lintWarningsCount = 0,
            serverStatus = serverStatus,
            onTriggerGc = { System.gc() },
            onResetWorkspace = onResetWorkspace,
            onDismiss = { showDebugDialog = false }
        )
    }
}
