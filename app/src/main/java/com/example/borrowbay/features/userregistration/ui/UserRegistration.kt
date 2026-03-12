package com.example.borrowbay.features.userregistration.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.borrowbay.features.userregistration.viewmodel.RegistrationStep
import com.example.borrowbay.features.userregistration.viewmodel.UserRegistrationViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(
    onBackClick: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: UserRegistrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Use rememberSaveable to preserve the URI during configuration changes
    var tempPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateAvatarUri(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            viewModel.updateAvatarUri(tempPhotoUri.toString())
        }
    }

    LaunchedEffect(uiState.isRegistrationSuccess) {
        if (uiState.isRegistrationSuccess) {
            onRegistrationSuccess()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Complete Profile", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (!viewModel.previousStep()) {
                                onBackClick()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RegistrationStep.entries.forEach { step ->
                        LinearProgressIndicator(
                            progress = { if (uiState.currentStep.stepNumber >= step.stepNumber) 1f else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF0066FF),
                            trackColor = Color(0xFFE2E8F0),
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        if (uiState.currentStep == RegistrationStep.RAZORPAY) {
                            viewModel.registerUser()
                        } else {
                            viewModel.nextStep()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                    enabled = viewModel.canGoNext() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        val text = if (uiState.currentStep == RegistrationStep.RAZORPAY) "Create Profile" else "Continue"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "stepTransition"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = when (step) {
                            RegistrationStep.PROFILE_PICTURE -> "Add profile picture"
                            RegistrationStep.NAME -> "What's your name?"
                            RegistrationStep.EMAIL -> "Your email address"
                            RegistrationStep.PHONE -> "Phone number"
                            RegistrationStep.ADDRESS -> "Where do you live?"
                            RegistrationStep.RAZORPAY -> "Razorpay ID"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A202C)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = when (step) {
                            RegistrationStep.PROFILE_PICTURE -> "Upload a photo so others can recognize you."
                            RegistrationStep.RAZORPAY -> "Add your Razorpay account ID to receive payments (Optional)."
                            else -> "This information helps us build trust in the community."
                        },
                        fontSize = 16.sp,
                        color = Color(0xFF718096)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    when (step) {
                        RegistrationStep.PROFILE_PICTURE -> {
                            ProfilePictureStep(
                                avatarUri = uiState.avatarUri,
                                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                                onCameraClick = {
                                    try {
                                        // Creating file in cache directory which is mapped to 'my_cache' in file_paths.xml
                                        val photoFile = File(context.cacheDir, "temp_profile.jpg")
                                        if (photoFile.exists()) photoFile.delete()
                                        photoFile.createNewFile()
                                        
                                        val authority = "${context.packageName}.fileprovider"
                                        val uri = FileProvider.getUriForFile(context, authority, photoFile)
                                        tempPhotoUri = uri
                                        
                                        Toast.makeText(context, "Launching Camera...", Toast.LENGTH_SHORT).show()
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Camera Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }
                        RegistrationStep.NAME -> {
                            RegistrationTextField(
                                label = "Full Name",
                                value = uiState.name,
                                onValueChange = { viewModel.updateName(it) },
                                placeholder = "e.g., John Doe",
                                isCompulsory = true
                            )
                        }
                        RegistrationStep.EMAIL -> {
                            RegistrationTextField(
                                label = "Email Address",
                                value = uiState.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                placeholder = "e.g., john@example.com",
                                isCompulsory = true,
                                keyboardType = KeyboardType.Email
                            )
                        }
                        RegistrationStep.PHONE -> {
                            RegistrationTextField(
                                label = "Phone Number",
                                value = uiState.phone,
                                onValueChange = { viewModel.updatePhone(it) },
                                placeholder = "e.g., +91 92345 68336",
                                isCompulsory = true,
                                keyboardType = KeyboardType.Phone
                            )
                        }
                        RegistrationStep.ADDRESS -> {
                            RegistrationTextField(
                                label = "Address Line 1",
                                value = uiState.addressLine1,
                                onValueChange = { viewModel.updateAddressLine1(it) },
                                placeholder = "Street address, P.O. box",
                                isCompulsory = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            RegistrationTextField(
                                label = "Address Line 2",
                                value = uiState.addressLine2,
                                onValueChange = { viewModel.updateAddressLine2(it) },
                                placeholder = "Apartment, suite, unit, building, floor",
                                isCompulsory = true
                            )
                        }
                        RegistrationStep.RAZORPAY -> {
                            RegistrationTextField(
                                label = "Razorpay ID",
                                value = uiState.razorpayId,
                                onValueChange = { viewModel.updateRazorpayId(it) },
                                placeholder = "e.g., acc_Hxxxxx",
                                isCompulsory = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfilePictureStep(
    avatarUri: String?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFFF7FAFC))
                .border(2.dp, Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = Color(0xFF718096),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionCard(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                modifier = Modifier.weight(1f),
                onClick = onCameraClick
            )
            OptionCard(
                icon = Icons.Default.PhotoLibrary,
                label = "Gallery",
                modifier = Modifier.weight(1f),
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
fun OptionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF0066FF))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A202C))
        }
    }
}

@Composable
fun RegistrationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isCompulsory: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isCompulsory) {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(" *")
                    }
                }
            },
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1A202C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color(0xFF718096)) },
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0066FF),
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )
    }
}
