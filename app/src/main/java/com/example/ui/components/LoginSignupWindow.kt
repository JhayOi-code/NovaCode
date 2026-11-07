package com.example.ui.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.auth.InAppAuthService
import com.example.model.EditorTheme
import com.example.model.UserAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GoogleAuthMode {
    LOGIN,
    SIGNUP
}

/**
 * Real in-app Google Sign-In Sheet for Linq Studio.
 * - Login mode: Email & Password
 * - Sign-up mode: Email, Password, and Username
 * - Smooth switcher: "new to the app? Sign up" / "Already have an account? Log in"
 * - Validates credentials in-app and persists account securely.
 */
@Composable
fun LoginSignupWindow(
    theme: EditorTheme,
    onSignInSuccess: (String, String) -> Unit,
    userAccount: UserAccount? = null,
    onSignOut: () -> Unit = {},
    onDismiss: () -> Unit,
    onBack: () -> Unit = onDismiss
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    var authMode by remember { mutableStateOf(GoogleAuthMode.LOGIN) }

    // Inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var technicalError by remember { mutableStateOf<String?>(null) }
    var showDetails by remember { mutableStateOf(false) }
    var copiedNotice by remember { mutableStateOf(false) }

    // Clear previous errors on open or mode switch
    LaunchedEffect(authMode) {
        errorMessage = null
        technicalError = null
        showDetails = false
    }

    LaunchedEffect(copiedNotice) {
        if (copiedNotice) {
            delay(1800)
            copiedNotice = false
        }
    }

    fun submitAuth() {
        if (isSubmitting) return
        errorMessage = null
        technicalError = null
        showDetails = false

        val cleanEmail = emailInput.trim().lowercase()
        val cleanPassword = passwordInput.trim()
        val cleanUsername = usernameInput.trim()

        if (cleanEmail.isEmpty()) {
            errorMessage = "Please enter your Google email address."
            return
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            errorMessage = "Please enter a valid Google email address."
            return
        }
        if (cleanPassword.isEmpty()) {
            errorMessage = "Please enter your password."
            return
        }

        if (authMode == GoogleAuthMode.SIGNUP && cleanUsername.isEmpty()) {
            errorMessage = "Please set a username for your profile."
            return
        }

        isSubmitting = true
        focusManager.clearFocus()

        scope.launch {
            delay(300) // Visual feedback
            val result = if (authMode == GoogleAuthMode.SIGNUP) {
                InAppAuthService.signUpWithGoogle(context, cleanUsername, cleanEmail, cleanPassword)
            } else {
                InAppAuthService.logInWithGoogle(context, cleanEmail, cleanPassword)
            }

            isSubmitting = false

            if (result.success) {
                onSignInSuccess(result.userDisplayName, result.userEmail)
                onDismiss()
            } else {
                errorMessage = result.errorMessage ?: "Authentication failed. Please check your credentials."
                technicalError = result.errorCode
            }
        }
    }

    BackHandler {
        onBack()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val isTablet = maxWidth >= 600.dp
            val windowModifier = if (isTablet) {
                Modifier
                    .widthIn(max = 480.dp)
                    .wrapContentHeight()
                    .padding(24.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * 0.94f)
                    .align(Alignment.BottomCenter)
            }

            val windowShape = if (isTablet) {
                RoundedCornerShape(24.dp)
            } else {
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            }

            Surface(
                shape = windowShape,
                color = theme.surfaceColor,
                border = BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.45f)),
                tonalElevation = 10.dp,
                modifier = windowModifier.testTag("login_signup_window")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Navigation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBack() }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .testTag("btn_back_auth")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = theme.onSurfaceColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Back",
                                color = theme.onSurfaceColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_auth_window")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = theme.lineNumberColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Google App Logo Badge
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (theme.isDark) Color(0xFF1E293B) else Color.White,
                        border = BorderStroke(1.2.dp, theme.borderColor),
                        tonalElevation = 2.dp,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (authMode == GoogleAuthMode.LOGIN) "Google Login" else "Google Sign Up",
                        color = theme.onSurfaceColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = 0.3.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (authMode == GoogleAuthMode.LOGIN) {
                            "Log in with your Google account credentials"
                        } else {
                            "Create your Google account profile to save your workspace"
                        },
                        color = theme.lineNumberColor,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // If user is currently signed in, display active profile indicator
                    if (userAccount != null && userAccount.isSignedIn) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Signed in as ${userAccount.username}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = theme.onSurfaceColor
                                    )
                                    Text(
                                        text = userAccount.email,
                                        fontSize = 11.5.sp,
                                        color = theme.lineNumberColor
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        onSignOut()
                                        onDismiss()
                                    },
                                    modifier = Modifier.testTag("btn_sign_out_current")
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign Out", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Adjacent Error Display
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF450A0A),
                            border = BorderStroke(1.2.dp, Color(0xFFDC2626)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("auth_error_container")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = errorMessage ?: "Authentication failed.",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 17.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (!technicalError.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { showDetails = !showDetails }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (showDetails) "Hide details" else "Show details",
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color(0xFFFCA5A5),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    AnimatedVisibility(visible = showDetails) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF2B0707), RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = technicalError ?: "",
                                                color = Color(0xFFFEE2E2),
                                                fontSize = 10.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 14.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        clipboard.setText(AnnotatedString(technicalError ?: ""))
                                                        copiedNotice = true
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (copiedNotice) "Copied!" else "Copy details",
                                                        color = Color(0xFFFCA5A5),
                                                        fontSize = 10.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Form Fields
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Username Field (Required on Sign Up)
                        if (authMode == GoogleAuthMode.SIGNUP) {
                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it; errorMessage = null },
                                label = { Text("Set Username", fontSize = 12.5.sp) },
                                placeholder = { Text("e.g. AlexCoder", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.primaryColor,
                                    unfocusedBorderColor = theme.borderColor,
                                    focusedContainerColor = theme.surfaceVariant.copy(alpha = 0.35f),
                                    unfocusedContainerColor = theme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_google_username")
                            )
                        }

                        // Google Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; errorMessage = null },
                            label = { Text("Google Email", fontSize = 12.5.sp) },
                            placeholder = { Text("username@gmail.com", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primaryColor,
                                unfocusedBorderColor = theme.borderColor,
                                focusedContainerColor = theme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = theme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_google_email")
                        )

                        // Google Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = null },
                            label = { Text("Password", fontSize = 12.5.sp) },
                            placeholder = { Text("Enter your password", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = theme.lineNumberColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submitAuth() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primaryColor,
                                unfocusedBorderColor = theme.borderColor,
                                focusedContainerColor = theme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = theme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_google_password")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button (Login or Sign up)
                    Button(
                        onClick = { submitAuth() },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (theme.isLight) theme.primaryColor else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag(if (authMode == GoogleAuthMode.LOGIN) "btn_google_login_submit" else "btn_google_signup_submit")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = if (theme.isLight) Color.White else Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (authMode == GoogleAuthMode.LOGIN) "Logging in with Google..." else "Signing up with Google...",
                                    color = if (theme.isLight) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (authMode == GoogleAuthMode.LOGIN) "Login" else "Sign up",
                                    color = if (theme.isLight) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle Link: "new to the app? Sign up" / "Already have an account? Log in"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authMode == GoogleAuthMode.LOGIN) "new to the app? " else "Already have an account? ",
                            color = theme.lineNumberColor,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (authMode == GoogleAuthMode.LOGIN) "Sign up" else "Log in",
                            color = theme.primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    authMode = if (authMode == GoogleAuthMode.LOGIN) GoogleAuthMode.SIGNUP else GoogleAuthMode.LOGIN
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("btn_toggle_auth_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Real Google account authentication for Linq Studio. Your credentials are used directly to sync your workspace.",
                        fontSize = 11.sp,
                        color = theme.lineNumberColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        }
    }
}
