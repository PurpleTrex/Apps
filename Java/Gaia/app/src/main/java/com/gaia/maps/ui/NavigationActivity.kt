package com.gaia.maps.ui

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

/**
 * Navigation Activity - Active turn-by-turn navigation
 */
class NavigationActivity : ComponentActivity() {
    
    private var mapView: MapView? = null
    private var isNavigating by mutableStateOf(false)
    private var distractionFreeMode by mutableStateOf(true)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            GaiaTheme {
                NavigationScreen()
            }
        }
    }
    
    @Composable
    fun NavigationScreen() {
        var currentInstruction by remember { mutableStateOf("Turn right on Main Street in 500 ft") }
        var distance by remember { mutableStateOf("2.3 miles") }
        var eta by remember { mutableStateOf("8 minutes") }
        
        LaunchedEffect(distractionFreeMode) {
            if (distractionFreeMode && isNavigating) {
                enableDistractionFreeMode()
            } else {
                disableDistractionFreeMode()
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Map View
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        mapView = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)
                        controller.setCenter(GeoPoint(37.7749, -122.4194))
                        
                        // Add sample route
                        val routeLine = Polyline().apply {
                            addPoint(GeoPoint(37.7749, -122.4194))
                            addPoint(GeoPoint(37.7849, -122.4094))
                            addPoint(GeoPoint(37.7949, -122.4094))
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 10f
                        }
                        overlays.add(routeLine)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Top Navigation Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                currentInstruction,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Distance: $distance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "ETA: $eta",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Icon(
                            Icons.Default.TurnRight,
                            "Next turn",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Bottom Control Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Distraction-Free Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (distractionFreeMode) Icons.Default.DoNotDisturb else Icons.Default.Notifications,
                                "Notifications",
                                tint = if (distractionFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Distraction-Free Mode",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (distractionFreeMode) {
                                    Text(
                                        "Notifications blocked",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Switch(
                            checked = distractionFreeMode,
                            onCheckedChange = { distractionFreeMode = it }
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Report incident */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Report")
                        }
                        
                        if (!isNavigating) {
                            Button(
                                onClick = { isNavigating = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Start")
                            }
                        } else {
                            Button(
                                onClick = { 
                                    isNavigating = false
                                    finish()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("End")
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun enableDistractionFreeMode() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Request Do Not Disturb access if needed
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                // Note: In production, should request permission from user
                return
            }
            
            // Enable Do Not Disturb mode
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }
    
    private fun disableDistractionFreeMode() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disableDistractionFreeMode()
    }
}
