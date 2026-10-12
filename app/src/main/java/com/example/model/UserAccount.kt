package com.example.model

data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val isGoogleUser: Boolean = false,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AppAuthState {
    WELCOME,
    AUTH,
    AUTHENTICATED
}
