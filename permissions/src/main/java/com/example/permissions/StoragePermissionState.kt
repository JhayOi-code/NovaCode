package com.example.permissions

import android.net.Uri

/**
 * Snapshot of the device storage permissions across all access tiers.
 */
data class StoragePermissionState(
    val tier1SandboxGranted: Boolean = true,
    val tier2SafGranted: Boolean = false,
    val grantedSafUris: List<Uri> = emptyList(),
    val tier3AllFilesGranted: Boolean = false,
    val legacyStorageGranted: Boolean = false,
    val isAndroid11OrAbove: Boolean = false,
    val activeStoragePath: String = ""
) {
    /**
     * True if the app has unrestricted POSIX file access across external storage.
     */
    val hasFullDeviceAccess: Boolean
        get() = tier3AllFilesGranted || (legacyStorageGranted && !isAndroid11OrAbove)

    /**
     * True if either All Files Access or at least one SAF tree directory has been granted.
     */
    val hasStorageAccess: Boolean
        get() = hasFullDeviceAccess || tier2SafGranted
}
