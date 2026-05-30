package com.unistay.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.models.Accommodation
import com.unistay.models.User
import java.util.*

object DataSeeder {

    fun seedData() {
        val db = FirebaseFirestore.getInstance()
        seedStudents(db)
        seedAccommodations(db)
    }

    private fun seedStudents(db: FirebaseFirestore) {
        val batch = db.batch()
        for (i in 1..50) {
            val userId = "student_$i"
            val user = User(
                userId = userId,
                email = "student$i@ub.ac.bw",
                firstName = "Student",
                lastName = i.toString(),
                role = "student",
                studentId = "ID-${1000 + i}",
                phoneNumber = "710000$i",
                isVerified = true
            )
            batch.set(db.collection("users").document(userId), user)
        }
        batch.commit().addOnSuccessListener { Log.d("DataSeeder", "50 Students seeded successfully") }
    }

    private fun seedAccommodations(db: FirebaseFirestore) {
        val locations = listOf("Gaborone West", "Broadhurst", "Tlokweng", "Mogoditshane", "Phakalane", "Block 6", "Block 9", "Village")
        val types = listOf("Ensuite", "Self-Contained", "Single", "Shared", "Studio", "Flat")
        val amenities = listOf("WiFi", "Laundry", "Parking", "Security", "AC", "Gym")
        
        val batch = db.batch()
        for (i in 1..50) {
            val id = "listing_$i"
            val price = (1500..5500).random()
            val acc = Accommodation(
                id = id,
                title = "${types.random()} Room in ${locations.random()}",
                description = "Modern and convenient student accommodation located near transport links. Includes water and electricity.",
                pricePerMonth = price,
                depositAmount = price,
                location = locations.random(),
                type = types.random(),
                amenities = amenities.shuffled().take(3),
                images = listOf("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?q=80&w=1000"),
                availableFrom = System.currentTimeMillis() + (0..30L * 24 * 60 * 60 * 1000).random(),
                providerId = "provider_1",
                providerName = "Admin Provider",
                status = "available"
            )
            batch.set(db.collection("accommodations").document(id), acc)
        }
        batch.commit().addOnSuccessListener { Log.d("DataSeeder", "50 Listings seeded successfully") }
    }
}
