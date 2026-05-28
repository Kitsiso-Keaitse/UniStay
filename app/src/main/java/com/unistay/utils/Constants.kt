package com.unistay.utils

object Constants {
    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_ACCOMMODATIONS = "accommodations"
    const val COLLECTION_RESERVATIONS = "reservations"
    const val COLLECTION_CHAT_MESSAGES = "chatMessages"
    const val COLLECTION_CHAT_THREADS = "chatThreads"

    // Accommodation Status
    const val STATUS_AVAILABLE = "available"
    const val STATUS_RESERVED = "reserved"
    const val STATUS_UNAVAILABLE = "unavailable"

    // User Roles
    const val ROLE_STUDENT = "student"
    const val ROLE_PROVIDER = "provider"

    // Gaborone Locations
    val GABORONE_LOCATIONS = listOf(
        "Gaborone West",
        "Broadhurst",
        "Tlokweng",
        "Mogoditshane",
        "Old Naledi",
        "Phakalane",
        "Riverwalk",
        "Block 8",
        "Bontleng"
    )

    // Accommodation Types
    val ACCOMMODATION_TYPES = listOf(
        "Ensuite",
        "Self-Contained",
        "Single Room",
        "Shared Room",
        "Studio",
        "Flat",
        "Bachelor Flat"
    )

    // Amenities
    val AMENITIES = listOf(
        "WiFi", "Water Included", "Electricity", "Parking",
        "Security", "Furnished", "Kitchen", "Laundry",
        "Garden", "Air Conditioning", "Study Desk", "Wardrobe"
    )
}