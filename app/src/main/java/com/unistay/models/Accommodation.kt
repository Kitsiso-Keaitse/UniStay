package com.unistay.models

data class Accommodation(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val pricePerMonth: Int = 0,
    val depositAmount: Int = 0,
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "", // Ensuite, Self-Contained, Single, Shared, Studio, Flat
    val amenities: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val availableFrom: Long = System.currentTimeMillis(),
    val providerId: String = "",
    val providerName: String = "",
    val status: String = "available", // available, reserved, unavailable
    val reservedBy: String = "",
    val reservedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val views: Int = 0,
    val saves: Int = 0
)

data class FilterCriteria(
    var minPrice: Int = 0,
    var maxPrice: Int = 10000,
    var locations: List<String> = emptyList(),
    var types: List<String> = emptyList(),
    var amenities: List<String> = emptyList(),
    var availableFrom: Long = 0
)