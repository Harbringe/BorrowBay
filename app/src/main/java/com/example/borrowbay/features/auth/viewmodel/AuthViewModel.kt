package com.example.borrowbay.features.auth.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borrowbay.data.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var verificationId: String? = null

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    checkUserProfile(currentUser.uid)
                } catch (e: Exception) {
                    _authState.value = AuthState.Idle
                }
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    checkUserProfile(user.uid)
                } else {
                    _authState.value = AuthState.Error("Google Sign-In failed: No user found")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign-In Error", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    checkUserProfile(user.uid)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Email Sign-In Error", e)
                if (e is FirebaseAuthInvalidUserException) {
                    // Try to sign up if user doesn't exist
                    signUpWithEmailInternal(email, password)
                } else if (e is FirebaseAuthInvalidCredentialsException) {
                    _authState.value = AuthState.Error("Invalid credentials. Check your email/password.")
                } else {
                    _authState.value = AuthState.Error(e.localizedMessage ?: "Email sign-in failed")
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            signUpWithEmailInternal(email, password)
        }
    }

    private suspend fun signUpWithEmailInternal(email: String, password: String) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                _authState.value = AuthState.NeedsRegistration
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Email Sign-Up Error", e)
            if (e is FirebaseAuthUserCollisionException) {
                // If they already have an account, try signing them in instead
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    if (result.user != null) checkUserProfile(result.user!!.uid)
                } catch (signInEx: Exception) {
                    _authState.value = AuthState.Error("Account already exists. Incorrect password.")
                }
            } else {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Email sign-up failed")
            }
        }
    }

    fun signInWithPhone(phoneNumber: String, activity: Activity) {
        if (phoneNumber.isBlank()) {
            _authState.value = AuthState.Error("Phone number cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    viewModelScope.launch {
                        try {
                            val result = auth.signInWithCredential(credential).await()
                            val user = result.user
                            if (user != null) {
                                checkUserProfile(user.uid)
                            }
                        } catch (e: Exception) {
                            _authState.value = AuthState.Error(e.localizedMessage ?: "Phone sign-in failed")
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("AuthViewModel", "Phone Verification Failed", e)
                    _authState.value = AuthState.Error(e.localizedMessage ?: "Verification failed")
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    _authState.value = AuthState.OtpSent(phoneNumber)
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    fun verifyOtp(otpCode: String) {
        val id = verificationId ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = PhoneAuthProvider.getCredential(id, otpCode)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    checkUserProfile(user.uid)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "OTP Verification Error", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Invalid OTP")
            }
        }
    }

    private suspend fun checkUserProfile(uid: String) {
        try {
            val exists = userRepository.userExists(uid)
            if (exists) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.NeedsRegistration
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Profile Check Error", e)
            _authState.value = AuthState.NeedsRegistration
        }
    }

    fun setErrorMessage(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class OtpSent(val phoneNumber: String) : AuthState()
    data object Authenticated : AuthState()
    data object NeedsRegistration : AuthState()
    data class Error(val message: String) : AuthState()
}
