package com.example.model

data class AvatarPreset(
    val id: String,
    val name: String,
    val iconKey: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
)

data class ProfileTemplate(
    val id: String,
    val name: String,
    val title: String,
    val primaryStack: String,
    val bio: String,
    val colorHex: Long,
    val avatarId: String
)

object ProfileAvatarPresets {
    val presets = listOf(
        AvatarPreset("avatar_terminal", "Hacker Terminal", "terminal", 0xFF00E5FF, 0xFF0B132B),
        AvatarPreset("avatar_security", "Cyber Guardian", "security", 0xFF10B981, 0xFF064E3B),
        AvatarPreset("avatar_cloud", "Cloud Architect", "cloud", 0xFF38BDF8, 0xFF0C4A6E),
        AvatarPreset("avatar_code", "Algorithm Master", "code", 0xFFA855F7, 0xFF3B0764),
        AvatarPreset("avatar_phone", "Android Dev", "phone_android", 0xFF22C55E, 0xFF14532D),
        AvatarPreset("avatar_palette", "UI/UX Designer", "palette", 0xFFEC4899, 0xFF831843),
        AvatarPreset("avatar_bug", "QA Engineer", "bug_report", 0xFFF59E0B, 0xFF78350F),
        AvatarPreset("avatar_storage", "Data Engineer", "storage", 0xFF6366F1, 0xFF1E1B4B)
    )

    val templates = listOf(
        ProfileTemplate(
            id = "fullstack",
            name = "Full-Stack Dev",
            title = "Full-Stack Software Engineer",
            primaryStack = "TypeScript • Node.js • Python • React",
            bio = "Crafting end-to-end applications from database to interface.",
            colorHex = 0xFF00E5FF,
            avatarId = "avatar_terminal"
        ),
        ProfileTemplate(
            id = "android",
            name = "Android Architect",
            title = "Senior Android Engineer",
            primaryStack = "Kotlin • Jetpack Compose • Coroutines",
            bio = "Building high-performance native Android experiences in Linq Studios.",
            colorHex = 0xFF10B981,
            avatarId = "avatar_phone"
        ),
        ProfileTemplate(
            id = "security",
            name = "Cyber Specialist",
            title = "Systems & Security Engineer",
            primaryStack = "Rust • C/C++ • Linux • Termux",
            bio = "Auditing protocols, low-level algorithms, and embedded systems.",
            colorHex = 0xFFA855F7,
            avatarId = "avatar_security"
        ),
        ProfileTemplate(
            id = "ai",
            name = "AI Engineer",
            title = "Machine Learning & AI Developer",
            primaryStack = "Python • Gemini API • PyTorch • SQL",
            bio = "Developing LLM integrations and neural networks on device.",
            colorHex = 0xFFF59E0B,
            avatarId = "avatar_code"
        ),
        ProfileTemplate(
            id = "designer",
            name = "UI/UX Creator",
            title = "Design Systems & Frontend Lead",
            primaryStack = "CSS3 • Material Design • Jetpack Compose",
            bio = "Obsessed with typography, spacing, and micro-interactions.",
            colorHex = 0xFFEC4899,
            avatarId = "avatar_palette"
        )
    )

    fun getById(id: String): AvatarPreset {
        return presets.firstOrNull { it.id == id } ?: presets.first()
    }
}

data class UserAccount(
    val username: String = "linq_dev",
    val title: String = "Mobile Software Engineer",
    val email: String = "developer@linqstudio.dev",
    val avatarUrl: String? = null,
    val avatarId: String = "avatar_terminal",
    val avatarCustomUri: String? = null,
    val bio: String = "Building high-performance cross-platform software on mobile.",
    val primaryStack: String = "Kotlin • Python • JavaScript",
    val isSignedIn: Boolean = false,
    val authProvider: String = "Guest", // "Google", "Custom", or "Guest"
    val avatarColorHex: Long = 0xFF00E5FF,
    val memberSince: String = "October 2026",
    val projectsCount: Int = 12,
    val commitsCount: Int = 84
)
