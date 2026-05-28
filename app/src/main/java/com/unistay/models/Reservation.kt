package com.unistay.models

data class Reservation(
    var reservationId: String = "",
    val accommodationId: String = "",
    val accommodationTitle: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val providerId: String = "",
    val amountPaid: Int = 0,
    val depositAmount: Int = 0,
    val referenceNumber: String = "",
    val paymentMethod: String = "",
    val status: String = "pending", // pending, confirmed, cancelled
    val moveInDate: Long = 0,
    val reservedAt: Long = System.currentTimeMillis(),
    val receiptUrl: String = ""
)