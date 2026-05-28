package com.unistay.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ChatMessage represents a single message within a chat thread
 */
data class ChatMessage(
    val messageId: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}

data class CreateChatThreadRequest(
    val studentId: String,
    val providerId: String,
    val studentName: String,
    val providerName: String,
    val accommodationId: String,
    val accommodationTitle: String,
    val initialMessage: String
)

data class ChatThreadPreview(
    val threadId: String,
    val otherParticipantId: String,
    val otherParticipantName: String,
    val otherParticipantAvatar: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val lastMessageSenderId: String,
    val accommodationTitle: String,
    val unreadCount: Int,
    val isTyping: Boolean = false
) {
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - lastMessageTime

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m"
            diff < 86400000 -> "${diff / 3600000}h"
            diff < 604800000 -> "${diff / 86400000}d"
            else -> {
                val date = Date(lastMessageTime)
                SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
            }
        }
    }
}

fun ChatThread.toPreview(currentUserId: String): ChatThreadPreview {
    val otherId = getOtherParticipant(currentUserId) ?: ""
    val otherName = getOtherParticipantName(currentUserId)
    val otherAvatar = participantAvatars[otherId] ?: ""
    val unread = getUnreadCount(currentUserId)

    return ChatThreadPreview(
        threadId = threadId,
        otherParticipantId = otherId,
        otherParticipantName = otherName,
        otherParticipantAvatar = otherAvatar,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        lastMessageSenderId = lastMessageSenderId,
        accommodationTitle = accommodationTitle,
        unreadCount = unread,
        isTyping = false
    )
}
