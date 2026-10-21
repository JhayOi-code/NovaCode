package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.EditorTheme
import com.example.model.UserAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AuthStep {
    SELECT_OR_CUSTOM,
    AUTHENTICATING,
    SUCCESS
}

@Composable
fun GoogleAuthDialog(
    theme: EditorTheme,
    userAccount: UserAccount,
    onSignInSuccess: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(AuthStep.SELECT_OR_CUSTOM) }
    var selectedName by remember { mutableStateOf(if (userAccount.isSignedIn) userAccount.username else "Alex Rivers") }
    var selectedEmail by remember { mutableStateOf(if (userAccount.isSignedIn) userAccount.email else "alex.rivers@gmail.com") }
    var isCustomEntry by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_auth")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = theme.surfaceColor,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.5f)),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("google_auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (userAccount.isSignedIn) "Google Account" else "Sign In with Google",
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_auth_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.lineNumberColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "auth_step_transition"
                ) { currentStep ->
                    when (currentStep) {
                        AuthStep.SELECT_OR_CUSTOM -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                if (userAccount.isSignedIn) {
                                    // Already signed in banner
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = theme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.VerifiedUser,
                                                        contentDescription = null,
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = userAccount.username,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.onSurfaceColor,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = userAccount.email,
                                                    color = theme.lineNumberColor,
                                                    fontSize = 12.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Permanently Signed In (${userAccount.authProvider})",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onSignOut()
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("auth_sign_out_button")
                                    ) {
                                        Text("Sign Out / Switch Account", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Explanatory note
                                    Text(
                                        text = "Sign in to synchronize your IDE profile, themes, and cloud repositories permanently.",
                                        color = theme.lineNumberColor,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    // Pre-filled Google Account Card
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = theme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                step = AuthStep.AUTHENTICATING
                                                scope.launch {
                                                    delay(1600)
                                                    step = AuthStep.SUCCESS
                                                    delay(1200)
                                                    onSignInSuccess(selectedName, selectedEmail)
                                                    onDismiss()
                                                }
                                            }
                                            .testTag("one_tap_google_account")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF4285F4).copy(alpha = 0.2f),
                                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4285F4)),
                                                modifier = Modifier.size(42.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = selectedName.take(1).uppercase(),
                                                        color = Color(0xFF4285F4),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = selectedName,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.onSurfaceColor,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = selectedEmail,
                                                    color = theme.lineNumberColor,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            Image(
                                                painter = painterResource(id = R.drawable.ic_google_logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // Custom input toggle
                                    if (isCustomEntry) {
                                        OutlinedTextField(
                                            value = selectedName,
                                            onValueChange = { selectedName = it },
                                            label = { Text("Google Display Name", fontSize = 12.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = theme.onSurfaceColor,
                                                unfocusedTextColor = theme.onSurfaceColor,
                                                focusedBorderColor = theme.primaryColor,
                                                unfocusedBorderColor = theme.borderColor
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("custom_google_name_input")
                                        )

                                        OutlinedTextField(
                                            value = selectedEmail,
                                            onValueChange = { selectedEmail = it },
                                            label = { Text("Google Email (@gmail.com)", fontSize = 12.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = theme.onSurfaceColor,
                                                unfocusedTextColor = theme.onSurfaceColor,
                                                focusedBorderColor = theme.primaryColor,
                                                unfocusedBorderColor = theme.borderColor
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("custom_google_email_input")
                                        )
                                    } else {
                                        Text(
                                            text = "Or enter a different Google account",
                                            color = theme.primaryColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .clickable { isCustomEntry = true }
                                                .padding(vertical = 4.dp)
                                        )
                                    }

                                    // Direct One-Tap Google Button
                                    Button(
                                        onClick = {
                                            step = AuthStep.AUTHENTICATING
                                            scope.launch {
                                                delay(1600)
                                                step = AuthStep.SUCCESS
                                                delay(1200)
                                                onSignInSuccess(selectedName, selectedEmail)
                                                onDismiss()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF1F2937)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("google_sign_in_primary_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_google_logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Continue with Google",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AuthStep.AUTHENTICATING -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = theme.primaryColor,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .scale(pulseScale)
                                    )
                                }

                                Text(
                                    text = "Connecting to Google Services...",
                                    color = theme.onSurfaceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Text(
                                    text = "Signing in as $selectedEmail",
                                    color = theme.lineNumberColor,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        AuthStep.SUCCESS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Signed In Successfully!",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )

                                Text(
                                    text = "Permanent sign-in active for $selectedName",
                                    color = theme.onSurfaceColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
