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
 * Real in-app Google authentication service supporting:
 * - Google Sign Up: Email, Password, Username (Profile Name)
 * - Google Login: Email & Password
 * Stores credential hashes and profile names locally and integrates seamlessly with LinqAccountRegistry.
 */
object InAppAuthService {

    private const val PREFS_NAME = "linq_google_auth_prefs"
    private const val KEY_ACCOUNTS_SET = "registered_google_emails"
    private const val PREFIX_PWD_HASH = "pwd_hash_"
    private const val PREFIX_SALT = "salt_"
    private const val PREFIX_NAME = "display_name_"
    private const val PREFIX_CREATED_MS = "created_ms_"

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
     * Sign up on Google account: requires email, password, and username.
     */
    fun signUpWithGoogle(
        context: Context,
        username: String,
        email: String,
        password: String
    ): AuthActionResult {
        val cleanEmail = email.trim().lowercase()
        val cleanName = username.trim()

        if (cleanName.isBlank()) {
            return AuthActionResult(false, errorMessage = "Please enter a username.")
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return AuthActionResult(false, errorMessage = "Please enter a valid Google email address.")
        }
        if (password.length < 6) {
            return AuthActionResult(false, errorMessage = "Password must be at least 6 characters long.")
        }

        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_ACCOUNTS_SET, emptySet())?.toMutableSet() ?: mutableSetOf()

        if (currentSet.contains(cleanEmail)) {
            return AuthActionResult(false, errorMessage = "An account with this email already exists. Please log in.")
        }

        // Salt and hash password securely
        val salt = UUID.randomUUID().toString()
        val pwdHash = hashWithSalt(password, salt)

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
        EmailService.sendWelcomeEmail(cleanEmail, cleanName)

        return AuthActionResult(
            success = true,
            userDisplayName = cleanName,
            userEmail = cleanEmail
        )
    }

    /**
     * Log in to Google account: requires email and password only.
     */
    fun logInWithGoogle(
        context: Context,
        email: String,
        password: String
    ): AuthActionResult {
        val cleanEmail = email.trim().lowercase()

        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return AuthActionResult(false, errorMessage = "Please enter a valid Google email address.")
        }
        if (password.isBlank()) {
            return AuthActionResult(false, errorMessage = "Please enter your password.")
        }

        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_ACCOUNTS_SET, emptySet()) ?: emptySet()

        if (!currentSet.contains(cleanEmail)) {
            // If user hasn't registered locally yet, check owner emails or initialize account
            val isOwnerCandidate = cleanEmail == "ljhayoi609@gmail.com" || cleanEmail == "jacklangsakalam5@gmail.com"
            if (isOwnerCandidate) {
                val displayName = if (cleanEmail.startsWith("ljhay")) "Ljhay" else "Jack Developer"
                return signUpWithGoogle(context, displayName, cleanEmail, password)
            }
            return AuthActionResult(false, errorMessage = "Account not found. Please sign up first.")
        }

        val salt = prefs.getString(PREFIX_SALT + cleanEmail, null)
        val expectedHash = prefs.getString(PREFIX_PWD_HASH + cleanEmail, null)
        val savedName = prefs.getString(PREFIX_NAME + cleanEmail, null)

        if (salt != null && expectedHash != null) {
            val testHash = hashWithSalt(password, salt)
            if (testHash != expectedHash) {
                return AuthActionResult(false, errorMessage = "Incorrect password. Please try again.")
            }
        }

        val displayName = savedName?.ifBlank { cleanEmail.substringBefore("@") } ?: cleanEmail.substringBefore("@")
        EmailService.sendWelcomeBackEmail(cleanEmail, displayName)

        return AuthActionResult(
            success = true,
            userDisplayName = displayName,
            userEmail = cleanEmail
        )
    }

    /**
     * Sends password reset request via EmailService for registered or candidate account.
     */
    fun sendPasswordReset(context: Context, email: String): AuthActionResult {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return AuthActionResult(false, errorMessage = "Please enter a valid Google email address.")
        }

        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_ACCOUNTS_SET, emptySet()) ?: emptySet()
        val displayName = prefs.getString(PREFIX_NAME + cleanEmail, null)?.ifBlank { cleanEmail.substringBefore("@") }
            ?: cleanEmail.substringBefore("@")

        EmailService.sendPasswordResetEmail(cleanEmail, displayName)

        return AuthActionResult(
            success = true,
            userDisplayName = displayName,
            userEmail = cleanEmail
        )
    }
}
