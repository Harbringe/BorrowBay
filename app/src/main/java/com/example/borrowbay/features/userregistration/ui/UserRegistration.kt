package com.example.borrowbay.features.userregistration.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.borrowbay.core.ui.components.LocationPickerDialog
import com.example.borrowbay.core.ui.components.PhoneInputField
import com.example.borrowbay.features.userregistration.viewmodel.RegistrationStep
import com.example.borrowbay.features.userregistration.viewmodel.UserRegistrationViewModel
import com.example.borrowbay.ui.theme.*
import org.osmdroid.config.Configuration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
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
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

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
        contract = TakePictureWithFrontCamera()
    ) { success: Boolean ->
        if (success && tempPhotoUri != null) {
            viewModel.updateAvatarUri(tempPhotoUri.toString())
        }
    }

    val launchCamera = {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("PROFILE_${timeStamp}_", ".jpg", storageDir)
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, photoFile)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("UserRegistration", "Camera Error", e)
            Toast.makeText(context, "Camera Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.isRegistrationSuccess) {
        if (uiState.isRegistrationSuccess) {
            onRegistrationSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(SurfaceLight)) {
                CenterAlignedTopAppBar(
                    title = { Text("Setup Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black) },
                    navigationIcon = {
                        Surface(
                            onClick = {
                                if (!viewModel.previousStep()) onBackClick()
                            },
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), 
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RegistrationStep.entries.forEach { step ->
                        Box(
                            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                                .background(if (step.stepNumber <= uiState.currentStep.stepNumber) Ocean else MutedLight)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp, color = SurfaceLight) {
                Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.nextStep(context)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Ocean, contentColor = OnPrimary),
                        enabled = viewModel.canGoNext() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val text = if (uiState.currentStep == RegistrationStep.RAZORPAY) "Finish Setup" else "Continue"
                                Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                                Spacer(Modifier.width(8.dp))
                                if (uiState.currentStep == RegistrationStep.RAZORPAY) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp), tint = OnPrimary)
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp), tint = OnPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(24.dp))
            AnimatedContent(targetState = uiState.currentStep, label = "StepTransition") { step ->
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    when (step) {
                        RegistrationStep.PROFILE_PICTURE -> {
                            HeaderSection("Add profile picture", "Upload a photo or we'll use your initials.")
                            Spacer(Modifier.height(32.dp))
                            ProfilePictureStep(
                                avatarUri = uiState.avatarUri,
                                initials = if (uiState.name.isNotBlank()) uiState.name.take(1).uppercase() else uiState.email.take(1).uppercase(),
                                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                                onCameraClick = {
                                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                        launchCamera()
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            )
                        }
                        RegistrationStep.NAME -> {
                            HeaderSection("What's your name?", "This helps build trust in our community.")
                            Spacer(Modifier.height(32.dp))
                            RegistrationInputField(
                                label = "Full Name",
                                value = uiState.name,
                                onValueChange = { viewModel.updateName(it) },
                                placeholder = "e.g., John Doe",
                                imeAction = ImeAction.Done
                            )
                            Text(
                                text = "${uiState.name.length}/50",
                                fontSize = 12.sp,
                                color = if (uiState.name.length >= 50) Destructive else MutedFgLight,
                                modifier = Modifier.padding(top = 4.dp).align(Alignment.End)
                            )
                        }
                        RegistrationStep.EMAIL -> {
                            HeaderSection("Your email address", "We'll use this for account notifications.")
                            Spacer(Modifier.height(32.dp))
                            RegistrationInputField(
                                label = "Email Address",
                                value = uiState.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                placeholder = "e.g., john@example.com",
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            )
                            Text(
                                text = "${uiState.email.length}/100",
                                fontSize = 12.sp,
                                color = if (uiState.email.length >= 100) Destructive else MutedFgLight,
                                modifier = Modifier.padding(top = 4.dp).align(Alignment.End)
                            )
                        }
                        RegistrationStep.PHONE -> {
                            HeaderSection("Phone number", "Required for secure communication between users.")
                            Spacer(Modifier.height(32.dp))
                            PhoneInputField(
                                phoneNumber = uiState.phone,
                                onPhoneNumberChange = { viewModel.updatePhone(it) },
                                selectedCountry = uiState.selectedCountry,
                                onCountrySelected = { viewModel.updateCountry(it) },
                                imeAction = ImeAction.Done,
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                            )
                        }
                        RegistrationStep.LOCATION -> {
                            HeaderSection("Where are you located?", "Set your primary location for rentals.")
                            Spacer(Modifier.height(32.dp))
                            LocationSelectionStep(
                                address = uiState.address,
                                latitude = uiState.latitude ?: 19.1235,
                                longitude = uiState.longitude ?: 73.0135,
                                onLocationSelected = { lat, lng, name, addr ->
                                    viewModel.updateLocation(lat, lng, name, addr)
                                }
                            )
                        }
                        RegistrationStep.RAZORPAY -> {
                            HeaderSection("Setup Payments", "Connect Razorpay to start earning from listings (Optional).")
                            Spacer(Modifier.height(32.dp))
                            RazorpaySetupStep(
                                razorpayId = uiState.razorpayId,
                                onIdChange = { viewModel.updateRazorpayId(it) }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(subtitle, color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun RegistrationInputField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String, 
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default
) {
    val focusManager = LocalFocusManager.current
    Column {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.replace("\n", "")) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = MutedFgLight) },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ocean,
                unfocusedBorderColor = BorderLight,
                unfocusedContainerColor = SurfaceLight,
                focusedContainerColor = SurfaceLight,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
fun LocationSelectionStep(
    address: String,
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double, String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Text("Primary Location", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BorderLight),
            color = SurfaceLight
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Ocean, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = address.ifBlank { "Select your location" },
                    color = if (address.isBlank()) MutedFgLight else Color.Black,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }
        }
        
        Text(
            "Setting your location helps us show you items nearby.",
            fontSize = 12.sp,
            color = MutedFgLight,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    if (showDialog) {
        LocationPickerDialog(
            initialLat = latitude,
            initialLng = longitude,
            onDismiss = { showDialog = false },
            onLocationSelected = { lat, lng, name, addr ->
                onLocationSelected(lat, lng, name, addr)
                showDialog = false
            }
        )
    }
}

@Composable
fun RazorpaySetupStep(razorpayId: String, onIdChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    Column {
        Text("Merchant ID", fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = razorpayId,
            onValueChange = { 
                if (it.length <= 20) onIdChange(it.replace("\n", "")) 
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("acc_Hxxxxx", color = MutedFgLight) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Ocean,
                unfocusedBorderColor = BorderLight,
                unfocusedContainerColor = SurfaceLight,
                focusedContainerColor = SurfaceLight,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        
        Text(
            text = "Limit: ${razorpayId.length}/20",
            fontSize = 11.sp,
            color = MutedFgLight,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MutedLight,
            onClick = { 
                onIdChange("acc_TEST_" + System.currentTimeMillis().toString().takeLast(6))
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(SurfaceLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Ocean)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Create Razorpay Test Account", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Start earning by enabling payments", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
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
                .background(SurfaceLight)
                .clickable { onGalleryClick() }
                .drawDashedBorder(MutedFgLight, 80.dp),
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
                Text(initials, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Ocean)
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
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Ocean)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}

fun Modifier.drawDashedBorder(color: Color, cornerRadius: androidx.compose.ui.unit.Dp) = this.then(
    Modifier.drawWithContent {
        drawContent()
        val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        drawRoundRect(color = color, style = stroke, cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()))
    }
)

class TakePictureWithFrontCamera : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super.createIntent(context, input).apply {
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            putExtra("camerafacing", "front")
            putExtra("previous_mode", "front")
        }
    }
}
