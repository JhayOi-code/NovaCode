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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.example.auth.LinqAccountRegistry
import com.example.model.EditorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN,
    SIGNUP
}

enum class AuthScreenState {
    FORM,
    FORGOT_PASSWORD
}

@Composable
fun LoginSignupWindow(
    theme: EditorTheme,
    onSignInSuccess: (String, String) -> Unit,
    userAccount: com.example.model.UserAccount? = null,
    onSignOut: () -> Unit = {},
    onDismiss: () -> Unit,
    onBack: () -> Unit = onDismiss
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var selectedTab by remember {
        val last = LinqAccountRegistry.getLastTab(context)
        mutableStateOf(if (last == "signup") AuthTab.SIGNUP else AuthTab.LOGIN)
    }

    var screenState by remember { mutableStateOf(AuthScreenState.FORM) }

    // Form fields
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Forgot password field
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordFeedback by remember { mutableStateOf<String?>(null) }
    var forgotPasswordCooldown by remember { mutableStateOf(0) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var technicalError by remember { mutableStateOf<String?>(null) }
    var noticeMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(forgotPasswordCooldown) {
        if (forgotPasswordCooldown > 0) {
            delay(1000)
            forgotPasswordCooldown -= 1
        }
    }

    fun performSignUp() {
        if (isSubmitting) return
        if (passwordInput != confirmPasswordInput) {
            errorMessage = "Passwords do not match."
            return
        }

        isSubmitting = true
        errorMessage = null
        technicalError = null

        scope.launch {
            delay(300) // Form submission feedback
            val result = InAppAuthService.createAccount(context, nameInput, emailInput, passwordInput)
            isSubmitting = false

            if (result.success) {
                onSignInSuccess(result.userDisplayName, result.userEmail)
                onDismiss()
            } else {
                errorMessage = result.errorMessage
                technicalError = result.errorCode
            }
        }
    }

    fun performLogIn() {
        if (isSubmitting) return
        isSubmitting = true
        errorMessage = null
        technicalError = null

        scope.launch {
            delay(300)
            val result = InAppAuthService.logIn(context, emailInput, passwordInput)
            isSubmitting = false

            if (result.success) {
                onSignInSuccess(result.userDisplayName, result.userEmail)
                onDismiss()
            } else {
                errorMessage = result.errorMessage
                technicalError = result.errorCode
            }
        }
    }

    fun performForgotPassword() {
        if (isSubmitting) return
        isSubmitting = true
        val (success, msg) = InAppAuthService.requestPasswordReset(forgotPasswordEmail)
        isSubmitting = false
        if (success) {
            forgotPasswordFeedback = msg
            forgotPasswordCooldown = 60
            errorMessage = null
        } else {
            errorMessage = msg
        }
    }

    BackHandler {
        if (screenState != AuthScreenState.FORM) {
            screenState = AuthScreenState.FORM
            errorMessage = null
            technicalError = null
        } else {
            onBack()
        }
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
                    .widthIn(max = 500.dp)
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
                border = BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.5f)),
                tonalElevation = 10.dp,
                modifier = windowModifier.testTag("login_signup_window")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .padding(bottom = 12.dp),
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
                                .clickable {
                                    if (screenState != AuthScreenState.FORM) {
                                        screenState = AuthScreenState.FORM
                                        errorMessage = null
                                        technicalError = null
                                    } else {
                                        onBack()
                                    }
                                }
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // App Logo
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (theme.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_linq_user_logo),
                                contentDescription = "Linq Studio Logo",
                                colorFilter = ColorFilter.tint(if (theme.isDark) Color.White else Color.Black),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome to Linq Studio",
                        color = theme.onSurfaceColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = 0.3.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Notice Message Banner
                    if (noticeMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = noticeMessage ?: "",
                                color = Color(0xFF34D399),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Error Box
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF450A0A),
                            border = BorderStroke(1.2.dp, Color(0xFFDC2626)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 17.sp
                                )
                                if (!technicalError.isNullOrBlank()) {
                                    Text(
                                        text = technicalError ?: "",
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    when (screenState) {
                        AuthScreenState.FORM -> {
                            // Segmented Tabs: "Log in" and "Sign up"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .background(theme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val isLogin = selectedTab == AuthTab.LOGIN
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isLogin) theme.surfaceColor else Color.Transparent)
                                        .clickable {
                                            if (selectedTab != AuthTab.LOGIN) {
                                                selectedTab = AuthTab.LOGIN
                                                errorMessage = null
                                                technicalError = null
                                                LinqAccountRegistry.setLastTab(context, "login")
                                            }
                                        }
                                        .testTag("tab_login"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Log in",
                                        fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLogin) theme.onSurfaceColor else theme.lineNumberColor,
                                        fontSize = 14.sp
                                    )
                                }

                                val isSignUp = selectedTab == AuthTab.SIGNUP
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSignUp) theme.surfaceColor else Color.Transparent)
                                        .clickable {
                                            if (selectedTab != AuthTab.SIGNUP) {
                                                selectedTab = AuthTab.SIGNUP
                                                errorMessage = null
                                                technicalError = null
                                                LinqAccountRegistry.setLastTab(context, "signup")
                                            }
                                        }
                                        .testTag("tab_signup"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Sign up",
                                        fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSignUp) theme.onSurfaceColor else theme.lineNumberColor,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (selectedTab == AuthTab.SIGNUP) {
                                // SIGN UP FORM
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it; errorMessage = null },
                                    label = { Text("Full Name", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = theme.primaryColor) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_name")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it; errorMessage = null },
                                    label = { Text("Email Address", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = theme.primaryColor) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_email")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it; errorMessage = null },
                                    label = { Text("Password", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryColor) },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle password visibility")
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_password")
                                )

                                // Password Strength Live Hint
                                val isLongEnough = passwordInput.length >= 8
                                val hasLetter = passwordInput.any { it.isLetter() }
                                val hasDigit = passwordInput.any { it.isDigit() }
                                val isStrong = isLongEnough && hasLetter && hasDigit

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Min 8 chars, 1 letter & 1 number",
                                        fontSize = 11.sp,
                                        color = if (isStrong) Color(0xFF10B981) else theme.lineNumberColor
                                    )
                                    if (passwordInput.isNotEmpty()) {
                                        Text(
                                            text = if (isStrong) "✓ Strong" else "Weak",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isStrong) Color(0xFF10B981) else Color(0xFFEF4444)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = confirmPasswordInput,
                                    onValueChange = { confirmPasswordInput = it; errorMessage = null },
                                    label = { Text("Confirm Password", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryColor) },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        performSignUp()
                                    }),
                                    modifier = Modifier.fillMaxWidth().testTag("input_signup_confirm_password")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { performSignUp() },
                                    enabled = !isSubmitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_submit_signup")
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Creating account...", color = Color.Black, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Create account", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                // LOG IN FORM
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it; errorMessage = null },
                                    label = { Text("Email Address", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = theme.primaryColor) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                    modifier = Modifier.fillMaxWidth().testTag("input_login_email")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it; errorMessage = null },
                                    label = { Text("Password", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.primaryColor) },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Toggle password visibility")
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        performLogIn()
                                    }),
                                    modifier = Modifier.fillMaxWidth().testTag("input_login_password")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Forgot password?",
                                        color = theme.primaryColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable {
                                                screenState = AuthScreenState.FORGOT_PASSWORD
                                                forgotPasswordEmail = emailInput
                                                errorMessage = null
                                            }
                                            .padding(vertical = 4.dp)
                                            .testTag("link_forgot_password")
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { performLogIn() },
                                    enabled = !isSubmitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_submit_login")
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Logging in...", color = Color.Black, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Log in", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bottom Tab Switcher Link
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (selectedTab == AuthTab.LOGIN) "New here? " else "Already have an account? ",
                                    color = theme.lineNumberColor,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (selectedTab == AuthTab.LOGIN) "Sign up" else "Log in",
                                    color = theme.primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.clickable {
                                        selectedTab = if (selectedTab == AuthTab.LOGIN) AuthTab.SIGNUP else AuthTab.LOGIN
                                        errorMessage = null
                                        technicalError = null
                                    }
                                )
                            }
                        }



                        AuthScreenState.FORGOT_PASSWORD -> {
                            // FORGOT PASSWORD SCREEN
                            Text(
                                text = "Reset Password",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter your email address and we'll send you a password reset link.",
                                color = theme.lineNumberColor,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = forgotPasswordEmail,
                                onValueChange = { forgotPasswordEmail = it; errorMessage = null },
                                label = { Text("Email Address", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = theme.primaryColor) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { performForgotPassword() }),
                                modifier = Modifier.fillMaxWidth().testTag("input_forgot_password_email")
                            )

                            if (forgotPasswordFeedback != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = forgotPasswordFeedback ?: "",
                                    color = Color(0xFF34D399),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { performForgotPassword() },
                                enabled = forgotPasswordCooldown == 0 && !isSubmitting,
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_send_reset_link")
                            ) {
                                if (forgotPasswordCooldown > 0) {
                                    Text("Wait ${forgotPasswordCooldown}s to resend", color = Color.Black, fontSize = 13.sp)
                                } else {
                                    Text("Send reset link", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Back to Log in",
                                color = theme.primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable { screenState = AuthScreenState.FORM; selectedTab = AuthTab.LOGIN; errorMessage = null }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
