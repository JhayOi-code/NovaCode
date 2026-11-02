package com.example.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveUserPresence(
    val sessionId: String,
    val userId: String,
    val lastSeenTimestamp: Long,
    val platform: String = "Android Compose"
)

object OwnerAuthService {

    // Real-time Active Users Presence Store
    private val presenceMap = mutableMapOf<String, ActiveUserPresence>()
    private val _activeUsersCount = MutableStateFlow(1)
    val activeUsersCount: StateFlow<Int> = _activeUsersCount.asStateFlow()

    fun recordHeartbeat(sessionId: String, userId: String) {
        val now = System.currentTimeMillis()
        synchronized(presenceMap) {
            presenceMap[sessionId] = ActiveUserPresence(
                sessionId = sessionId,
                userId = userId,
                lastSeenTimestamp = now
            )

            // Prune entries older than 90 seconds (stale or killed apps)
            val threshold = now - 90_000L
            val iterator = presenceMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.lastSeenTimestamp < threshold) {
                    iterator.remove()
                }
            }

            // Deduplicate: same user on 2 devices counts as 1 user
            val uniqueUsers = presenceMap.values.map {
                if (it.userId.isNotEmpty() && it.userId != "guest") it.userId else it.sessionId
            }.toSet()

            _activeUsersCount.value = uniqueUsers.size.coerceAtLeast(1)
        }
    }

    fun removePresence(sessionId: String) {
        synchronized(presenceMap) {
            presenceMap.remove(sessionId)
            val uniqueUsers = presenceMap.values.map {
                if (it.userId.isNotEmpty() && it.userId != "guest") it.userId else it.sessionId
            }.toSet()
            _activeUsersCount.value = uniqueUsers.size.coerceAtLeast(1)
        }
    }

    suspend fun runEmailDiagnostics(email: String): SmtpDeliveryResult {
        return SmtpEmailSender.testConnectionAndSendDiagnostics(email)
    }
}
