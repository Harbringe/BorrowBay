package com.example.borrowbay

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun ProfileApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Profile) }
    
    var userProfile by remember { 
        mutableStateOf(
            UserProfile(
                name = "Mourya",
                phone = "+91 87884838383",
                email = "mourya@example.com",
                address = "Navi Mumbai,Mumbai"
            )
        )
    }

    BackHandler(enabled = currentScreen != Screen.Profile) {
        currentScreen = Screen.Profile
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            is Screen.Profile -> {
                ProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    profile = userProfile,
                    onProfileClick = { currentScreen = Screen.Details },
                    onActiveListingsClick = { currentScreen = Screen.ActiveListings },
                    onRentalHistoryClick = { currentScreen = Screen.RentalHistory }
                )
            }
            is Screen.Details -> {
                DetailsScreen(
                    modifier = Modifier.padding(innerPadding),
                    profile = userProfile,
                    onSave = { updatedProfile ->
                        userProfile = updatedProfile
                        currentScreen = Screen.Profile
                    },
                    onBack = { currentScreen = Screen.Profile }
                )
            }
            is Screen.ActiveListings -> {
                ActiveListingsScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentScreen = Screen.Profile }
                )
            }
            is Screen.RentalHistory -> {
                RentalHistoryScreen(
                    modifier = Modifier.padding(innerPadding),
                    onBack = { currentScreen = Screen.Profile }
                )
            }
        }
    }
}
