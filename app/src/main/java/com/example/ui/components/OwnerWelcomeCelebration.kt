package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun OwnerWelcomeCelebration(
    onDismiss: () -> Unit
) {
    val fullTitle = "Welcome back, Ljhay"
    val fullSubtitle = "Creator of Linq Studio • LinqStudiosLLC"

    var typedTitle by remember { mutableStateOf("") }
    var typedSubtitle by remember { mutableStateOf("") }
    val glowAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0.5f) }

    // Particles simulation for celebratory 60fps effect
    val particles = remember {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.002f + Random.nextFloat() * 0.004f,
                size = 3f + Random.nextFloat() * 5f,
                color = if (it % 2 == 0) Color(0xFFF59E0B) else Color(0xFF8B5CF6)
            )
        }
    }

    LaunchedEffect(Unit) {
        // Animate badge scale & glow
        badgeScale.animateTo(1.15f, tween(800, easing = FastOutSlowInEasing))
        badgeScale.animateTo(1.0f, tween(400))
        glowAlpha.animateTo(1f, tween(1000))

        // Typewriter sequence
        for (i in 1..fullTitle.length) {
            typedTitle = fullTitle.take(i)
            delay(45)
        }

        delay(150)
        for (i in 1..fullSubtitle.length) {
            typedSubtitle = fullSubtitle.take(i)
            delay(25)
        }

        // Display for total ~4.5 seconds then dismiss
        delay(3200)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE605070D))
            .clickable { onDismiss() } // Skippable by tap!
            .testTag("owner_welcome_celebration")
    ) {
        // Floating Celebratory Particles Canvas (60 FPS)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = glowAlpha.value * 0.7f),
                    radius = p.size,
                    center = Offset(p.x * width, ((p.y + (p.speed * 100)) % 1f) * height)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gold & Purple Verified Crown / Shield Badge
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFF8B5CF6)))
                ),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .size(90.dp)
                    .scale(badgeScale.value)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF3B0764), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Creator Crown",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Typewritten Title
            Text(
                text = typedTitle,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = typedSubtitle,
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Tap to Skip hint
            Text(
                text = "Tap anywhere to enter Linq Studio",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                modifier = Modifier.alpha(0.8f)
            )
        }
    }
}

private class Particle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var size: Float,
    var color: Color
)

@Composable
fun VerifiedOwnerBadge(
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2E1065),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("verified_owner_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Owner of Linq Studio",
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "Owner",
                color = Color(0xFFF59E0B),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
