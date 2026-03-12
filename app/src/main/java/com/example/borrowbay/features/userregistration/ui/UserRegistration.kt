package com.example.borrowbay.features.userregistration.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.borrowbay.features.userregistration.viewmodel.RegistrationStep
import com.example.borrowbay.features.userregistration.viewmodel.UserRegistrationViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationScreen(
    onBackClick: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: UserRegistrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Handle System Back Button
    BackHandler {
        if (!viewModel.previousStep()) {
            onBackClick()
        }
    }
    
    var tempPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.updateAvatarUri(uri.toString())
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) viewModel.updateAvatarUri(tempPhotoUri.toString())
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
                            if (!viewModel.previousStep()) onBackClick()
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
                        val isCompleted = uiState.currentStep.stepNumber > step.stepNumber
                        val isCurrent = uiState.currentStep.stepNumber == step.stepNumber
                        
                        LinearProgressIndicator(
                            progress = { if (isCompleted || isCurrent) 1f else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isCurrent) Color(0xFF0066FF) else if (isCompleted) Color(0xFF38A169) else Color(0xFFE2E8F0),
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
                        val text = if (uiState.currentStep == RegistrationStep.RAZORPAY) "Finish Setup" else "Continue"
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
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
                            RegistrationStep.LOCATION -> "Where are you located?"
                            RegistrationStep.RAZORPAY -> "Setup Payments"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A202C)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = when (step) {
                            RegistrationStep.PROFILE_PICTURE -> "Upload a photo or we'll use your initials."
                            RegistrationStep.LOCATION -> "Set your primary location for rentals."
                            RegistrationStep.RAZORPAY -> "Connect Razorpay to start earning from listings (Optional)."
                            else -> "This helps build trust in our community."
                        },
                        fontSize = 16.sp,
                        color = Color(0xFF718096)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    when (step) {
                        RegistrationStep.PROFILE_PICTURE -> {
                            ProfilePictureStep(
                                avatarUri = uiState.avatarUri,
                                initials = if (uiState.name.isNotBlank()) uiState.name.take(1).uppercase() else uiState.email.take(1).uppercase(),
                                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                                onCameraClick = {
                                    val photoFile = File(context.cacheDir, "temp_profile.jpg")
                                    if (photoFile.exists()) photoFile.delete()
                                    photoFile.createNewFile()
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            )
                        }
                        RegistrationStep.NAME -> {
                            RegistrationTextField("Full Name", uiState.name, { viewModel.updateName(it) }, "e.g., John Doe", true)
                        }
                        RegistrationStep.EMAIL -> {
                            RegistrationTextField("Email Address", uiState.email, { viewModel.updateEmail(it) }, "e.g., john@example.com", true, KeyboardType.Email)
                        }
                        RegistrationStep.PHONE -> {
                            RegistrationTextField("Phone Number", uiState.phone, { viewModel.updatePhone(it) }, "e.g., +91 9876543210", true, KeyboardType.Phone)
                        }
                        RegistrationStep.LOCATION -> {
                            LocationSelectionStep(
                                locationName = uiState.locationName,
                                latitude = uiState.latitude,
                                longitude = uiState.longitude,
                                onLocationSelected = { lat, lng, name, addr ->
                                    viewModel.updateLocation(lat, lng, name, addr)
                                }
                            )
                        }
                        RegistrationStep.RAZORPAY -> {
                            RazorpaySetupStep(
                                razorpayId = uiState.razorpayId,
                                onIdChange = { viewModel.updateRazorpayId(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSelectionStep(
    locationName: String,
    latitude: Double?,
    longitude: Double?,
    onLocationSelected: (Double, Double, String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitude ?: 20.5937, longitude ?: 78.9629), if (latitude != null) 15f else 5f)
    }
    
    val markerState = rememberMarkerState(position = LatLng(latitude ?: 0.0, longitude ?: 0.0))

    fun updateFromLocation(lat: Double, lng: Double) {
        scope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addressList = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }
                val addr = addressList?.firstOrNull()
                if (addr != null) {
                    val name = addr.locality ?: addr.subAdminArea ?: "Unknown Location"
                    val fullAddr = addr.getAddressLine(0) ?: ""
                    onLocationSelected(lat, lng, name, fullAddr)
                    markerState.position = LatLng(lat, lng)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 15f)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { loc ->
                loc?.let { updateFromLocation(it.latitude, it.longitude) }
            }
        }
    }

    Column {
        OutlinedTextField(
            value = locationName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Selected Location") },
            placeholder = { Text("Search or use current location") },
            trailingIcon = {
                IconButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { loc ->
                            loc?.let { updateFromLocation(it.latitude, it.longitude) }
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }
                }) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF0066FF))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { updateFromLocation(it.latitude, it.longitude) },
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                if (latitude != null) Marker(state = markerState)
            }
        }
        Text("Tap on map to select exact location", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun RazorpaySetupStep(razorpayId: String, onIdChange: (String) -> Unit) {
    Column {
        Text("Receiving Payments", fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
        Text("To list items for rent, you need to connect a Razorpay Account.", fontSize = 14.sp, color = Color(0xFF718096))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { 
                // In real app, launch Razorpay onboarding
                onIdChange("acc_TEST_" + System.currentTimeMillis().toString().takeLast(6))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Create Razorpay Test Account")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Or enter existing Merchant ID", fontSize = 14.sp, color = Color(0xFF718096))
        OutlinedTextField(
            value = razorpayId,
            onValueChange = onIdChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("acc_Hxxxxx") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun ProfilePictureStep(
    avatarUri: String?,
    initials: String,
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
                Text(initials, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0066FF))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionCard(Icons.Default.CameraAlt, "Camera", onCameraClick, Modifier.weight(1f))
            OptionCard(Icons.Default.PhotoLibrary, "Gallery", onGalleryClick, Modifier.weight(1f))
        }
    }
}

@Composable
fun OptionCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
fun RegistrationTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, isCompulsory: Boolean, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (isCompulsory) withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
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
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF), unfocusedBorderColor = Color(0xFFE2E8F0))
        )
    }
}
