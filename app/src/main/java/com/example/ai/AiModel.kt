package com.example.ai

import java.util.UUID

enum class AiProvider(val displayName: String, val defaultModel: String, val models: List<String>) {
    ANTHROPIC("Anthropic", "claude-3-5-sonnet-20241022", listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229")),
    OPENAI("OpenAI", "gpt-4o", listOf("gpt-4o", "gpt-4o-mini", "o1-mini", "gpt-4-turbo")),
    GEMINI("Gemini", "gemini-1.5-pro", listOf("gemini-1.5-pro", "gemini-1.5-flash", "gemini-2.0-flash-exp")),
    OPENROUTER("OpenRouter", "anthropic/claude-3.5-sonnet", listOf("anthropic/claude-3.5-sonnet", "openai/gpt-4o", "meta-llama/llama-3.3-70b-instruct")),
    CUSTOM("Custom Endpoint", "custom-model", listOf("custom-model"))
}

data class AiSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val selectedModel: String = AiProvider.GEMINI.defaultModel,
    val customEndpointUrl: String = "https://api.openai.com/v1/chat/completions",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096
)

enum class AiRole {
    USER, ASSISTANT, SYSTEM
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: AiRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val proposedDiff: ProposedDiff? = null
)

data class ProposedDiff(
    val originalCode: String,
    val proposedCode: String,
    val description: String = "Suggested code modification"
)
