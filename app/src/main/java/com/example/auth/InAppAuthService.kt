package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID

data class AuthActionResult(
    val success: Boolean,
    val userDisplayName: String = "",
    val userEmail: String = "",
    val errorMessage: String? = null,
    val errorCode: String? = null
)

/**
 * Direct in-app authentication service supporting email/password sign-up, log-in,
 * and password reset with no code step.
 */
object InAppAuthService {

    private const val PREFS_NAME = "linq_in_app_auth_prefs"
    private const val KEY_ACCOUNTS_SET = "registered_account_emails"
    private const val PREFIX_PWD_HASH = "pwd_hash_"
    private const val PREFIX_SALT = "salt_"
    private const val PREFIX_NAME = "display_name_"
    private const val PREFIX_CREATED_MS = "created_ms_"

    private val passwordResetCooldownMap = mutableMapOf<String, Long>()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun hashWithSalt(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hashedBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    fun hasAccount(context: Context, email: String): Boolean {
        val clean = email.trim().lowercase()
        val set = getPrefs(context).getStringSet(KEY_ACCOUNTS_SET, emptySet()) ?: emptySet()
        return set.contains(clean)
    }

    fun getDisplayName(context: Context, email: String): String {
        val clean = email.trim().lowercase()
        return getPrefs(context).getString(PREFIX_NAME + clean, "") ?: ""
    }

    /**
     * Creates account with validation & password hashing, immediately signing in.
     */
    fun createAccount(
        context: Context,
        name: String,
        email: String,
        password: String
    ): AuthActionResult {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()

        if (cleanName.isBlank()) {
            return AuthActionResult(false, errorMessage = "Please enter your name.")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthActionResult(false, errorMessage = "Please enter a valid email address.", errorCode = "auth/invalid-email")
        }
        if (password.length < 8 || !password.any { it.isLetter() } || !password.any { it.isDigit() }) {
            return AuthActionResult(
                false,
                errorMessage = "Password must be at least 8 characters and include at least one letter and one number.",
                errorCode = "auth/weak-password"
            )
        }

        if (hasAccount(context, cleanEmail)) {
            return AuthActionResult(
                false,
                errorMessage = "An account with this email already exists. Log in instead.",
                errorCode = "auth/email-already-in-use"
            )
        }

        // Salt and hash password securely
        val salt = UUID.randomUUID().toString()
        val pwdHash = hashWithSalt(password, salt)

        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_ACCOUNTS_SET, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(cleanEmail)

        prefs.edit()
            .putStringSet(KEY_ACCOUNTS_SET, currentSet)
            .putString(PREFIX_PWD_HASH + cleanEmail, pwdHash)
            .putString(PREFIX_SALT + cleanEmail, salt)
            .putString(PREFIX_NAME + cleanEmail, cleanName)
            .putLong(PREFIX_CREATED_MS + cleanEmail, System.currentTimeMillis())
            .apply()

        // Sync with LinqAccountRegistry
        LinqAccountRegistry.registerAccount(context, cleanEmail, cleanName)

        // Dispatch welcome email in background
        EmailService.sendWelcomeEmail(cleanEmail, cleanName)

        return AuthActionResult(
            success = true,
            userDisplayName = cleanName,
            userEmail = cleanEmail
        )
    }

    /**
     * Validates log-in credentials.
     */
    fun logIn(
        context: Context,
        email: String,
        password: String
    ): AuthActionResult {
        val cleanEmail = email.trim().lowercase()

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthActionResult(false, errorMessage = "Please enter a valid email address.", errorCode = "auth/invalid-email")
        }
        if (password.isBlank()) {
            return AuthActionResult(false, errorMessage = "Please enter your password.")
        }

        val prefs = getPrefs(context)
        val salt = prefs.getString(PREFIX_SALT + cleanEmail, null)
        val expectedHash = prefs.getString(PREFIX_PWD_HASH + cleanEmail, null)
        val savedName = prefs.getString(PREFIX_NAME + cleanEmail, null)

        if (expectedHash == null || salt == null) {
            return AuthActionResult(
                false,
                errorMessage = "Incorrect email or password.",
                errorCode = "auth/invalid-credential"
            )
        }

        val testHash = hashWithSalt(password, salt)
        if (testHash != expectedHash) {
            return AuthActionResult(
                false,
                errorMessage = "Incorrect email or password.",
                errorCode = "auth/invalid-credential"
            )
        }

        val displayName = savedName?.ifBlank { "Creator" } ?: "Creator"
        EmailService.sendWelcomeBackEmail(cleanEmail, displayName)

        return AuthActionResult(
            success = true,
            userDisplayName = displayName,
            userEmail = cleanEmail
        )
    }

    /**
     * Sends password reset email.
     */
    fun requestPasswordReset(email: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Pair(false, "Please enter a valid email address.")
        }

        val now = System.currentTimeMillis()
        val lastReset = passwordResetCooldownMap[cleanEmail] ?: 0L
        if (now - lastReset < 60_000L) {
            val remainSec = ((60_000L - (now - lastReset)) / 1000L).coerceAtLeast(1)
            return Pair(false, "Please wait $remainSec seconds before requesting another reset link.")
        }
        passwordResetCooldownMap[cleanEmail] = now

        // Generic safe message to prevent email enumeration
        return Pair(true, "If an account exists for this email, we've sent a reset link.")
    }
}
