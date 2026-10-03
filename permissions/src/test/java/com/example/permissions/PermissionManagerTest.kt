package com.example.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionManagerTest {

    @Test
    fun testStoragePermissionStateDefaults() {
        val state = StoragePermissionState(
            tier1SandboxGranted = true,
            tier2SafGranted = false,
            tier3AllFilesGranted = false,
            legacyStorageGranted = false,
            isAndroid11OrAbove = true
        )

        assertTrue(state.tier1SandboxGranted)
        assertFalse(state.hasFullDeviceAccess)
        assertFalse(state.hasStorageAccess)
    }

    @Test
    fun testStoragePermissionStateFullAccess() {
        val state = StoragePermissionState(
            tier1SandboxGranted = true,
            tier2SafGranted = false,
            tier3AllFilesGranted = true,
            isAndroid11OrAbove = true
        )

        assertTrue(state.hasFullDeviceAccess)
        assertTrue(state.hasStorageAccess)
    }

    @Test
    fun testStoragePermissionStateSafAccess() {
        val state = StoragePermissionState(
            tier1SandboxGranted = true,
            tier2SafGranted = true,
            tier3AllFilesGranted = false,
            isAndroid11OrAbove = true
        )

        assertFalse(state.hasFullDeviceAccess)
        assertTrue(state.hasStorageAccess)
    }
}
