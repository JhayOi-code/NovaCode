package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EditorTheme
import com.example.quotes.CodeQuote
import com.example.quotes.QuoteManager
import kotlinx.coroutines.delay

/**
 * Minimalist, theme-adaptive student welcome screen for Linq Studio.
 * Shown only on the very first launch.
 */
@Composable
fun WelcomeScreen(
    theme: EditorTheme,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val quoteManager = remember { QuoteManager(context) }
    var currentQuote by remember { mutableStateOf(QuoteManager.FALLBACK_QUOTE) }

    // Animation stages
    var isLogoVisible by remember { mutableStateOf(false) }
    var displayedTitle by remember { mutableStateOf("") }
    var isQuoteVisible by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    // Load quote & drive entry orchestration
    LaunchedEffect(Unit) {
        currentQuote = quoteManager.getNextQuote()
        delay(120)
        isLogoVisible = true

        delay(400)
        // Typewriter effect for "Linq"
        val fullTitle = "Linq"
        for (i in 1..fullTitle.length) {
            displayedTitle = fullTitle.substring(0, i)
            delay(130)
        }

        delay(250)
        isQuoteVisible = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0.72f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logo_alpha"
    )

    val quoteAlpha by animateFloatAsState(
        targetValue = if (isQuoteVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "quote_alpha"
    )

    // Moving ambient gradient glow
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_offset"
    )

    // "Tap to begin" pulse animation
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    AnimatedVisibility(
        visible = !isExiting,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400)) + slideOutVertically(tween(450, easing = FastOutSlowInEasing)) { -it / 3 }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.editorBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isExiting) {
                        isExiting = true
                    }
                }
                .testTag("welcome_screen_container")
        ) {
            val screenWidth = maxWidth
            // Ambient subtle glow background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                theme.primaryColor.copy(alpha = if (theme.isLight) 0.08f else 0.14f),
                                Color.Transparent
                            ),
                            radius = (screenWidth.value * 1.5f).coerceAtLeast(400f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Center Column: Adaptive Logo, Animated Typewriter Linq, and Random Quote
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                ) {
                    // Adaptive Linq Logo (Theme aware: black on light surface, white on dark surface)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (theme.isLight) Color(0xFFF1F5F9) else theme.surfaceColor,
                        shadowElevation = if (theme.isLight) 4.dp else 0.dp,
                        modifier = Modifier
                            .size(88.dp)
                            .scale(logoScale)
                            .alpha(logoAlpha)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_linq_user_logo),
                                contentDescription = "Linq Logo",
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (theme.isDark) Color.White else Color.Black),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Typewriter Title "Linq"
                    Text(
                        text = displayedTitle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                        color = theme.onSurfaceColor,
                        modifier = Modifier.testTag("welcome_app_title")
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Large Elegant Quote
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(quoteAlpha)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "“${currentQuote.quote}”",
                            fontSize = if (screenWidth < 360.dp) 17.sp else 20.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            color = theme.onSurfaceColor.copy(alpha = 0.92f),
                            textAlign = TextAlign.Center,
                            lineHeight = if (screenWidth < 360.dp) 26.sp else 30.sp,
                            modifier = Modifier.testTag("welcome_quote_text")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "— ${currentQuote.author}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = theme.lineNumberColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("welcome_quote_author")
                        )
                    }
                }

                // Bottom: Subtle "Tap to begin" text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Tap to begin",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        color = theme.primaryColor,
                        modifier = Modifier
                            .alpha(pulseAlpha)
                            .testTag("welcome_tap_to_begin")
                    )
                }
            }
        }
    }

    LaunchedEffect(isExiting) {
        if (isExiting) {
            delay(400)
            onDismiss()
        }
    }
}
