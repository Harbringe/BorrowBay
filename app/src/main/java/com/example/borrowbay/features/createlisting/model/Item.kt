package com.example.borrowbay.features.createlisting.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val description: String = "",
    val pricePerDay: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val imageUrls: List<String> = emptyList(),
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val ownerId: String = "",
    val sellerEmail: String = "",
    val sellerPhone: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
