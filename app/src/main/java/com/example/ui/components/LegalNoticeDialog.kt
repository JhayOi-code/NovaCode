package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EditorTheme

@Composable
fun LegalNoticeDialog(
    theme: EditorTheme,
    initialTab: String = "privacy", // "privacy" or "terms"
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(initialTab) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = theme.surfaceColor,
            border = BorderStroke(1.dp, theme.borderColor),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(16.dp)
                .testTag("legal_notice_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Tabs and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeTab == "privacy") theme.primaryColor else theme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { activeTab = "privacy" }
                                .testTag("tab_legal_privacy")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (activeTab == "privacy") Color.White else theme.lineNumberColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Privacy Policy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (activeTab == "privacy") Color.White else theme.onSurfaceColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeTab == "terms") theme.primaryColor else theme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { activeTab = "terms" }
                                .testTag("tab_legal_terms")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = if (activeTab == "terms") Color.White else theme.lineNumberColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Terms of Service",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (activeTab == "terms") Color.White else theme.onSurfaceColor
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("btn_close_legal_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor)
                    }
                }

                HorizontalDivider(
                    color = theme.borderColor,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (activeTab == "privacy") {
                        Text(
                            text = "Linq Studio Privacy & Data Protection Policy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = theme.onSurfaceColor
                        )
                        Text(
                            text = "Effective Date: November 2026 • Zero Telemetry without explicit user opt-in",
                            fontSize = 12.sp,
                            color = theme.lineNumberColor
                        )

                        Text(
                            text = "1. Security & Data Protection Principles",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "Linq Studio strictly adheres to privacy-by-design. Your source code and projects are saved exclusively to your authenticated user account. All passwords are protected using industry-standard salted PBKDF2 hashing. Web sessions use secure, HttpOnly, SameSite=Strict cookies to prevent cross-site request forgery.",
                            fontSize = 12.5.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "2. Services Data Transmission Disclosures",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "Linq Studio communicates exclusively with the following vetted services to provide development and authentication features:",
                            fontSize = 12.5.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f)
                        )

                        val services = listOf(
                            Triple("1. Google Identity / Firebase Authentication", "Purpose: Account authentication and verification", "Data Sent: OAuth ID token, email address, password hash"),
                            Triple("2. Google Cloud Firestore", "Purpose: Cloud sync of workspace files and settings", "Data Sent: Encrypted project files, locked strictly to user UID"),
                            Triple("3. Google Gemini API / Firebase AI Logic", "Purpose: Optional AI code explanation and debugging", "Data Sent: Explicitly submitted code snippets. Zero training on user code"),
                            Triple("4. Linq SMTP Relay (Nodemailer)", "Purpose: Transactional security alerts and password reset", "Data Sent: Target email address and secure one-time reset tokens"),
                            Triple("5. User BYOK AI Providers (OpenAI, Anthropic, Groq)", "Purpose: User-configured AI assistant models", "Data Sent: Prompts sent directly using developer's private API key"),
                            Triple("6. Local On-Device Sandbox Engine", "Purpose: Offline code execution for Python, Java, Kotlin, C++", "Data Sent: ZERO external transmission. Executes in isolated sandbox")
                        )

                        services.forEach { (name, purpose, data) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, theme.borderColor.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = theme.onSurfaceColor)
                                    Text(purpose, fontSize = 11.5.sp, color = theme.lineNumberColor)
                                    Text(data, fontSize = 11.5.sp, color = theme.primaryColor)
                                }
                            }
                        }

                        Text(
                            text = "3. Analytics & Telemetry Policy",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "No analytics, tracking pixels, or session replay scripts are ever loaded unless the user explicitly grants permission. Users may opt out at any time from Settings.",
                            fontSize = 12.5.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = "Linq Studio Terms of Service",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = theme.onSurfaceColor
                        )
                        Text(
                            text = "Last Revised: November 2026",
                            fontSize = 12.sp,
                            color = theme.lineNumberColor
                        )

                        Text(
                            text = "1. Age Requirement & Eligibility",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "You must be at least 13 years of age to register an account with Linq Studio. By providing your Date of Birth during sign up, you affirm that you meet this minimum age requirement.",
                            fontSize = 12.5.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "2. User Code & Intellectual Property",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "You retain 100% intellectual property ownership of all source code, software, and projects you write or build in Linq Studio. Linq Studio does not claim any ownership rights over your creations.",
                            fontSize = 12.5.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "3. Acceptable Use & Account Security",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = theme.primaryColor
                        )
                        Text(
                            text = "You are responsible for maintaining the confidentiality of your Google account credentials. You agree not to use Linq Studio's runners or API endpoints to generate malware, distribute malicious code, or engage in unauthorized network attacks.",
                            fontSize = 12.5.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }

                HorizontalDivider(
                    color = theme.borderColor,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_dismiss_legal_dialog")
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
