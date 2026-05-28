package com.unistay.models

data class NotificationItem(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // match, reservation, payment, message, info
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionId: String = "", // Listing ID, Reservation ID, or Thread ID
    val actionType: String = "" // listing, reservation, chat
)