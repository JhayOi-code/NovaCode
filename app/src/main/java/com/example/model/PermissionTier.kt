package com.example.model

enum class PermissionTier(
    val title: String,
    val description: String,
    val badge: String
) {
    TIER_1_APP_STORAGE(
        title = "Tier 1: App Sandbox",
        description = "Isolated internal storage. Works on all Android versions without asking for special permissions. Ideal for temporary files and local templates.",
        badge = "Zero Perms"
    ),
    TIER_2_SAF(
        title = "Tier 2: SAF (Storage Access Framework)",
        description = "Pick any specific folder or external SD card/USB OTG. Grants persistable URI read/write permissions. Works on Google Play without special declaration.",
        badge = "Google Play Ready"
    ),
    TIER_3_ALL_FILES(
        title = "Tier 3: All Files Access",
        description = "Full filesystem access across internal storage via MANAGE_EXTERNAL_STORAGE (Android 11+) or READ/WRITE_EXTERNAL_STORAGE (Android 8-10). Direct POSIX paths.",
        badge = "Full Device Speed"
    ),
    TIER_4_RESTRICTED(
        title = "Tier 4: Restricted / System (Root/Shizuku)",
        description = "Android 11+ completely blocks direct access to Android/data and Android/obb. For system internals or game modding, Shizuku or Root provides an ADB bridge.",
        badge = "Advanced"
    );
}

data class PermissionStatus(
    val tier1Granted: Boolean = true,
    val tier2Granted: Boolean = false,
    val tier2Uri: String? = null,
    val tier3Granted: Boolean = false,
    val tier4Active: Boolean = false,
    val activeStoragePath: String = ""
)
