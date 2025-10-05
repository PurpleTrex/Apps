package com.gaia.maps

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import org.osmdroid.config.Configuration

/**
 * Gaia Application class
 * Initializes offline maps configuration and notification channels
 */
class GaiaApplication : Application() {
    
    companion object {
        const val NAVIGATION_CHANNEL_ID = "navigation_channel"
        const val DOWNLOAD_CHANNEL_ID = "download_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Configure OSMDroid for offline maps
        Configuration.getInstance().apply {
            userAgentValue = packageName
            // Set cache path for offline map tiles
            osmdroidBasePath = getExternalFilesDir(null)
            osmdroidTileCache = getExternalFilesDir("osmdroid/tiles")
        }
        
        // Create notification channels
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Navigation channel
            val navigationChannel = NotificationChannel(
                NAVIGATION_CHANNEL_ID,
                "Navigation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active navigation notifications"
                setShowBadge(true)
            }
            
            // Download channel
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Map Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Map download progress notifications"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(navigationChannel)
            notificationManager.createNotificationChannel(downloadChannel)
        }
    }
}
