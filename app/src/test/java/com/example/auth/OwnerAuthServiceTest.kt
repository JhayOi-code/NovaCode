package com.example.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerAuthServiceTest {

    @Test
    fun testPresenceTrackingAndHeartbeat() {
        OwnerAuthService.recordHeartbeat("sess-1", "user-1")
        OwnerAuthService.recordHeartbeat("sess-2", "user-2")
        // Two unique users
        assertEquals(2, OwnerAuthService.activeUsersCount.value)

        // Same user on another session should deduplicate
        OwnerAuthService.recordHeartbeat("sess-3", "user-1")
        assertEquals(2, OwnerAuthService.activeUsersCount.value)

        // Remove session
        OwnerAuthService.removePresence("sess-2")
        assertEquals(1, OwnerAuthService.activeUsersCount.value)
    }

    @Test
    fun testSmtpConfiguredCheck() {
        // Without environment variables set, isConfigured returns false
        assertFalse(SmtpEmailSender.isConfigured())
    }
}
