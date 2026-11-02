package com.example.ui.tours

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

data class TourStep(
    val title: String,
    val description: String,
    val tip: String? = null,
    val icon: ImageVector,
    val codeExample: String? = null
)

enum class TourCategory(val label: String, val icon: ImageVector) {
    APP_NAVIGATION("App Navigation", Icons.Default.School),
    CODING_CONCEPTS("Coding 101", Icons.Default.Psychology)
}

object TourContent {
    val navigationTour = listOf(
        TourStep(
            title = "The Code Canvas",
            description = "This is your creative workspace. Line numbers on the left help you track where code lives and identify where errors happen.",
            tip = "Notice syntax highlighting: keywords, strings, and numbers are colored to make reading easier.",
            icon = Icons.Default.Code
        ),
        TourStep(
            title = "The Green Run Button",
            description = "Whenever you want to test your program, tap the green Run button in the top toolbar to execute your code live.",
            tip = "You can run Python, JavaScript, Java, C, C++, and more with real output and exit codes.",
            icon = Icons.Default.PlayArrow
        ),
        TourStep(
            title = "Files & Tabs",
            description = "Easily switch between open files using the tab bar across the top. Tap the folder icon to open the project file explorer.",
            tip = "Use 'New File' to pick from starter templates with pre-configured examples ready to run.",
            icon = Icons.Default.Folder
        ),
        TourStep(
            title = "Output & Terminal Panel",
            description = "Results from your program appear in the bottom execution panel, streaming stdout and stderr in real-time.",
            tip = "You can supply user input directly into the stdin box when prompted by input() or Scanner.",
            icon = Icons.Default.Terminal
        ),
        TourStep(
            title = "Coding Assistant & Shortcuts",
            description = "Above the keyboard, the mobile helper toolbar gives you instant access to brackets {}, quotes, and autocomplete suggestions.",
            tip = "Tap the keyboard icon in the toolbar anytime to view all handy shortcuts like Ctrl+S and Ctrl+Enter.",
            icon = Icons.Default.Lightbulb
        )
    )

    val conceptsTour = listOf(
        TourStep(
            title = "Variables: Labeled Boxes",
            description = "Variables store values like player scores, names, or prices. Think of them as labeled storage containers you can change anytime.",
            tip = "Always give your variables clear, descriptive names so your code is easy to read.",
            icon = Icons.Default.Code,
            codeExample = "val studentScore = 95\nval studentName = \"Alex\""
        ),
        TourStep(
            title = "Decisions: If & Else",
            description = "Conditionals let your app make decisions. If the condition is true, the first block runs; otherwise, the alternative executes.",
            tip = "You can chain multiple checks using else if to handle various scenarios.",
            icon = Icons.Default.Psychology,
            codeExample = "if (studentScore >= 90) {\n    println(\"Grade: Excellent!\")\n} else {\n    println(\"Keep practicing!\")\n}"
        ),
        TourStep(
            title = "Loops: Repeating Tasks",
            description = "Loops repeat instructions automatically so you never have to copy and paste code over and over again.",
            tip = "For loops are great when you know how many times to repeat; while loops run while a condition stays true.",
            icon = Icons.Default.PlayArrow,
            codeExample = "for (round in 1..3) {\n    println(\"Practice round #\" + round)\n}"
        ),
        TourStep(
            title = "Functions: Reusable Recipes",
            description = "A function bundles multiple commands into a named reusable action that you can call whenever needed.",
            tip = "Functions keep your project organized into small, manageable pieces.",
            icon = Icons.Default.School,
            codeExample = "fun greetStudent(name: String) {\n    println(\"Welcome to coding, \" + name)\n}"
        ),
        TourStep(
            title = "Debugging: Errors are Clues",
            description = "Encountering an error is a normal and essential part of learning to code! Read the message and line number carefully.",
            tip = "Errors point directly to what went wrong—check for missing brackets, typos, or variable names.",
            icon = Icons.Default.Lightbulb,
            codeExample = "// Look at the line number in the output\n// and check for missing quotes or parentheses"
        )
    )
}

/**
 * Interactive step-by-step guided tour overlay for students learning coding and IDE navigation.
 */
@Composable
fun BeginnerGuidedTourOverlay(
    theme: EditorTheme,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TourCategory.APP_NAVIGATION) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val currentSteps = remember(selectedCategory) {
        when (selectedCategory) {
            TourCategory.APP_NAVIGATION -> TourContent.navigationTour
            TourCategory.CODING_CONCEPTS -> TourContent.conceptsTour
        }
    }

    val step = currentSteps.getOrElse(currentStepIndex) { currentSteps.first() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(onClick = onDismiss)
            .testTag("guided_tour_scrim"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (theme.isLight) Color(0xFFFFFFFF) else theme.surfaceColor
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .clickable(enabled = false) {}
                .testTag("guided_tour_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Tour Track Switcher + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TourCategory.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) theme.primaryColor else theme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedCategory = category
                                        currentStepIndex = 0
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = category.label,
                                        tint = if (isSelected) Color.White else theme.onSurfaceColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = category.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else theme.onSurfaceColor
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("tour_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tour",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Step content with smooth animation
                AnimatedContent(
                    targetState = Pair(selectedCategory, currentStepIndex),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tour_step_content"
                ) { (_, index) ->
                    val currentItem = currentSteps[index.coerceIn(currentSteps.indices)]
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Step Badge & Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = theme.primaryColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = currentItem.icon,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Step ${index + 1} of ${currentSteps.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = theme.primaryColor
                                )
                                Text(
                                    text = currentItem.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor
                                )
                            }
                        }

                        // Description
                        Text(
                            text = currentItem.description,
                            fontSize = 14.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            lineHeight = 21.sp
                        )

                        // Code Example if present
                        currentItem.codeExample?.let { code ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (theme.isLight) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = code,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (theme.isLight) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Helpful Tip box
                        currentItem.tip?.let { tipText ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = theme.primaryColor.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primaryColor.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = theme.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tipText,
                                        fontSize = 12.sp,
                                        color = theme.onSurfaceColor,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Progress Indicator Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentSteps.indices.forEach { dotIndex ->
                        val isCurrent = dotIndex == currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isCurrent) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) theme.primaryColor else theme.lineNumberColor.copy(alpha = 0.35f))
                        )
                    }
                }

                // Action Navigation Buttons: Back, Skip, Next / Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor),
                            modifier = Modifier.testTag("tour_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontSize = 13.sp)
                        }
                    } else {
                        Text(
                            text = "Skip Tour",
                            fontSize = 13.sp,
                            color = theme.lineNumberColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDismiss() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("tour_skip_button")
                        )
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < currentSteps.size - 1) {
                                currentStepIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("tour_next_button")
                    ) {
                        if (currentStepIndex < currentSteps.size - 1) {
                            Text("Next", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Got it",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Got it!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
