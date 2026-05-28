package com.unistay.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    val userId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "", // "student" or "provider"
    val studentId: String = "",
    val phoneNumber: String = "",
    val profileImage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("verified")
    @set:PropertyName("verified")
    var isVerified: Boolean = false
) {
    @Exclude
    fun getFullName(): String = "$firstName $lastName"
}