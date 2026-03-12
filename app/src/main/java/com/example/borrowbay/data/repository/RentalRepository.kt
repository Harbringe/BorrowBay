package com.example.borrowbay.data.repository

import com.example.borrowbay.data.model.Category
import com.example.borrowbay.data.model.Owner
import com.example.borrowbay.data.model.RentalItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RentalRepository {
    private val fakeCategories = listOf(
        Category("1", "All", "🏷️"),
//        Category("2", "Electronics", "📷"),
//        Category("3", "Sports", "🚴"),
//        Category("4", "Tools", "🔧"),
//        Category("5", "Camping", "⛺")
    )

    private val fakeRentals = listOf(
        RentalItem(
            id = "1",
            name = "Canon EOS 90D DSLR",
            pricePerDay = 25.0,
            distance = 0.8,
            rating = 4.9,
            location = "San Francisco, CA",
            imageUrls = listOf("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?q=80&w=1000&auto=format&fit=crop"),
            isAvailable = true,
            ownerId = "o1",
            categoryId = "2",
            owner = Owner("o1", "Alex M.", null)
        ),
//        RentalItem(
//            id = "2",
//            name = "Trek Mountain Bike",
//            pricePerDay = 18.0,
//            distance = 1.2,
//            rating = 4.7,
//            location = "San Francisco, CA",
//            imageUrls = listOf("https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=1000&auto=format&fit=crop"),
//            isAvailable = true,
//            ownerId = "o2",
//            categoryId = "3",
//            owner = Owner("o2", "Sarah K.", null)
//        ),
//        RentalItem(
//            id = "3",
//            name = "DeWalt Power Drill",
//            pricePerDay = 12.0,
//            distance = 2.1,
//            rating = 4.8,
//            location = "San Francisco, CA",
//            imageUrls = listOf("https://images.unsplash.com/photo-1504148455328-c376907d081c?q=80&w=1000&auto=format&fit=crop"),
//            isAvailable = true,
//            ownerId = "o3",
//            categoryId = "4",
//            owner = Owner("o3", "Mike R.", null)
//        ),
//        RentalItem(
//            id = "4",
//            name = "4-Person Camping Tent",
//            pricePerDay = 15.0,
//            distance = 3.4,
//            rating = 4.6,
//            location = "San Francisco, CA",
//            imageUrls = listOf("https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?q=80&w=1000&auto=format&fit=crop"),
//            isAvailable = false,
//            ownerId = "o4",
//            categoryId = "5",
//            owner = Owner("o4", "Lisa T.", null)
//        )
    )

    fun getCategories(): Flow<List<Category>> = flow {
        delay(500) // Simulate network delay
        emit(fakeCategories)
    }

    fun getNearbyRentals(): Flow<List<RentalItem>> = flow {
        delay(800)
        emit(fakeRentals)
    }

    fun getTrendingRentals(): Flow<List<RentalItem>> = flow {
        delay(1000)
        emit(fakeRentals.shuffled())
    }
}
