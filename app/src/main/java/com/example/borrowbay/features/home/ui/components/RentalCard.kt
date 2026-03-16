package com.example.borrowbay.features.home.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.borrowbay.core.util.FormattingUtils
import com.example.borrowbay.data.model.RentalItem
import com.example.borrowbay.ui.theme.*
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
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
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = (if (item.isAvailable) Emerald else Color.Gray).copy(alpha = 0.9f)
                ) {
                    Text(
                        text = if (item.isAvailable) "Available" else "Rented",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Top gradient for better readability of badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp)) {
                Text(
                    text = FormattingUtils.formatName(item.name, 22),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${FormattingUtils.formatCurrency(item.pricePerDay)}",
                            fontSize = 15.sp,
                            color = Ocean,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/day",
                            fontSize = 10.sp,
                            color = MutedFgLight,
                            modifier = Modifier.padding(bottom = 1.dp, start = 1.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.MyLocation, null, tint = MutedFgLight, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = FormattingUtils.formatDistance(item.distance),
                            fontSize = 9.sp,
                            color = MutedFgLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

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
                            text = if (remainingDays < 0) "Overdue: ${-remainingDays}d" else "$remainingDays days left",
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingDays < 0) Destructive else EmeraldDark,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ownerName = item.owner?.name ?: "User"
                    val avatarUrl = item.owner?.avatarUrl
                    
                    Box(modifier = Modifier.size(18.dp)) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = MutedLight
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(3.dp),
                                    tint = MutedFgLight
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (showRenterInfo) "By ${FormattingUtils.formatName(ownerName, 15)}" else FormattingUtils.formatName(ownerName, 15),
                        fontSize = 10.sp,
                        color = MutedFgLight,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
