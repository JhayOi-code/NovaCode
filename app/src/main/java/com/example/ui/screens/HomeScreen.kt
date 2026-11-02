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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.FolderOpen
import com.example.ui.components.ActiveUsersMonitorCard
import com.example.ui.components.VerifiedOwnerBadge
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
import com.example.ui.components.FolderPickerBottomSheet
import com.example.ui.components.LoginSignupWindow
import com.example.ui.components.UserProfileAvatarView
import java.io.File

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
    onResetWorkspace: () -> Unit = {},
    activeUsersCount: Int = 1,
    isOnline: Boolean = true,
    activeWebFolder: String = "www",
    availableHostFolders: List<String> = listOf("src", "www", "public", "scripts", "styles", "docs", "data", "site"),
    onChooseFolderWithParam: (String) -> Unit = {},
    onImportDeviceDirectory: (File, String) -> Unit = { _, _ -> },
    onImportZipArchive: (File, String) -> Unit = { _, _ -> },
    onImportMultipleFiles: (File, String) -> Unit = { _, _ -> },
    onChooseFolder: () -> Unit = {},
    onChooseFiles: () -> Unit = {},
    openFiles: List<CodeFile> = emptyList()
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLoginSignupSheet by remember { mutableStateOf(false) }
    var showDebugDialog by remember { mutableStateOf(false) }
    var showFolderPickerSheet by remember { mutableStateOf(false) }

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
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (theme.isDark) Color.White else Color.Black),
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
                                text = "Student Code Studio • ${fpsState.currentFps} FPS (${fpsState.targetRefreshRate}Hz)",
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

        // Hero / Profile Banner with In-App Account & Permanent Status
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (userAccount.isOwnerVerified) Color(0xFFF59E0B).copy(alpha = 0.6f) else theme.borderColor),
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
                            val avatarRingColor = when {
                                userAccount.isOwnerVerified -> Color(0xFFF59E0B)
                                userAccount.isSignedIn -> theme.primaryColor
                                else -> theme.primaryColor.copy(alpha = 0.5f)
                            }
                            UserProfileAvatarView(
                                account = if (userAccount.isOwnerVerified) userAccount.copy(username = "Ljhay") else userAccount,
                                size = 52.dp,
                                borderColor = avatarRingColor,
                                modifier = Modifier
                                    .scale(avatarPulse)
                                    .clickable { showEditProfileDialog = true }
                                    .testTag("home_profile_avatar")
                            )

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (userAccount.isOwnerVerified) "Ljhay" else userAccount.username,
                                        color = theme.onSurfaceColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (userAccount.isOwnerVerified) {
                                        VerifiedOwnerBadge()
                                    } else if (userAccount.isSignedIn) {
                                        Icon(
                                            imageVector = Icons.Default.VerifiedUser,
                                            contentDescription = "Verified Account",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (userAccount.isOwnerVerified) "Linq Studios LLC • System Owner" else userAccount.title,
                                    color = if (userAccount.isOwnerVerified) Color(0xFFD97706) else theme.lineNumberColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (userAccount.isOwnerVerified) FontWeight.Medium else FontWeight.Normal
                                )
                                Text(
                                    text = when {
                                        userAccount.isOwnerVerified -> "Owner Verified • Full Dev Access"
                                        userAccount.isSignedIn -> "Signed In (${userAccount.authProvider})"
                                        else -> "Guest Mode • Tap to Sign In"
                                    },
                                    color = when {
                                        userAccount.isOwnerVerified -> Color(0xFFF59E0B)
                                        userAccount.isSignedIn -> Color(0xFF10B981)
                                        else -> theme.lineNumberColor.copy(alpha = 0.8f)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (userAccount.isSignedIn) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }

                        // Edit / Sign In Actions with 48x48 min touch targets
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (userAccount.isSignedIn) {
                                        showEditProfileDialog = true
                                    } else {
                                        showLoginSignupSheet = true
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.surfaceVariant)
                                    .testTag("home_edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = if (userAccount.isSignedIn) Icons.Default.Edit else Icons.Default.Lock,
                                    contentDescription = if (userAccount.isSignedIn) "Edit Profile" else "Log in to customize profile",
                                    tint = if (userAccount.isSignedIn) theme.primaryColor else theme.lineNumberColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Direct In-App Log In / Sign Up Prompt if not signed in
                    if (!userAccount.isSignedIn) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLoginSignupSheet = true }
                                .testTag("home_auth_trigger_row")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Tap to log in or sign up",
                                        color = theme.onSurfaceColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = theme.lineNumberColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Part 7B: Owner-Only Live Active Users Monitor Card
        if (userAccount.isOwnerVerified) {
            item {
                ActiveUsersMonitorCard(
                    theme = theme,
                    activeUsersCount = activeUsersCount,
                    isOnline = isOnline
                )
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
        // Dev Server Status Card with unclipped Stop button, Test folder label, and real files
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                modifier = Modifier.fillMaxWidth().testTag("home_dev_server_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                        // Part 5C: Start / Stop & Preview Web buttons with minHeight = 48dp, Stop minWidth = 72dp, softWrap = false
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isRunning = serverStatus.startsWith("Running")
                            Button(
                                onClick = {
                                    if (isRunning) onStopDevServer() else onStartDevServer()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) Color(0xFFEF4444) else theme.surfaceVariant,
                                    contentColor = if (isRunning) Color.White else theme.onSurfaceColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
                                    .testTag("toggle_dev_server_button")
                            ) {
                                Text(
                                    text = if (isRunning) "Stop" else "Start",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Button(
                                onClick = { onOpenWebPreview(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 88.dp, minHeight = 48.dp)
                                    .testTag("home_open_web_preview_button")
                            ) {
                                Text(
                                    text = "Preview Web",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Part 5C: Test folder row & Folder/Files chooser chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Test folder:",
                                fontSize = 11.sp,
                                color = theme.lineNumberColor,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.editorBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
                            ) {
                                Text(
                                    text = activeWebFolder.ifBlank { "www" },
                                    color = theme.primaryColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        showFolderPickerSheet = true
                                        onChooseFolder()
                                    }
                                    .testTag("choose_folder_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                                    Text("Choose folder", fontSize = 11.sp, color = theme.onSurfaceColor, fontWeight = FontWeight.Medium)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        showFolderPickerSheet = true
                                        onChooseFiles()
                                    }
                                    .testTag("choose_files_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = theme.secondaryColor, modifier = Modifier.size(14.dp))
                                    Text("Choose file(s)", fontSize = 11.sp, color = theme.onSurfaceColor, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Real project files horizontally scrollable chip row (HTML files first)
                    val displayWebFiles = openFiles.ifEmpty { recentFiles }.filter {
                        it.name.endsWith(".html", ignoreCase = true) || it.name.endsWith(".css", ignoreCase = true) || it.name.endsWith(".js", ignoreCase = true) || it.name.endsWith(".svg", ignoreCase = true)
                    }.map { it.name }.distinct().sortedWith(compareBy({ !it.endsWith(".html", ignoreCase = true) }, { it })).ifEmpty {
                        listOf("index.html", "about.html", "style.css", "app.js")
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(end = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayWebFiles.forEach { fileName ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.editorBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                    modifier = Modifier
                                        .clickable { onOpenWebPreview(fileName) }
                                        .testTag("quick_test_$fileName")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                fileName.endsWith(".html") -> Icons.Default.Language
                                                fileName.endsWith(".css") -> Icons.Default.Bolt
                                                fileName.endsWith(".js") -> Icons.Default.DataObject
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = if (fileName.endsWith(".html")) theme.primaryColor else theme.secondaryColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = fileName,
                                            color = theme.onSurfaceColor,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Right fade gradient indicating horizontal scrollability
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(32.dp)
                                .height(34.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, theme.surfaceColor)
                                    )
                                )
                        )
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

    if (showLoginSignupSheet) {
        LoginSignupWindow(
            theme = theme,
            onSignInSuccess = { name, email ->
                onSignInWithGoogle(name, email)
                showLoginSignupSheet = false
            },
            onBack = { showLoginSignupSheet = false },
            onDismiss = { showLoginSignupSheet = false }
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

    if (showFolderPickerSheet) {
        FolderPickerBottomSheet(
            theme = theme,
            activeFolder = activeWebFolder,
            workspaceFiles = openFiles.ifEmpty { recentFiles },
            availableFolders = availableHostFolders,
            onSelectWorkspaceFolder = { folder ->
                onChooseFolderWithParam(folder)
                showFolderPickerSheet = false
            },
            onImportDeviceDirectory = { dir, name ->
                onImportDeviceDirectory(dir, name)
                showFolderPickerSheet = false
            },
            onImportZipArchive = { dir, name ->
                onImportZipArchive(dir, name)
                showFolderPickerSheet = false
            },
            onImportMultipleFiles = { dir, name ->
                onImportMultipleFiles(dir, name)
                showFolderPickerSheet = false
            },
            onDismiss = { showFolderPickerSheet = false }
        )
    }
}
