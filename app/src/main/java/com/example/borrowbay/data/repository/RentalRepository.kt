package com.example.borrowbay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.example.borrowbay.data.model.Category
import com.example.borrowbay.data.model.RentalItem
import com.example.borrowbay.data.model.Owner
import com.example.borrowbay.features.home.viewmodel.SortOption
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class PaginatedResult<T>(
    val items: List<T>,
    val lastDoc: DocumentSnapshot?,
    val hasMore: Boolean
)

class RentalRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RentalRepository", "Firestore Error (Categories): ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val icon = doc.getString("icon")
                    Category(id = id, name = name, icon = icon)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    fun getNearbyRentals(
        lat: Double?,
        lng: Double?,
        category: String?,
        query: String?,
        sort: SortOption
    ): Flow<List<RentalItem>> = callbackFlow {
        var baseQuery: Query = firestore.collection("products")
        
        if (category != null) {
            baseQuery = baseQuery.whereEqualTo("categoryId", category)
        }

        val listener = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("RentalRepository", "Firestore Error (Nearby): ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            var items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(RentalItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            if (!query.isNullOrBlank()) {
                items = items.filter { it.name.contains(query, ignoreCase = true) }
            }

            // Client side distance simulation for now
            items = items.map { item ->
                val dist = if (lat != null && lng != null) {
                    // Simple mock distance
                    (Math.random() * 5)
                } else {
                    (Math.random() * 10) // Fallback distance
                }
                item.copy(distance = dist)
            }

            val sortedItems = when (sort) {
                SortOption.DISTANCE -> items.sortedBy { it.distance }
                SortOption.PRICE_LOW_HIGH -> items.sortedBy { it.pricePerDay }
                SortOption.PRICE_HIGH_LOW -> items.sortedBy { it.pricePerDay }.reversed()
                SortOption.RATING -> items.sortedByDescending { it.rating }
            }

            trySend(sortedItems.take(4))
        }
        awaitClose { listener.remove() }
    }

    suspend fun getGlobalRentals(
        category: String?,
        query: String?,
        sort: SortOption,
        limit: Long,
        lastDoc: Any?
    ): PaginatedResult<RentalItem> {
        return try {
            var baseQuery: Query = firestore.collection("products")
            
            if (category != null) {
                baseQuery = baseQuery.whereEqualTo("categoryId", category)
            }

            baseQuery = when (sort) {
                SortOption.PRICE_LOW_HIGH -> baseQuery.orderBy("pricePerDay", Query.Direction.ASCENDING)
                SortOption.PRICE_HIGH_LOW -> baseQuery.orderBy("pricePerDay", Query.Direction.DESCENDING)
                SortOption.RATING -> baseQuery.orderBy("rating", Query.Direction.DESCENDING)
                else -> baseQuery.orderBy("name")
            }

            if (lastDoc != null && lastDoc is DocumentSnapshot) {
                baseQuery = baseQuery.startAfter(lastDoc)
            }

            val snapshot = baseQuery.limit(limit).get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RentalItem::class.java)?.copy(id = doc.id)
            }
            
            val filteredItems = if (!query.isNullOrBlank()) {
                items.filter { it.name.contains(query, ignoreCase = true) }
            } else items

            PaginatedResult(
                items = filteredItems,
                lastDoc = snapshot.documents.lastOrNull(),
                hasMore = items.size >= limit
            )
        } catch (e: Exception) {
            Log.e("RentalRepository", "Firestore Error (Global): ${e.message}")
            PaginatedResult(emptyList(), null, false)
        }
    }

    suspend fun seedTestData() {
        try {
            val categories = listOf(
                Category("cat1", "Electronics", "📷"),
                Category("cat2", "Sports", "🚲"),
                Category("cat3", "Tools", "🔧"),
                Category("cat4", "Camping", "⛺")
            )

            val products = listOf(
                RentalItem(id = "p1", name = "Canon EOS 90D DSLR", description = "Great for photography", pricePerDay = 1500.0, location = "Downtown", ownerId = "o1", categoryId = "cat1", imageUrls = listOf("https://images.unsplash.com/photo-1516035069371-29a1b244cc32"), owner = Owner("o1", "Alex M.", phone = "123", email = "alex@test.com")),
                RentalItem(id = "p2", name = "Trek Mountain Bike", description = "All-terrain bike", pricePerDay = 800.0, location = "Uptown", ownerId = "o2", categoryId = "cat2", imageUrls = listOf("https://images.unsplash.com/photo-1485965120184-e220f721d03e"), owner = Owner("o2", "Sarah K.", phone = "123", email = "sarah@test.com")),
                RentalItem(id = "p3", name = "DeWalt Drill Machine", description = "High power drill", pricePerDay = 500.0, location = "Suburbs", ownerId = "o3", categoryId = "cat3", imageUrls = listOf("https://images.unsplash.com/photo-1504148455328-c376907d081c"), owner = Owner("o3", "Mike R.", phone = "123", email = "mike@test.com")),
                RentalItem(id = "p4", name = "Camping Tent (4P)", description = "Waterproof family tent", pricePerDay = 1200.0, location = "City Center", ownerId = "o4", categoryId = "cat4", imageUrls = listOf("https://images.unsplash.com/photo-1504280390367-361c6d9f38f4"), owner = Owner("o4", "Emma L.", phone = "123", email = "emma@test.com")),
                RentalItem(id = "p5", name = "Sony WH-1000XM4", description = "Noise cancelling headphones", pricePerDay = 600.0, location = "North Side", ownerId = "o1", categoryId = "cat1", imageUrls = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e"), owner = Owner("o1", "Alex M.", phone = "123", email = "alex@test.com")),
                RentalItem(id = "p6", name = "Electric Guitar", description = "Fender Stratocaster style", pricePerDay = 1000.0, location = "East Side", ownerId = "o5", categoryId = "cat1", imageUrls = listOf("https://images.unsplash.com/photo-1550291652-639a23c30f02"), owner = Owner("o5", "John D.", phone = "123", email = "john@test.com")),
                RentalItem(id = "p7", name = "Inflatable Kayak", description = "Single person kayak", pricePerDay = 2000.0, location = "West Side", ownerId = "o2", categoryId = "cat2", imageUrls = listOf("https://images.unsplash.com/photo-1544551763-46a013bb70d5"), owner = Owner("o2", "Sarah K.", phone = "123", email = "sarah@test.com")),
                RentalItem(id = "p8", name = "Lawn Mower", description = "Petrol lawn mower", pricePerDay = 900.0, location = "South Side", ownerId = "o3", categoryId = "cat3", imageUrls = listOf("https://images.unsplash.com/photo-1592842407634-929007e997a6"), owner = Owner("o3", "Mike R.", phone = "123", email = "mike@test.com")),
                RentalItem(id = "p9", name = "Projector 4K", description = "Home cinema projector", pricePerDay = 1800.0, location = "Downtown", ownerId = "o1", categoryId = "cat1", imageUrls = listOf("https://images.unsplash.com/photo-1535016120720-40c646be8960"), owner = Owner("o1", "Alex M.", phone = "123", email = "alex@test.com")),
                RentalItem(id = "p10", name = "Dumbbell Set 20kg", description = "Adjustable weight set", pricePerDay = 400.0, location = "Gym District", ownerId = "o2", categoryId = "cat2", imageUrls = listOf("https://images.unsplash.com/photo-1583454110551-21f2fa202143"), owner = Owner("o2", "Sarah K.", phone = "123", email = "sarah@test.com"))
            )

            val batch = firestore.batch()
            
            categories.forEach { cat ->
                batch.set(firestore.collection("categories").document(cat.id), cat)
            }
            
            products.forEach { prod ->
                batch.set(firestore.collection("products").document(prod.id), prod)
            }
            
            batch.commit().await()
            Log.d("RentalRepository", "Successfully seeded test data")
        } catch (e: Exception) {
            Log.e("RentalRepository", "Error seeding data: ${e.message}")
        }
    }

    fun getTrendingRentals(): Flow<List<RentalItem>> = callbackFlow {
        val listener = firestore.collection("products")
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RentalRepository", "Firestore Error (Trending): ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RentalItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }
}
