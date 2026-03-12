package com.example.borrowbay.data.model

data class Item(
    val id: String? = null,
    val name: String,
    val category: String,
    val description: String,
    val rentAmount: Double,
    val securityDeposit: Double,
    val imageUrls: List<String> = emptyList(),
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sellerId: String,
    val sellerEmail: String,
    val sellerPhone: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
