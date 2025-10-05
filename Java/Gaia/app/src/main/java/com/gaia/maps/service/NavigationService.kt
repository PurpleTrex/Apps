package com.gaia.maps.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gaia.maps.GaiaApplication
import com.gaia.maps.R
import com.gaia.maps.ui.NavigationActivity

/**
 * Foreground service for active navigation
 * Keeps navigation running even when app is in background
 */
class NavigationService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START_NAVIGATION = "com.gaia.maps.START_NAVIGATION"
        const val ACTION_STOP_NAVIGATION = "com.gaia.maps.STOP_NAVIGATION"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_NAVIGATION -> {
                startForegroundService()
            }
            ACTION_STOP_NAVIGATION -> {
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, NavigationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, NavigationService::class.java).apply {
            action = ACTION_STOP_NAVIGATION
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, GaiaApplication.NAVIGATION_CHANNEL_ID)
            .setContentTitle("Gaia Navigation Active")
            .setContentText("Turn right on Main Street in 500 ft")
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Stop Navigation", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .build()
    }
}
