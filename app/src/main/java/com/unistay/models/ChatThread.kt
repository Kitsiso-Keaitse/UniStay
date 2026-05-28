package com.unistay.models

import java.text.SimpleDateFormat
import java.util.*

data class ChatThread(
    var threadId: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantAvatars: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String = "",
    val accommodationId: String = "",
    val accommodationTitle: String = "",
    val unreadCount: Map<String, Int> = emptyMap()
) {
    fun getOtherParticipant(currentUserId: String): String? {
        return participants.firstOrNull { it != currentUserId }
    }

    fun getOtherParticipantName(currentUserId: String): String {
        val otherId = getOtherParticipant(currentUserId) ?: return "User"
        return participantNames[otherId] ?: "User"
    }

    fun getUnreadCount(userId: String): Int {
        return unreadCount[userId] ?: 0
    }

    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - lastMessageTime

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} min ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            diff < 604800000 -> "${diff / 86400000} days ago"
            else -> {
                val date = Date(lastMessageTime)
                SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
            }
        }
    }
}

