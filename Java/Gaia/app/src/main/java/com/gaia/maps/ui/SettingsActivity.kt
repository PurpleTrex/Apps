package com.gaia.maps.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gaia.maps.data.RouteType

/**
 * Settings Activity - Configure navigation preferences
 */
class SettingsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            GaiaTheme {
                SettingsScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen() {
        var routeType by remember { mutableStateOf(RouteType.FASTEST) }
        var avoidHighways by remember { mutableStateOf(false) }
        var avoidTolls by remember { mutableStateOf(false) }
        var preferResidential by remember { mutableStateOf(false) }
        var preferInterstate by remember { mutableStateOf(false) }
        var sensitiveMode by remember { mutableStateOf(false) }
        var distractionFreeMode by remember { mutableStateOf(true) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Navigation Preferences Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Navigation Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        // Route Type Selection
                        Text("Route Type", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        
                        RouteType.values().forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = routeType == type,
                                    onClick = { routeType = type }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    when (type) {
                                        RouteType.FASTEST -> "Fastest Route"
                                        RouteType.SHORTEST -> "Shortest Route"
                                        RouteType.SCENIC -> "Scenic Route"
                                    }
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        // Road Preferences
                        Text("Road Preferences", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        
                        SwitchRow(
                            label = "Prefer Interstate Roads",
                            checked = preferInterstate,
                            onCheckedChange = { 
                                preferInterstate = it
                                if (it) preferResidential = false
                            }
                        )
                        
                        SwitchRow(
                            label = "Prefer Residential Roads",
                            checked = preferResidential,
                            onCheckedChange = { 
                                preferResidential = it
                                if (it) preferInterstate = false
                            }
                        )
                        
                        SwitchRow(
                            label = "Avoid Highways",
                            checked = avoidHighways,
                            onCheckedChange = { avoidHighways = it }
                        )
                        
                        SwitchRow(
                            label = "Avoid Tolls",
                            checked = avoidTolls,
                            onCheckedChange = { avoidTolls = it }
                        )
                    }
                }
                
                // Special Modes Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Special Modes",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        // Sensitive Mode
                        SwitchRow(
                            label = "Sensitive Mode",
                            description = "Avoid construction zones and police activity",
                            checked = sensitiveMode,
                            onCheckedChange = { sensitiveMode = it }
                        )
                        
                        // Distraction-Free Mode
                        SwitchRow(
                            label = "Distraction-Free Navigation",
                            description = "Block all notifications during navigation",
                            checked = distractionFreeMode,
                            onCheckedChange = { distractionFreeMode = it }
                        )
                    }
                }
                
                // Privacy Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Privacy & Security",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "✓ No external tracking\n" +
                            "✓ No metadata collection\n" +
                            "✓ All navigation processed locally\n" +
                            "✓ Your location never leaves your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    fun SwitchRow(
        label: String,
        description: String? = null,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                if (description != null) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
