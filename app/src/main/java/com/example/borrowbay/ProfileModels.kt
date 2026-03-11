package com.example.borrowbay

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)

sealed class Screen {
    object Profile : Screen()
    object Details : Screen()
    object ActiveListings : Screen()
    object RentalHistory : Screen()
}
