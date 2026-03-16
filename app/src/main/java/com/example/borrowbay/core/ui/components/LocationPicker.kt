package com.example.borrowbay.core.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.borrowbay.ui.theme.BorderLight
import com.example.borrowbay.ui.theme.MutedFgLight
import com.example.borrowbay.ui.theme.MutedLight
import com.example.borrowbay.ui.theme.Ocean
import com.example.borrowbay.ui.theme.SurfaceLight
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun LocationPickerDialog(
    initialLat: Double,
    initialLng: Double,
    onDismiss: () -> Unit,
    onLocationSelected: (Double, Double, String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var address by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(initialLat) }
    var longitude by remember { mutableDoubleStateOf(initialLng) }
    
    var suggestions by remember { mutableStateOf<List<android.location.Address>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var isProgrammaticUpdate by remember { mutableStateOf(false) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    fun updateLocationAndAddress(latVal: Double, lngVal: Double, isFromMap: Boolean = false) {
        latitude = latVal
        longitude = lngVal
        if (isFromMap) isProgrammaticUpdate = true
        address = "Fetching address..."
        
        scope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val fetchedAddress = withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(latVal, lngVal, 1)?.firstOrNull()
                    } else {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(latVal, lngVal, 1)?.firstOrNull()
                    }
                }
                if (fetchedAddress != null) {
                    address = fetchedAddress.getAddressLine(0) ?: ""
                    locationName = fetchedAddress.locality ?: fetchedAddress.subAdminArea ?: "Unknown Location"
                } else {
                    address = "Location at ${String.format("%.4f, %.4f", latVal, lngVal)}"
                    locationName = "Custom Location"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    address = "Location at ${String.format("%.4f, %.4f", latVal, lngVal)}"
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                @SuppressLint("MissingPermission")
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc ->
                        loc?.let { updateLocationAndAddress(it.latitude, it.longitude) }
                    }
            } catch (e: SecurityException) { e.printStackTrace() }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (latitude != 0.0 && longitude != 0.0) {
            updateLocationAndAddress(latitude, longitude)
        } else {
            val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fineGranted || coarseGranted) {
                @SuppressLint("MissingPermission")
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc -> loc?.let { updateLocationAndAddress(it.latitude, it.longitude) } }
            } else {
                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    fun searchAddress(query: String) {
        searchJob?.cancel()
        searchJob = scope.launch(Dispatchers.IO) {
            delay(800)
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocationName(query, 5)
                withContext(Dispatchers.Main) {
                    suggestions = results ?: emptyList()
                    showSuggestions = suggestions.isNotEmpty()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                    Text("Select Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(MutedLight).border(1.dp, BorderLight, RoundedCornerShape(20.dp))) {
                    AndroidView<MapView>(
                        factory = { 
                            mapView.apply {
                                val eventsReceiver = object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        updateLocationAndAddress(p.latitude, p.longitude, isFromMap = true)
                                        return true
                                    }
                                    override fun longPressHelper(p: GeoPoint): Boolean = false
                                }
                                overlays.add(MapEventsOverlay(eventsReceiver))
                            }
                        },
                        update = { view ->
                            if (latitude != 0.0) {
                                val geoPoint = GeoPoint(latitude, longitude)
                                val currentCenter = view.mapCenter
                                val latDiff = Math.abs(currentCenter.latitude - latitude)
                                val lngDiff = Math.abs(currentCenter.longitude - longitude)
                                if (latDiff > 0.0001 || lngDiff > 0.0001) { view.controller.animateTo(geoPoint) }
                                view.overlays.removeAll { it is Marker }
                                val marker = Marker(view).apply {
                                    position = geoPoint
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Selected Location"
                                }
                                view.overlays.add(marker)
                            }
                            view.invalidate()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
                        Text("Tap map to select", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it.replace("\n", "")
                                if (!isProgrammaticUpdate && it.length > 3) searchAddress(it) else showSuggestions = false
                                isProgrammaticUpdate = false
                            },
                            placeholder = { Text("Search or tap map", color = MutedFgLight) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Ocean, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (fineGranted || coarseGranted) {
                                        @SuppressLint("MissingPermission")
                                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                            .addOnSuccessListener { loc -> loc?.let { updateLocationAndAddress(it.latitude, it.longitude) } }
                                    } else {
                                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    }
                                }) { Icon(Icons.Outlined.MyLocation, "Current location", tint = Ocean, modifier = Modifier.size(20.dp)) }
                            },
                            modifier = Modifier.fillMaxWidth(), 
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            singleLine = true,
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ocean, unfocusedBorderColor = BorderLight, unfocusedContainerColor = SurfaceLight, focusedContainerColor = SurfaceLight, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                        )
                        DropdownMenu(
                            expanded = showSuggestions,
                            onDismissRequest = { showSuggestions = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color.White),
                            properties = PopupProperties(focusable = false)
                        ) {
                            suggestions.forEach { addr ->
                                DropdownMenuItem(
                                    text = { Text(addr.getAddressLine(0), color = Color.Black, fontSize = 14.sp) },
                                    onClick = {
                                        isProgrammaticUpdate = true
                                        showSuggestions = false
                                        searchJob?.cancel()
                                        updateLocationAndAddress(addr.latitude, addr.longitude)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Place, null, tint = Ocean, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onLocationSelected(latitude, longitude, locationName, address) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ocean),
                    enabled = address.isNotEmpty() && latitude != 0.0 && address != "Fetching address..."
                ) { Text("Confirm Location", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
