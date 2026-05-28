package com.unistay.utils

import kotlin.random.Random

object PaymentSimulator {

    data class PaymentResult(
        val success: Boolean,
        val referenceNumber: String,
        val message: String
    )

    fun processPayment(amount: Int, method: String): PaymentResult {
        // Simulate payment processing
        Thread.sleep(1500) // Simulate network delay

        // Always succeeds for demo (in real app, would integrate with payment gateway)
        val reference = generateReferenceNumber()

        return PaymentResult(
            success = true,
            referenceNumber = reference,
            message = "Payment of BWP $amount via $method successful"
        )
    }

    private fun generateReferenceNumber(): String {
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val random = Random.nextInt(10000, 99999)
        return "UB-${year.toString().takeLast(2)}-$random"
    }
}