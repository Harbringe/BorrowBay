package com.example.borrowbay.features.userregistration.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RegistrationStep(val stepNumber: Int) {
    PROFILE_PICTURE(1),
    NAME(2),
    EMAIL(3),
    PHONE(4),
    ADDRESS(5),
    RAZORPAY(6)
}

data class UserRegistrationUiState(
    val currentStep: RegistrationStep = RegistrationStep.PROFILE_PICTURE,
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
        if (phone.all { it.isDigit() || it == '+' || it == ' ' }) {
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

    fun nextStep() {
        val current = _uiState.value.currentStep
        val next = RegistrationStep.values().find { it.stepNumber == current.stepNumber + 1 }
        if (next != null) {
            _uiState.update { it.copy(currentStep = next) }
        }
    }

    fun previousStep(): Boolean {
        val current = _uiState.value.currentStep
        val prev = RegistrationStep.values().find { it.stepNumber == current.stepNumber - 1 }
        return if (prev != null) {
            _uiState.update { it.copy(currentStep = prev) }
            true
        } else {
            false
        }
    }

    fun canGoNext(): Boolean {
        val state = _uiState.value
        return when (state.currentStep) {
            RegistrationStep.PROFILE_PICTURE -> state.avatarUri != null
            RegistrationStep.NAME -> state.name.isNotBlank()
            RegistrationStep.EMAIL -> isValidEmail(state.email)
            RegistrationStep.PHONE -> state.phone.isNotBlank()
            RegistrationStep.ADDRESS -> state.addressLine1.isNotBlank() && state.addressLine2.isNotBlank()
            RegistrationStep.RAZORPAY -> true // Optional
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun registerUser() {
        _uiState.update { it.copy(isLoading = true) }
        // Simulate network call
        _uiState.update { it.copy(isLoading = false, isRegistrationSuccess = true) }
    }
}
