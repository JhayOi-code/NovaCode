package com.example.model

data class ProfileAvatarPreset(
    val id: String,
    val name: String,
    val category: String,
    val iconKey: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val description: String
)

object ProfileAvatarPresets {
    val presets = listOf(
        ProfileAvatarPreset(
            id = "avatar_terminal",
            name = "Terminal Wizard",
            category = "Core",
            iconKey = "terminal",
            primaryColorHex = 0xFF6366F1,
            secondaryColorHex = 0xFF4338CA,
            description = "CLI enthusiast & bash master"
        ),
        ProfileAvatarPreset(
            id = "avatar_ninja",
            name = "Code Specialist",
            category = "Security",
            iconKey = "security",
            primaryColorHex = 0xFF10B981,
            secondaryColorHex = 0xFF065F46,
            description = "Security and architecture expert"
        ),
        ProfileAvatarPreset(
            id = "avatar_cloud",
            name = "Cloud Architect",
            category = "Infra",
            iconKey = "cloud",
            primaryColorHex = 0xFF0284C7,
            secondaryColorHex = 0xFF0369A1,
            description = "Distributed systems & devops"
        ),
        ProfileAvatarPreset(
            id = "avatar_pixel",
            name = "Pixel Crafter",
            category = "UI/UX",
            iconKey = "palette",
            primaryColorHex = 0xFFEC4899,
            secondaryColorHex = 0xFFBE185D,
            description = "Jetpack Compose & frontend"
        ),
        ProfileAvatarPreset(
            id = "avatar_maverick",
            name = "Mobile Maverick",
            category = "Mobile",
            iconKey = "phone_android",
            primaryColorHex = 0xFFF59E0B,
            secondaryColorHex = 0xFFB45309,
            description = "Android native engineer"
        ),
        ProfileAvatarPreset(
            id = "avatar_kotlin",
            name = "Kotlin Champion",
            category = "Language",
            iconKey = "code",
            primaryColorHex = 0xFF8B5CF6,
            secondaryColorHex = 0xFF6D28D9,
            description = "Coroutines & Flow specialist"
        ),
        ProfileAvatarPreset(
            id = "avatar_data",
            name = "Data Alchemist",
            category = "Data",
            iconKey = "storage",
            primaryColorHex = 0xFF14B8A6,
            secondaryColorHex = 0xFF0F766E,
            description = "Local storage & caches"
        ),
        ProfileAvatarPreset(
            id = "avatar_bug",
            name = "Bug Hunter",
            category = "QA",
            iconKey = "bug_report",
            primaryColorHex = 0xFFEF4444,
            secondaryColorHex = 0xFFB91C1C,
            description = "Test suites & zero regressions"
        )
    )

    fun getById(id: String): ProfileAvatarPreset {
        return presets.find { it.id == id } ?: presets.first()
    }
}

data class UserAccount(
    val id: String = "user_default",
    val name: String = "Developer",
    val customTitle: String = "Mobile Developer",
    val avatarId: String = "avatar_terminal",
    val avatarCustomUri: String? = null,
    val isGoogleUser: Boolean = false,
    val email: String? = null
)
