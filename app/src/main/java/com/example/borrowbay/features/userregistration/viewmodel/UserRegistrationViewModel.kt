package com.example.borrowbay.features.userregistration.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UserRegistrationUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val razorpayId: String = "",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistrationSuccess: Boolean = false
)

class UserRegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserRegistrationUiState())
    val uiState: StateFlow<UserRegistrationUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        if (name.all { it.isLetter() || it.isWhitespace() }) {
            _uiState.update { it.copy(name = name) }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePhone(phone: String) {
        if (phone.all { it.isDigit() || it == '+' }) {
            _uiState.update { it.copy(phone = phone) }
        }
    }

    fun updateAddressLine1(address: String) {
        _uiState.update { it.copy(addressLine1 = address) }
    }

    fun updateAddressLine2(address: String) {
        _uiState.update { it.copy(addressLine2 = address) }
    }

    fun updateRazorpayId(id: String) {
        _uiState.update { it.copy(razorpayId = id) }
    }

    fun updateAvatarUri(uri: String?) {
        _uiState.update { it.copy(avatarUri = uri) }
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        return state.name.isNotBlank() &&
                isValidEmail(state.email) &&
                state.phone.isNotBlank() &&
                state.addressLine1.isNotBlank() &&
                state.addressLine2.isNotBlank()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun registerUser() {
        // Implementation for registration logic (e.g., calling a repository)
        // For now, we'll just simulate a successful registration
        _uiState.update { it.copy(isLoading = true) }
        // Simulate network call
        _uiState.update { it.copy(isLoading = false, isRegistrationSuccess = true) }
    }
}
