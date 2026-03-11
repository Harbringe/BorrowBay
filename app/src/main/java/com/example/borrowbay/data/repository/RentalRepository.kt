package com.example.borrowbay.data.repository

import com.example.borrowbay.data.model.Category
import com.example.borrowbay.data.model.Owner
import com.example.borrowbay.data.model.RentalItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RentalRepository {
    fun getCategories(): Flow<List<Category>> = flowOf(
        listOf(
            Category("1", "All"),
            Category("2", "Electronics"),
            Category("3", "Sports"),
            Category("4", "Tools"),
            Category("5", "Camping")
        )
    )

    fun getNearbyRentals(): Flow<List<RentalItem>> = flowOf(
        listOf(
            RentalItem("1", "Canon EOS R5", 50.0, 1.2, 4.8, "", true, Owner("o1", "John Doe")),
            RentalItem("2", "Mountain Bike", 25.0, 2.5, 4.5, "", false, Owner("o2", "Jane Smith")),
            RentalItem("3", "Drill Machine", 10.0, 0.8, 4.2, "", true, Owner("o3", "Mike Ross"))
        )
    )

    fun getTrendingRentals(): Flow<List<RentalItem>> = flowOf(
        listOf(
            RentalItem("4", "DJI Mavic Air 2", 40.0, 3.5, 4.9, "", true, Owner("o4", "Harvey Specter")),
            RentalItem("5", "Camping Tent", 15.0, 5.0, 4.7, "", true, Owner("o5", "Louis Litt")),
            RentalItem("6", "Projector", 30.0, 2.1, 4.6, "", false, Owner("o6", "Donna Paulsen"))
        )
    )
}
