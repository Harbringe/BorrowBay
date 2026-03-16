package com.example.borrowbay.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.borrowbay.features.profile.model.UserProfile
import com.example.borrowbay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profile: UserProfile,
    isLoading: Boolean = false,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit,
    onActiveListingsClick: () -> Unit,
    onRentalHistoryClick: () -> Unit,
    onPaymentSetupClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Header Card
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 60.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile.name.ifBlank { "User" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.email,
                            color = MutedFgLight,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onProfileClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ocean.copy(alpha = 0.1f)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Edit Profile", color = Ocean, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Profile Image overlapping
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MutedLight)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center).size(24.dp),
                                color = Ocean,
                                strokeWidth = 2.dp
                            )
                        } else if (!profile.avatarUri.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.avatarUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile",
                                modifier = Modifier.align(Alignment.Center).size(48.dp),
                                tint = MutedFgLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Section
            Text(
                text = "Activities",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MutedFgLight,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.5f))
            ) {
                Column {
                    ProfileMenuItem(
                        title = "Active Listings",
                        subtitle = "Manage items you've posted",
                        icon = Icons.Default.ListAlt,
                        onClick = onActiveListingsClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderLight.copy(alpha = 0.3f))
                    ProfileMenuItem(
                        title = "Rental History",
                        subtitle = "View your past and current rentals",
                        icon = Icons.Default.History,
                        onClick = onRentalHistoryClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BorderLight.copy(alpha = 0.3f))
                    ProfileMenuItem(
                        title = "Payment Setup",
                        subtitle = "Configure how you receive payments",
                        icon = Icons.Default.Payments,
                        onClick = onPaymentSetupClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out
            Button(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Destructive.copy(alpha = 0.2f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Destructive, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text = "Sign Out", fontWeight = FontWeight.Bold, color = Destructive)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = Ocean.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Ocean, modifier = Modifier.size(22.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = subtitle, fontSize = 12.sp, color = MutedFgLight)
        }
        
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = MutedFgLight,
            modifier = Modifier.size(20.dp)
        )
    }
}
