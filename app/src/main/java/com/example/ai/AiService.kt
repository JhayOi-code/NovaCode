package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiService(private val keyStore: AiKeyStore) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun testConnection(settings: AiSettings): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = keyStore.getApiKey(settings.provider)
            ?: return@withContext Result.failure(Exception("No API key configured for ${settings.provider.displayName}"))

        try {
            when (settings.provider) {
                AiProvider.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/${settings.selectedModel}:generateContent?key=$apiKey"
                    val json = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply { put("text", "Hello, reply with 'OK'") })
                                })
                            })
                        })
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder().url(url).post(body).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Connected successfully to Gemini (${settings.selectedModel})")
                    } else {
                        Result.failure(Exception("Gemini error ${response.code}: ${response.body?.string()}"))
                    }
                }
                AiProvider.OPENAI, AiProvider.OPENROUTER, AiProvider.CUSTOM -> {
                    val url = when (settings.provider) {
                        AiProvider.OPENAI -> "https://api.openai.com/v1/chat/completions"
                        AiProvider.OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions"
                        else -> settings.customEndpointUrl
                    }
                    val json = JSONObject().apply {
                        put("model", settings.selectedModel)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "ping")
                            })
                        })
                        put("max_tokens", 10)
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val builder = Request.Builder().url(url).post(body)
                        .addHeader("Authorization", "Bearer $apiKey")
                    if (settings.provider == AiProvider.OPENROUTER) {
                        builder.addHeader("HTTP-Referer", "https://linqstudio.app")
                        builder.addHeader("X-Title", "Linq Studio Mobile IDE")
                    }
                    val response = client.newCall(builder.build()).execute()
                    if (response.isSuccessful) {
                        Result.success("Connected successfully to ${settings.provider.displayName} (${settings.selectedModel})")
                    } else {
                        Result.failure(Exception("HTTP ${response.code}: ${response.body?.string()}"))
                    }
                }
                AiProvider.ANTHROPIC -> {
                    val url = "https://api.anthropic.com/v1/messages"
                    val json = JSONObject().apply {
                        put("model", settings.selectedModel)
                        put("max_tokens", 10)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "ping")
                            })
                        })
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        Result.success("Connected successfully to Anthropic (${settings.selectedModel})")
                    } else {
                        Result.failure(Exception("Anthropic error ${response.code}: ${response.body?.string()}"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamChatResponse(
        settings: AiSettings,
        history: List<AiChatMessage>,
        systemPrompt: String = "You are Linq AI, an expert mobile coding assistant. Provide clear, direct code solutions and explain concisely. Always format code using markdown code fences with the language specified."
    ): Flow<String> = flow {
        val apiKey = keyStore.getApiKey(settings.provider)
        if (apiKey == null) {
            emit("Error: No API key found for ${settings.provider.displayName}. Please configure your API key in Settings -> AI Assistant.")
            return@flow
        }

        try {
            when (settings.provider) {
                AiProvider.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/${settings.selectedModel}:generateContent?key=$apiKey"
                    val contentsArray = JSONArray()
                    history.forEach { msg ->
                        val role = if (msg.role == AiRole.USER) "user" else "model"
                        contentsArray.put(JSONObject().apply {
                            put("role", role)
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", msg.content) })
                            })
                        })
                    }
                    val json = JSONObject().apply {
                        put("contents", contentsArray)
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemPrompt) })
                            })
                        })
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder().url(url).post(body).build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        emit("Error: ${response.code} ${response.message}\n${response.body?.string()}")
                        return@flow
                    }
                    val respObj = JSONObject(response.body?.string() ?: "{}")
                    val candidates = respObj.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val contentObj = firstCandidate?.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text", "") ?: "No response received"
                    emit(text)
                }
                AiProvider.ANTHROPIC -> {
                    val url = "https://api.anthropic.com/v1/messages"
                    val msgsArray = JSONArray()
                    history.filter { it.role != AiRole.SYSTEM }.forEach { msg ->
                        msgsArray.put(JSONObject().apply {
                            put("role", if (msg.role == AiRole.USER) "user" else "assistant")
                            put("content", msg.content)
                        })
                    }
                    val json = JSONObject().apply {
                        put("model", settings.selectedModel)
                        put("max_tokens", settings.maxTokens)
                        put("system", systemPrompt)
                        put("messages", msgsArray)
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        emit("Anthropic Error ${response.code}: ${response.body?.string()}")
                        return@flow
                    }
                    val respObj = JSONObject(response.body?.string() ?: "{}")
                    val contentArr = respObj.optJSONArray("content")
                    val text = contentArr?.optJSONObject(0)?.optString("text", "") ?: "No response"
                    emit(text)
                }
                else -> {
                    val url = if (settings.provider == AiProvider.OPENAI) "https://api.openai.com/v1/chat/completions"
                    else if (settings.provider == AiProvider.OPENROUTER) "https://openrouter.ai/api/v1/chat/completions"
                    else settings.customEndpointUrl

                    val msgsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                    }
                    history.forEach { msg ->
                        msgsArray.put(JSONObject().apply {
                            put("role", if (msg.role == AiRole.USER) "user" else "assistant")
                            put("content", msg.content)
                        })
                    }
                    val json = JSONObject().apply {
                        put("model", settings.selectedModel)
                        put("messages", msgsArray)
                        put("temperature", settings.temperature.toDouble())
                    }
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val reqBuilder = Request.Builder().url(url).post(body)
                        .addHeader("Authorization", "Bearer $apiKey")
                    val response = client.newCall(reqBuilder.build()).execute()
                    if (!response.isSuccessful) {
                        emit("Error ${response.code}: ${response.body?.string()}")
                        return@flow
                    }
                    val respObj = JSONObject(response.body?.string() ?: "{}")
                    val choices = respObj.optJSONArray("choices")
                    val choice = choices?.optJSONObject(0)
                    val message = choice?.optJSONObject("message")
                    val text = message?.optString("content", "") ?: "No response"
                    emit(text)
                }
            }
        } catch (e: Exception) {
            emit("Request failed: ${e.localizedMessage ?: e.message}")
        }
    }.flowOn(Dispatchers.IO)
}
