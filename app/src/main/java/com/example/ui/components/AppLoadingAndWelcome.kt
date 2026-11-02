package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.EditorTheme
import com.example.model.UserAccount
import kotlinx.coroutines.delay

/**
 * Unique personalized welcome message model.
 */
data class UniqueWelcomeMessage(
    val title: String,
    val badge: String,
    val greeting: String,
    val quote: String,
    val highlight: String
)

object WelcomeMessageGenerator {
    private val uniqueGreetings = listOf(
        UniqueWelcomeMessage(
            title = "Architect",
            badge = "ENGINEERING GRADE",
            greeting = "Welcome, Software Architect! Your portable developer studio is initialized.",
            quote = "\"Simplicity is prerequisite for reliability.\" — Edsger Dijkstra",
            highlight = "Complete real folder hierarchies, local terminal execution, and zero mock fallbacks."
        ),
        UniqueWelcomeMessage(
            title = "Creator",
            badge = "CREATIVE STUDIO",
            greeting = "Welcome aboard, Creator! Your ideas, your code, on your terms.",
            quote = "\"The best way to predict the future is to invent it.\" — Alan Kay",
            highlight = "Python runner, live HTML5 localhost preview, Java engine, and custom cropped photo avatars."
        ),
        UniqueWelcomeMessage(
            title = "Pioneer",
            badge = "NEXT GEN IDE",
            greeting = "Welcome, Pioneer! Coding at desktop speed directly on your phone.",
            quote = "\"Talk is cheap. Show me the code.\" — Linus Torvalds",
            highlight = "Hardware keyboard shortcuts (Ctrl+S, Ctrl+F, Ctrl+Enter) and instant responsive feedback."
        ),
        UniqueWelcomeMessage(
            title = "Builder",
            badge = "HIGH PERFORMANCE",
            greeting = "Welcome to Linq Studios, Builder! Your workspace is calibrated and ready.",
            quote = "\"First, solve the problem. Then, write the code.\" — John Johnson",
            highlight = "Accurate folder scaffolding, real project templates, and zero-compromise developer UX."
        )
    )

    fun getUniqueGreeting(account: UserAccount?): UniqueWelcomeMessage {
        val seed = (account?.username?.hashCode() ?: 0) + (account?.title?.hashCode() ?: 0)
        val index = kotlin.math.abs(seed) % uniqueGreetings.size
        return uniqueGreetings[index]
    }
}

/**
 * Animated Startup Loading Screen for Linq Studios.
 * Displays during app initial bootstrap with smooth breathing logo and engine status.
 */
@Composable
fun AppLoadingScreen(
    theme: EditorTheme,
    onLoadingComplete: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0.08f) }
    var statusText by remember { mutableStateOf("Initializing Workspace Engine...") }
    var isDone by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    LaunchedEffect(Unit) {
        delay(200)
        progress = 0.35f
        statusText = "Mounting Project Trees & Real Folders..."
        delay(350)
        progress = 0.70f
        statusText = "Calibrating Syntax Engine & Shortcuts..."
        delay(350)
        progress = 1.0f
        statusText = "Linq Studios Ready"
        delay(300)
        isDone = true
        delay(250)
        onLoadingComplete()
    }

    AnimatedVisibility(
        visible = !isDone,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(400)) + scaleOut(targetScale = 1.08f, animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0F17))
                .testTag("app_loading_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Glowing Linq Emblem (New Black and White Logo)
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(92.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_linq_user_logo),
                        contentDescription = "Linq Logo",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "LINQ STUDIOS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 3.sp
                )

                Text(
                    text = "Mobile Native IDE & Dev Server",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusText,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Personalized Welcome Experience for New Users.
 * Shows smooth animated introduction with unique messages per user and quick feature highlights.
 */
@Composable
fun UniqueWelcomeDialog(
    theme: EditorTheme,
    userAccount: UserAccount,
    onDismiss: () -> Unit
) {
    val welcome = remember(userAccount) {
        WelcomeMessageGenerator.getUniqueGreeting(userAccount)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("unique_welcome_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = theme.primaryColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(14.dp))
                        Text(
                            text = welcome.badge,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Title & Greeting
                Text(
                    text = welcome.greeting,
                    color = theme.onSurfaceColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                // Quote Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.editorBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = welcome.quote,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = welcome.highlight,
                            color = theme.primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                // 4 Core Superpowers
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FeatureHighlightRow(
                        icon = Icons.Default.Folder,
                        title = "Real Folders & Project Templates",
                        desc = "Create real empty folders or scaffold full Python, Web, & Java workspaces.",
                        tint = Color(0xFFF59E0B)
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.Keyboard,
                        title = "Hardware Shortcuts & Quick Keys",
                        desc = "Ctrl+S (Save), Ctrl+F (Find), and Ctrl+Enter (Run code instantly).",
                        tint = Color(0xFF00E5FF)
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.Crop,
                        title = "Custom Profile & Gallery Crop",
                        desc = "Pick any photo from your phone, crop it seamlessly, and build your developer identity.",
                        tint = Color(0xFF10B981)
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.PlayArrow,
                        title = "Interactive Localhost Server",
                        desc = "Live dev server on port 8080 with zero internet required.",
                        tint = Color(0xFFA855F7)
                    )
                }

                // Launch Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_launch_studio")
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch Linq Studios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111827).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.15f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}
