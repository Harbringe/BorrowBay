package com.example.borrowbay.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class RentalItem(
    val id: String,
    val name: String,
    val pricePerDay: Double,
    val distanceKm: Double,
    val rating: Double,
    val imageUrl: String,
    val isAvailable: Boolean,
    val owner: Owner
)

data class Owner(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector? = null
)
