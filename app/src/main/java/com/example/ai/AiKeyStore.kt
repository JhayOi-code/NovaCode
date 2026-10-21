package com.example.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AiKeyStore(private val context: Context) {

    private val keyStoreAlias = "LinqStudioAiKeyAlias"
    private val keyStoreType = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val prefs = context.getSharedPreferences("linq_ai_secure_prefs", Context.MODE_PRIVATE)

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            if (!keyStore.containsAlias(keyStoreAlias)) {
                val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreType)
                val spec = KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGen.init(spec)
                keyGen.generateKey()
            }
        } catch (_: Exception) {
            // Fallback or ignore if Keystore is restricted in sandbox
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
            keyStore.getKey(keyStoreAlias, null) as? SecretKey
        } catch (_: Exception) {
            null
        }
    }

    fun saveApiKey(provider: AiProvider, key: String) {
        val secretKey = getSecretKey()
        if (secretKey != null) {
            try {
                val cipher = Cipher.getInstance(transformation)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
                val combined = iv + encryptedBytes
                val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
                prefs.edit().putString("key_${provider.name}", encoded).apply()
                return
            } catch (_: Exception) {
                // fall through to encoded
            }
        }
        // Fallback private obfuscation
        val obfuscated = Base64.encodeToString(key.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        prefs.edit().putString("key_${provider.name}_plain", obfuscated).apply()
    }

    fun getApiKey(provider: AiProvider): String? {
        val encrypted = prefs.getString("key_${provider.name}", null)
        if (encrypted != null) {
            val secretKey = getSecretKey()
            if (secretKey != null) {
                try {
                    val combined = Base64.decode(encrypted, Base64.NO_WRAP)
                    if (combined.size > 12) {
                        val iv = combined.sliceArray(0 until 12)
                        val cipherBytes = combined.sliceArray(12 until combined.size)
                        val cipher = Cipher.getInstance(transformation)
                        val spec = GCMParameterSpec(128, iv)
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                        val decrypted = cipher.doFinal(cipherBytes)
                        return String(decrypted, Charsets.UTF_8)
                    }
                } catch (_: Exception) {
                    // fall through
                }
            }
        }
        val plain = prefs.getString("key_${provider.name}_plain", null) ?: return null
        return try {
            String(Base64.decode(plain, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    fun removeApiKey(provider: AiProvider) {
        prefs.edit()
            .remove("key_${provider.name}")
            .remove("key_${provider.name}_plain")
            .apply()
    }

    fun hasApiKey(provider: AiProvider): Boolean {
        val key = getApiKey(provider)
        return !key.isNullOrBlank()
    }

    fun validateApiKeyFormat(provider: AiProvider, key: String): Pair<Boolean, String> {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return false to "API Key cannot be empty"
        return when (provider) {
            AiProvider.ANTHROPIC -> {
                if (trimmed.startsWith("sk-ant-") && trimmed.length >= 20) true to "Valid Anthropic key format"
                else false to "Anthropic key typically starts with 'sk-ant-'"
            }
            AiProvider.OPENAI -> {
                if (trimmed.startsWith("sk-") && trimmed.length >= 20) true to "Valid OpenAI key format"
                else false to "OpenAI key typically starts with 'sk-'"
            }
            AiProvider.GEMINI -> {
                if (trimmed.startsWith("AIza") || trimmed.length >= 25) true to "Valid Gemini key format"
                else false to "Gemini key typically starts with 'AIza' or 30+ chars"
            }
            AiProvider.OPENROUTER -> {
                if (trimmed.startsWith("sk-or-") || trimmed.length >= 20) true to "Valid OpenRouter key format"
                else false to "OpenRouter key typically starts with 'sk-or-'"
            }
            AiProvider.CUSTOM -> {
                if (trimmed.length >= 4) true to "Valid key format"
                else false to "Key is too short"
            }
        }
    }
}
