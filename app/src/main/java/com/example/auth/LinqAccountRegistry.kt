package com.example.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Server-side profile records registry simulation for Linq Studio.
 * Tracks user profiles (users/{uid}), last used login/signup tabs, and registration state.
 */
object LinqAccountRegistry {
    private const val PREFS_NAME = "linq_account_registry_prefs"
    private const val KEY_REGISTERED_EMAILS = "registered_emails"
    private const val KEY_LAST_TAB = "last_selected_tab"
    private const val KEY_HAS_EVER_SIGNED_IN = "has_ever_signed_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if a Linq Studio server-side profile exists for this email.
     */
    fun hasAccount(context: Context, email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val set = getPrefs(context).getStringSet(KEY_REGISTERED_EMAILS, emptySet()) ?: emptySet()
        return set.contains(cleanEmail)
    }

    /**
     * Registers a new Linq Studio account profile (users/{uid}).
     */
    fun registerAccount(context: Context, email: String, displayName: String) {
        val cleanEmail = email.trim().lowercase()
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_REGISTERED_EMAILS, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(cleanEmail)
        prefs.edit()
            .putStringSet(KEY_REGISTERED_EMAILS, currentSet)
            .putString("profile_name_$cleanEmail", displayName)
            .putBoolean(KEY_HAS_EVER_SIGNED_IN, true)
            .apply()
    }

    /**
     * Removes an unconfirmed auth user / profile record when cancelled.
     */
    fun deleteAccount(context: Context, email: String) {
        val cleanEmail = email.trim().lowercase()
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_REGISTERED_EMAILS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentSet.contains(cleanEmail)) {
            currentSet.remove(cleanEmail)
            prefs.edit()
                .putStringSet(KEY_REGISTERED_EMAILS, currentSet)
                .remove("profile_name_$cleanEmail")
                .apply()
        }
    }

    /**
     * Checks if the device has ever signed in before.
     */
    fun hasEverSignedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_EVER_SIGNED_IN, false)
    }

    /**
     * Marks that the device has completed at least one sign in.
     */
    fun setEverSignedIn(context: Context, value: Boolean = true) {
        getPrefs(context).edit().putBoolean(KEY_HAS_EVER_SIGNED_IN, value).apply()
    }

    /**
     * Returns "login" or "signup".
     * Default is "signup" on a device that has never signed in, and "login" after that.
     */
    fun getLastTab(context: Context): String {
        val prefs = getPrefs(context)
        val defaultTab = if (hasEverSignedIn(context)) "login" else "signup"
        return prefs.getString(KEY_LAST_TAB, defaultTab) ?: defaultTab
    }

    /**
     * Remembers the last tab used ("login" or "signup").
     */
    fun setLastTab(context: Context, tab: String) {
        getPrefs(context).edit().putString(KEY_LAST_TAB, tab).apply()
    }
}
