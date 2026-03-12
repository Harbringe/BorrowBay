package com.example.borrowbay.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.borrowbay.SupabaseClient
// import com.example.borrowbay.data.models.Product
// import io.github.jan.supabase.gotrue.auth
// import io.github.jan.supabase.postgrest.postgrest
// import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ListingUiState {
    object Idle : ListingUiState()
    object Loading : ListingUiState()
    object Success : ListingUiState()
    object BankDetailsMissing : ListingUiState()
    data class Error(val msg: String) : ListingUiState()
}

class ProductViewModel : ViewModel() {
    // private val client = SupabaseClient.client
    
    private val _listingState = mutableStateOf<ListingUiState>(ListingUiState.Idle)
    val listingState: State<ListingUiState> = _listingState

    fun checkBankDetailsAndInitialize() {
        // Mocking behavior for UI testing
        _listingState.value = ListingUiState.Idle
    }

    fun listProduct(
        name: String,
        category: String,
        description: String,
        rentAmount: Double,
        securityDeposit: Double,
        address: String,
        lat: Double?,
        lng: Double?,
        imageByteLists: List<ByteArray>
    ) {
        viewModelScope.launch {
            _listingState.value = ListingUiState.Loading
            try {
                /*
                val currentUser = client.auth.currentUserOrNull()
                
                if (currentUser != null) {
                    val imageUrls = mutableListOf<String>()
                    val bucket = client.storage.from("products")
                    
                    imageByteLists.forEach { bytes ->
                        val fileName = "${UUID.randomUUID()}.jpg"
                        bucket.upload(fileName, bytes)
                        imageUrls.add(bucket.publicUrl(fileName))
                    }

                    val product = Product(
                        name = name,
                        category = category,
                        description = description,
                        rentAmount = rentAmount,
                        securityDeposit = securityDeposit,
                        imageUrls = imageUrls,
                        address = address,
                        latitude = lat,
                        longitude = lng,
                        sellerId = currentUser.id,
                        sellerEmail = currentUser.email ?: ""
                    )

                    client.postgrest["products"].insert(product)
                }
                */
                // Simulate network delay
                kotlinx.coroutines.delay(1500)
                _listingState.value = ListingUiState.Success

            } catch (e: Exception) {
                _listingState.value = ListingUiState.Error(e.message ?: "Failed to list product")
            }
        }
    }

    fun resetListingState() {
        _listingState.value = ListingUiState.Idle
    }
}
