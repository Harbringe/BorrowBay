package com.example.borrowbay.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borrowbay.data.model.RentalItem
import com.example.borrowbay.data.repository.RentalRepository
import com.example.borrowbay.data.repository.UserRepository
import com.example.borrowbay.features.profile.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val rentalRepository: RentalRepository = RentalRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "",
            phone = "",
            email = "",
            address = ""
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _currentScreen = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Profile)
    val currentScreen: StateFlow<ProfileScreenState> = _currentScreen.asStateFlow()

    private val _isLoadingListings = MutableStateFlow(false)
    val isLoadingListings: StateFlow<Boolean> = _isLoadingListings.asStateFlow()

    val userListings: StateFlow<List<RentalItem>> = _currentScreen.flatMapLatest { screen ->
        if (screen == ProfileScreenState.ActiveListings) {
            val uid = auth.currentUser?.uid
            if (uid != null) rentalRepository.getUserListings(uid) else flowOf(emptyList())
        } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRentals: StateFlow<List<RentalItem>> = _currentScreen.flatMapLatest { screen ->
        if (screen == ProfileScreenState.RentalHistory) {
            val uid = auth.currentUser?.uid
            if (uid != null) rentalRepository.getUserRentals(uid) else flowOf(emptyList())
        } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val user = userRepository.getUser(currentUser.uid)
                    if (user != null) {
                        _userProfile.value = UserProfile(
                            name = if (user.name.isNotBlank()) user.name else (currentUser.displayName ?: ""),
                            phone = user.phone ?: (currentUser.phoneNumber ?: ""),
                            email = if (user.email.isNotBlank()) user.email else (currentUser.email ?: ""),
                            address = user.address ?: "",
                            razorpayId = user.razorpayId ?: "",
                            avatarUri = user.avatarUrl ?: (currentUser.photoUrl?.toString() ?: "")
                        )
                    } else {
                        // Fallback to Firebase Auth info
                        _userProfile.value = UserProfile(
                            name = currentUser.displayName ?: "",
                            phone = currentUser.phoneNumber ?: "",
                            email = currentUser.email ?: "",
                            address = "",
                            avatarUri = currentUser.photoUrl?.toString() ?: ""
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateProfile(updatedProfile: UserProfile) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val data = mapOf(
                        "name" to updatedProfile.name,
                        "phone" to updatedProfile.phone,
                        "email" to updatedProfile.email,
                        "address" to updatedProfile.address,
                        "razorpayId" to updatedProfile.razorpayId,
                        "avatarUrl" to updatedProfile.avatarUri
                    )
                    
                    firestore.collection("users").document(currentUser.uid)
                        .update(data)
                    
                    _userProfile.value = updatedProfile
                    navigateTo(ProfileScreenState.Profile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun navigateTo(screen: ProfileScreenState) {
        _currentScreen.value = screen
    }

    fun handleBackPress(): Boolean {
        return if (_currentScreen.value != ProfileScreenState.Profile) {
            _currentScreen.value = ProfileScreenState.Profile
            true
        } else {
            false
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
}

sealed class ProfileScreenState {
    data object Profile : ProfileScreenState()
    data object Details : ProfileScreenState()
    data object ActiveListings : ProfileScreenState()
    data object RentalHistory : ProfileScreenState()
    data object PaymentSetup : ProfileScreenState()
}
