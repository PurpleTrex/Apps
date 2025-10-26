package com.hades.sshserver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hades.sshserver.service.SshServerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Check if auto-start is enabled in preferences
            // For now, we'll skip auto-start
            
            // Uncomment to enable auto-start on boot:
            // val serviceIntent = Intent(context, SshServerService::class.java).apply {
            //     action = SshServerService.ACTION_START_SERVER
            // }
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //     context.startForegroundService(serviceIntent)
            // } else {
            //     context.startService(serviceIntent)
            // }
        }
    }
}
