package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

        // App Logo (Theme adaptive: black on light surface, white on dark surface)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (theme.isLight) Color(0xFFF1F5F9) else theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor),
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_linq_user_logo),
                    contentDescription = "Linq Studio Logo",
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(if (theme.isDark) Color.White else Color.Black),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Linq Studio",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = theme.onSurfaceColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Learn to code, right from your phone • v2.4.0",
                fontSize = 13.sp,
                color = theme.lineNumberColor,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.testTag("about_tagline")
            )
        }

        // Distinctive Accent Left Bar Learner Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("student_mission_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Distinctive solid accent left bar
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(theme.primaryColor)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = theme.primaryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Your first line of code starts here.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = theme.primaryColor
                        )
                    }

                    Text(
                        text = "Welcome to Linq Studio! Learning to program should be welcoming, accessible, and exciting for everyone. Whether you are exploring your very first Python loop, writing a simple webpage, or tinkering with Java, this environment was crafted to help you learn and practice right from your phone. Don't be afraid to make mistakes—every bug you encounter is simply a puzzle waiting to be solved.",
                        color = theme.onSurfaceColor.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Developer Attribution Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier.fillMaxWidth().testTag("developer_attribution_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(theme.primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Ljhay",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = theme.onSurfaceColor
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Creator",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "Creator & Developer",
                            fontSize = 11.sp,
                            color = theme.primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Built with care by Ljhay to give students everywhere an encouraging, portable place to study and practice programming.",
                    color = theme.onSurfaceColor,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // What You Can Do
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier.fillMaxWidth().testTag("what_you_can_do_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "What You Can Do",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = theme.onSurfaceColor
                )

                StudentFeatureItem(Icons.Default.PlayArrow, "Run Code Instantly", "Test programs in Python, JavaScript, Java, C, and SQL with live output", theme)
                StudentFeatureItem(Icons.Default.School, "Beginner Guided Tours", "Interactive step-by-step overlays explaining coding concepts and navigation", theme)
                StudentFeatureItem(Icons.Default.Code, "Starter Code & Templates", "Jumpstart your projects with ready-to-run examples in various languages", theme)
                StudentFeatureItem(Icons.Default.Language, "Live Web Testing", "Preview HTML, CSS, and JavaScript with the built-in local test browser", theme)
                StudentFeatureItem(Icons.Default.Palette, "10 Beautiful Themes", "Choose Linq Light for crisp daytime study, or Dark themes for nighttime coding", theme)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StudentFeatureItem(icon: ImageVector, title: String, subtitle: String, theme: EditorTheme) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = theme.primaryColor.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
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
