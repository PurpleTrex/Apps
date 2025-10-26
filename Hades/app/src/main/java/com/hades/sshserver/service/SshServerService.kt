package com.hades.sshserver.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hades.sshserver.R
import com.hades.sshserver.data.ServerStatus
import com.hades.sshserver.ui.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import java.io.File

class SshServerService : Service() {

    private var sshServer: SshServer? = null
    private val binder = SshServerBinder()

    private val _serverStatus = MutableStateFlow(ServerStatus.STOPPED)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _activeConnections = MutableStateFlow(0)
    val activeConnections: StateFlow<Int> = _activeConnections.asStateFlow()

    inner class SshServerBinder : Binder() {
        fun getService(): SshServerService = this@SshServerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVER -> startSshServer()
            ACTION_STOP_SERVER -> stopSshServer()
        }
        return START_STICKY
    }

    private fun startSshServer() {
        try {
            _serverStatus.value = ServerStatus.STARTING

            // Set up SSH server
            sshServer = SshServer.setUpDefaultServer().apply {
                port = DEFAULT_PORT
                host = DEFAULT_HOST

                // Set up host key
                val hostKeyFile = File(filesDir, "hostkey.ser")
                keyPairProvider = SimpleGeneratorHostKeyProvider(hostKeyFile.toPath())

                // Simple password authentication (for demo purposes)
                passwordAuthenticator = PasswordAuthenticator { username, password, _ ->
                    // TODO: Implement proper authentication with stored credentials
                    username == "admin" && password == "admin"
                }

                // Configure SFTP subsystem
                subsystemFactories = listOf(
                    SftpSubsystemFactory.Builder()
                        .build()
                )

                start()
            }

            _serverStatus.value = ServerStatus.RUNNING
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            e.printStackTrace()
            _serverStatus.value = ServerStatus.ERROR
            stopSelf()
        }
    }

    private fun stopSshServer() {
        try {
            _serverStatus.value = ServerStatus.STOPPING
            sshServer?.stop()
            sshServer = null
            _serverStatus.value = ServerStatus.STOPPED
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
            _serverStatus.value = ServerStatus.ERROR
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SSH Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "SSH server status notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SSH Server Running")
            .setContentText("Port: $DEFAULT_PORT | Connections: ${_activeConnections.value}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun getPort(): Int = sshServer?.port ?: DEFAULT_PORT

    override fun onDestroy() {
        super.onDestroy()
        stopSshServer()
    }

    companion object {
        const val ACTION_START_SERVER = "com.hades.sshserver.START_SERVER"
        const val ACTION_STOP_SERVER = "com.hades.sshserver.STOP_SERVER"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ssh_server_channel"
        private const val DEFAULT_PORT = 2222
        private const val DEFAULT_HOST = "0.0.0.0"
    }
}
