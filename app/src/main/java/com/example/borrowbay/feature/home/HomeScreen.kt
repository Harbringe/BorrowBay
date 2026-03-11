package com.example.borrowbay.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.offset(y = 50.dp) // Adjust position to look like it's center of bottom nav
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(location = uiState.userLocation)
            }

            item {
                CategoryRow(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )
            }

            item {
                Column {
                    SectionHeader(
                        title = "Nearby rentals",
                        onSeeAllClick = { /* TODO */ }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.nearbyRentals) { item ->
                            RentalCard(item = item)
                        }
                    }
                }
            }

            item {
                Column {
                    SectionHeader(
                        title = "Trending now",
                        onSeeAllClick = { /* TODO */ }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.trendingRentals) { item ->
                            RentalCard(item = item)
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = true,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            selected = false,
            onClick = { }
        )
        // Placeholder for middle Add button
        Spacer(modifier = Modifier.weight(1f))
        
        NavigationBarItem(
//            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Chat") },

            label = { Text("Chat") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Note: This preview might need a mock ViewModel or UI State
    HomeScreen()
}
