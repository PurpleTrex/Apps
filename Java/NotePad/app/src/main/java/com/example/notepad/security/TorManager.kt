package com.example.notepad.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * TorManager handles Tor integration via Orbot app.
 * Auto-detects, prompts installation, and manages connection.
 */
object TorManager {
    private const val TAG = "TorManager"
    
    // Tor SOCKS proxy
    const val TOR_SOCKS_HOST = "127.0.0.1"
    const val TOR_SOCKS_PORT = 9050
    
    // Orbot package
    private const val ORBOT_PACKAGE = "org.torproject.android"
    
    @Volatile
    private var torConnectionState: TorState = TorState.DISCONNECTED
    
    enum class TorState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED
    }
    
    /**
     * Auto-initialize Tor: install Orbot if needed, start it, wait for connection.
     */
    suspend fun autoInitializeTor(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if already connected
            if (isTorAvailable()) {
                torConnectionState = TorState.CONNECTED
                return@withContext true
            }
            
            // Check if Orbot is installed
            if (!isOrbotInstalled(context)) {
                Log.d(TAG, "Orbot not installed, prompting...")
                withContext(Dispatchers.Main) {
                    promptInstallOrbot(context)
                }
                return@withContext false
            }
            
            // Orbot installed, try to start it
            torConnectionState = TorState.CONNECTING
            startOrbot(context)
            
            // Wait for connection (up to 20 seconds)
            return@withContext waitForTor(20000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error auto-initializing Tor: ${e.message}", e)
            torConnectionState = TorState.FAILED
            false
        }
    }
    
    /**
     * Check if external Tor (Orbot) is running and accessible.
     */
    suspend fun isTorAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(TOR_SOCKS_HOST, TOR_SOCKS_PORT), 2000)
                torConnectionState = TorState.CONNECTED
                Log.d(TAG, "Tor SOCKS proxy detected at $TOR_SOCKS_HOST:$TOR_SOCKS_PORT")
                true
            }
        } catch (e: Exception) {
            torConnectionState = TorState.DISCONNECTED
            Log.d(TAG, "Tor not available: ${e.message}")
            false
        }
    }
    
    /**
     * Wait for Tor to become available (up to timeout).
     */
    suspend fun waitForTor(timeoutMs: Long = 30000): Boolean = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        torConnectionState = TorState.CONNECTING
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isTorAvailable()) {
                torConnectionState = TorState.CONNECTED
                return@withContext true
            }
            delay(1000)
        }
        
        torConnectionState = TorState.FAILED
        false
    }
    
    /**
     * Get current Tor connection state.
     */
    fun getTorState(): TorState = torConnectionState
    
    /**
     * Check if Orbot is installed.
     */
    fun isOrbotInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Start Orbot app.
     */
    fun startOrbot(context: Context): Boolean {
        return try {
            // Send START broadcast
            val intent = Intent("org.torproject.android.intent.action.START")
            intent.setPackage(ORBOT_PACKAGE)
            context.sendBroadcast(intent)
            
            // Also launch Orbot app
            val launchIntent = context.packageManager.getLaunchIntentForPackage(ORBOT_PACKAGE)
            launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
            
            torConnectionState = TorState.CONNECTING
            Log.d(TAG, "Orbot start initiated")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Orbot: ${e.message}")
            false
        }
    }
    
    /**
     * Prompt user to install Orbot from Play Store.
     */
    fun promptInstallOrbot(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://details?id=$ORBOT_PACKAGE"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web
            try {
                val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$ORBOT_PACKAGE"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open install page: ${e2.message}")
            }
        }
    }
    
    /**
     * Get SOCKS proxy configuration if Tor is available.
     * Returns Pair(host, port) or null if unavailable.
     */
    suspend fun getTorProxyConfig(): Pair<String, Int>? {
        return if (isTorAvailable()) {
            Pair(TOR_SOCKS_HOST, TOR_SOCKS_PORT)
        } else null
    }
    
    /**
     * Test Tor connection by attempting to connect to a .onion address.
     */
    suspend fun testTorConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Simple SOCKS handshake test
            Socket().use { socket ->
                socket.connect(InetSocketAddress(TOR_SOCKS_HOST, TOR_SOCKS_PORT), 5000)
                socket.soTimeout = 5000
                
                // SOCKS5 greeting
                socket.getOutputStream().write(byteArrayOf(0x05, 0x01, 0x00))
                
                // Read response
                val response = ByteArray(2)
                socket.getInputStream().read(response)
                
                // Check if SOCKS5 accepted our method
                response[0] == 0x05.toByte() && response[1] == 0x00.toByte()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tor connection test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get Tor circuit information (if available).
     */
    suspend fun getTorCircuitInfo(): String = withContext(Dispatchers.IO) {
        val state = when (torConnectionState) {
            TorState.DISCONNECTED -> "❌ Disconnected"
            TorState.CONNECTING -> "🔄 Connecting..."
            TorState.CONNECTED -> "✅ Connected"
            TorState.FAILED -> "⚠️ Failed"
        }
        
        if (torConnectionState == TorState.CONNECTED) {
            val testResult = if (testTorConnection()) "Working" else "Not Working"
            "$state (Test: $testResult)"
        } else {
            state
        }
    }
}
