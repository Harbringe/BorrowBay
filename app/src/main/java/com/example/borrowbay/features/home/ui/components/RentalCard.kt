package com.example.borrowbay.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.borrowbay.data.model.RentalItem
import com.example.borrowbay.ui.theme.*
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RentalCard(
    item: RentalItem,
    modifier: Modifier = Modifier,
    showRenterInfo: Boolean = false,
    showRentalStatus: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MutedLight)
            ) {
                AsyncImage(
                    model = item.imageUrls.firstOrNull(),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Availability Badge
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(10.dp),
                    color = (if (item.isAvailable) Emerald else Color.Gray).copy(alpha = 0.9f)
                ) {
                    Text(
                        text = if (item.isAvailable) "Available" else "Rented",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 20.sp.value.dp) // Maintain consistent title height
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${item.pricePerDay.toInt()}",
                            fontSize = 18.sp,
                            color = Ocean,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/day",
                            fontSize = 12.sp,
                            color = MutedFgLight,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.MyLocation, null, tint = MutedFgLight, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        val distanceText = String.format(Locale.ROOT, "%.1f", item.distance)
                        Text(
                            text = "$distanceText km",
                            fontSize = 11.sp,
                            color = MutedFgLight
                        )
                    }
                }

                // Placeholder or Actual Rental Status to keep height consistent
                if (showRentalStatus && item.rentedAt != null && item.rentalDurationDays != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val elapsedDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - item.rentedAt)
                    val remainingDays = item.rentalDurationDays - elapsedDays
                    
                    Surface(
                        color = if (remainingDays < 0) Destructive.copy(alpha = 0.1f) else Emerald.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (remainingDays < 0) "Overdue by ${-remainingDays} days" else "$remainingDays days remaining",
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (remainingDays < 0) Destructive else EmeraldDark
                        )
                    }
                } else if (showRentalStatus) {
                     Spacer(modifier = Modifier.height(8.dp))
                     Box(modifier = Modifier.height(24.dp)) // Placeholder height
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val ownerName = item.owner?.name ?: "Unknown"
                    val avatarUrl = item.owner?.avatarUrl
                    
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(MutedLight),
                            tint = MutedFgLight
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (showRenterInfo) "Rented by $ownerName" else ownerName,
                        fontSize = 12.sp,
                        color = MutedFgLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
