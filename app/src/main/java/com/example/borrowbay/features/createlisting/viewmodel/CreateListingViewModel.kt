package com.example.borrowbay.features.createlisting.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borrowbay.features.createlisting.data.ListingRepository
import com.example.borrowbay.features.createlisting.model.Item
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ListingUiState {
    object Idle : ListingUiState()
    object Loading : ListingUiState()
    object Success : ListingUiState()
    object BankDetailsMissing : ListingUiState()
    data class Error(val msg: String) : ListingUiState()
}

class CreateListingViewModel(
    private val repository: ListingRepository = ListingRepository()
) : ViewModel() {
    
    private val _listingState = mutableStateOf<ListingUiState>(ListingUiState.Idle)
    val listingState: State<ListingUiState> = _listingState

    fun checkBankDetailsAndInitialize() {
        _listingState.value = ListingUiState.Idle
    }

    fun listProduct(
        name: String,
        category: String,
        description: String,
        rentAmount: Double,
        securityDeposit: Double,
        address: String,
        lat: Double,
        lng: Double,
        imageByteLists: List<ByteArray>
    ) {
        viewModelScope.launch {
            _listingState.value = ListingUiState.Loading
            
            val item = Item(
                name = name,
                category = category,
                description = description,
                rentAmount = rentAmount,
                securityDeposit = securityDeposit,
                address = address,
                latitude = lat,
                longitude = lng,
                sellerId = "demo_user_id",
                sellerEmail = "demo@example.com"
            )

            val result = repository.addItem(item, imageByteLists)
            
            if (result.isSuccess) {
                _listingState.value = ListingUiState.Success
            } else {
                _listingState.value = ListingUiState.Error(result.exceptionOrNull()?.message ?: "Failed to list item")
            }
        }
    }

    fun resetListingState() {
        _listingState.value = ListingUiState.Idle
    }
}
