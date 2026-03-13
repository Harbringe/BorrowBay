package com.example.borrowbay.features.productdetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.borrowbay.data.repository.RentalRepository
import com.example.borrowbay.data.repository.UserRepository

class ProductDetailViewModelFactory(
    private val productId: String,
    private val rentalRepository: RentalRepository = RentalRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            return ProductDetailViewModel(productId, rentalRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
