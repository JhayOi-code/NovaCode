package com.example

import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UserProfileCustomizationTest {

    @Test
    fun testUserAccountDefaultValues() {
        val account = UserAccount(
            id = "test_user_1",
            name = "Alex",
            email = "alex@test.com"
        )
        assertEquals("Alex", account.name)
        assertEquals("avatar_terminal", account.avatarId)
        assertEquals("Mobile Developer", account.customTitle)
        assertNull(account.avatarCustomUri)
    }

    @Test
    fun testUserAccountProfileCustomization() {
        val original = UserAccount(
            id = "test_user_2",
            name = "Developer",
            email = "dev@test.com"
        )
        val customized = original.copy(
            name = "NeoHacker",
            customTitle = "Lead Architect",
            avatarId = "avatar_cyberpunk",
            avatarCustomUri = "content://media/external/images/media/42"
        )
        assertEquals("NeoHacker", customized.name)
        assertEquals("Lead Architect", customized.customTitle)
        assertEquals("avatar_cyberpunk", customized.avatarId)
        assertEquals("content://media/external/images/media/42", customized.avatarCustomUri)
    }

    @Test
    fun testAvatarPresetsLookup() {
        val terminalPreset = ProfileAvatarPresets.getPreset("avatar_terminal")
        assertEquals("Terminal Matrix", terminalPreset.name)
        assertEquals("Developer", terminalPreset.category)

        val cyberpunkPreset = ProfileAvatarPresets.getPreset("avatar_cyberpunk")
        assertEquals("Cyberpunk Neo", cyberpunkPreset.name)

        // Unknown preset returns fallback default preset
        val unknown = ProfileAvatarPresets.getPreset("non_existent_id")
        assertNotNull(unknown)
        assertEquals("Terminal Matrix", unknown.name)
    }
}
