package com.example.borrowbay.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.borrowbay.features.userregistration.ui.RazorpaySetupStep
import com.example.borrowbay.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSetupScreen(
    currentId: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var razorpayId by remember { mutableStateOf(currentId) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Payment Setup", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black) },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        shadowElevation = 1.dp,
                        modifier = Modifier.padding(start = 16.dp).size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SurfaceLight)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp, color = SurfaceLight) {
                Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                    Button(
                        onClick = { onSave(razorpayId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ocean, contentColor = OnPrimary)
                    ) {
                        Text("Save Payment Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Reusing the identical UI from the registration screen
            RazorpaySetupStep(
                razorpayId = razorpayId,
                onIdChange = { razorpayId = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Your Razorpay ID is required to receive security deposits and rental payments directly from borrowers.",
                color = MutedFgLight,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
