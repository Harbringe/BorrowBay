package com.example.borrowbay.features.createlisting.data

import com.example.borrowbay.core.supabase
import com.example.borrowbay.features.createlisting.model.Item
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ListingRepository {
    private val client = supabase

    suspend fun addItem(item: Item, imageBytesList: List<ByteArray>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val imageUrls = mutableListOf<String>()
            val bucket = client.storage.from("items")

            imageBytesList.forEach { bytes ->
                val fileName = "${UUID.randomUUID()}.jpg"
                bucket.upload(fileName, bytes)
                imageUrls.add(bucket.publicUrl(fileName))
            }

            val finalItem = item.copy(imageUrls = imageUrls)
            client.postgrest["items"].insert(finalItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
