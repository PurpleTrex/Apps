package com.gaia.maps.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Download Maps Activity - Select and download offline maps
 */
class DownloadMapsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            GaiaTheme {
                DownloadMapsScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DownloadMapsScreen() {
        // Sample cities for offline maps
        val cities = remember {
            listOf(
                CityMap("San Francisco", "California", "~250 MB"),
                CityMap("Los Angeles", "California", "~480 MB"),
                CityMap("New York", "New York", "~620 MB"),
                CityMap("Chicago", "Illinois", "~380 MB"),
                CityMap("Houston", "Texas", "~340 MB"),
                CityMap("Phoenix", "Arizona", "~290 MB"),
                CityMap("Philadelphia", "Pennsylvania", "~310 MB"),
                CityMap("San Antonio", "Texas", "~260 MB"),
                CityMap("San Diego", "California", "~280 MB"),
                CityMap("Dallas", "Texas", "~350 MB"),
                CityMap("Seattle", "Washington", "~270 MB"),
                CityMap("Boston", "Massachusetts", "~240 MB"),
                CityMap("Austin", "Texas", "~230 MB"),
                CityMap("Portland", "Oregon", "~220 MB"),
                CityMap("Denver", "Colorado", "~250 MB")
            )
        }
        
        var searchQuery by remember { mutableStateOf("") }
        var downloadingCity by remember { mutableStateOf<String?>(null) }
        var downloadedCities by remember { mutableStateOf(setOf<String>()) }
        
        val filteredCities = cities.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.state.contains(searchQuery, ignoreCase = true)
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Download Maps") },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Download maps for offline navigation. No internet required after download.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search cities...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Cities List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredCities) { city ->
                        CityMapItem(
                            city = city,
                            isDownloaded = downloadedCities.contains(city.name),
                            isDownloading = downloadingCity == city.name,
                            onDownload = {
                                downloadingCity = city.name
                                // Simulate download
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(3000)
                                    downloadedCities = downloadedCities + city.name
                                    downloadingCity = null
                                }
                            },
                            onDelete = {
                                downloadedCities = downloadedCities - city.name
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    @Composable
    fun CityMapItem(
        city: CityMap,
        isDownloaded: Boolean,
        isDownloading: Boolean,
        onDownload: () -> Unit,
        onDelete: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!isDownloaded && !isDownloading) onDownload() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        city.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${city.state} • ${city.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                when {
                    isDownloading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    isDownloaded -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Downloaded",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    else -> {
                        Icon(
                            Icons.Default.Download,
                            "Download",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
    
    data class CityMap(
        val name: String,
        val state: String,
        val size: String
    )
}
