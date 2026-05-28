package com.unistay.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.unistay.models.Accommodation
import com.unistay.models.User
import java.util.*

object DataSeedUtils {

    private val locations = listOf("Gaborone West", "Broadhurst", "Tlokweng", "Mogoditshane", "Phakalane", "Block 6", "Block 8", "Phase 2", "Village", "Extension 9")
    private val types = listOf("Ensuite", "Self-Contained", "Single", "Shared", "Studio", "Flat")
    private val amenitiesList = listOf("WiFi", "Laundry", "Parking", "Security", "AC", "Gym", "Pool", "Study Room", "Backup Water", "Electric Fence")
    
    // Diverse house placeholders
    private val roomImages = listOf(
        "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1493809842364-78817add7ffb?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1554995207-c18c203602cb?auto=format&fit=crop&w=800&q=80"
    )

    fun seedData(onComplete: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        seedStudents(db) { studentMsg ->
            seedAccommodations(db) { accMsg ->
                onComplete("$studentMsg\n$accMsg")
            }
        }
    }

    private fun seedStudents(db: FirebaseFirestore, callback: (String) -> Unit) {
        val batch = db.batch()
        for (i in 1..50) {
            val userId = "student_seed_$i"
            val user = User(
                userId = userId,
                email = "student$i@unistay.bw",
                firstName = "Student",
                lastName = "Number $i",
                role = "student",
                studentId = "ST${2024000 + i}",
                phoneNumber = "71000$i",
                profileImage = "https://i.pravatar.cc/150?u=$userId",
                isVerified = true,
                createdAt = System.currentTimeMillis() - (i * 3600000L)
            )
            val docRef = db.collection("users").document(userId)
            batch.set(docRef, user)
        }
        batch.commit().addOnSuccessListener { callback("Successfully seeded 50 students.") }
            .addOnFailureListener { callback("Student seed failed: ${it.message}") }
    }

    private fun seedAccommodations(db: FirebaseFirestore, callback: (String) -> Unit) {
        val batch = db.batch()
        val random = Random()

        for (i in 1..50) {
            val id = "acc_seed_$i"
            val price = 1200 + random.nextInt(4000)
            val deposit = price
            val location = locations[random.nextInt(locations.size)]
            val type = types[random.nextInt(types.size)]
            
            val acc = Accommodation(
                id = id,
                title = "$type in $location",
                description = "Modern and convenient $type located in $location. Features include ${amenitiesList.shuffled().take(2).joinToString(", ")}. Perfect for students.",
                pricePerMonth = price,
                depositAmount = deposit,
                location = location,
                type = type,
                amenities = amenitiesList.shuffled().take(3 + random.nextInt(4)),
                images = listOf(roomImages[random.nextInt(roomImages.size)]),
                availableFrom = System.currentTimeMillis() + (random.nextInt(60).toLong() * 24 * 60 * 60 * 1000),
                providerId = "provider_seed_${1 + (i % 10)}",
                providerName = "Landlord ${1 + (i % 10)}",
                status = "available",
                createdAt = System.currentTimeMillis() - (i * 7200000L)
            )
            val docRef = db.collection("accommodations").document(id)
            batch.set(docRef, acc)
        }
        batch.commit().addOnSuccessListener { callback("Successfully seeded 50 listings.") }
            .addOnFailureListener { callback("Listing seed failed: ${it.message}") }
    }
}