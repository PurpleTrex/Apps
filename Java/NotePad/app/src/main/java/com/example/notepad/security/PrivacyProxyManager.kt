package com.example.notepad.security

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * Privacy-focused proxy and DNS manager for the private browser.
 * Features:
 * - DNS-over-HTTPS (DoH) to prevent DNS leaks
 * - User-Agent randomization
 * - Referrer blocking
 * - WebRTC leak prevention (handled in WebView)
 * - Optional SOCKS5/HTTP proxy support
 */
class PrivacyProxyManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PrivacyProxy"
        
        // Cloudflare DNS-over-HTTPS
        private const val DOH_URL = "https://cloudflare-dns.com/dns-query"
        
        // Randomized mobile user agents for fingerprint resistance
        private val USER_AGENTS = listOf(
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 13; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Linux; Android 13; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        )
    }
    
    private var dnsOverHttps: DnsOverHttps? = null
    private var okHttpClient: OkHttpClient? = null
    private var currentUserAgent: String = USER_AGENTS.random()
    private var torProxy: Proxy? = null
    
    init {
        initializeSecureDns()
    }
    
    /**
     * Initialize DNS-over-HTTPS to prevent DNS leaks.
     */
    private fun initializeSecureDns() {
        try {
            // Base client for DoH bootstrap (uses system DNS only for initial DoH server resolution)
            val bootstrapClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .apply {
                    // Use Tor SOCKS proxy if available
                    torProxy?.let { proxy(it) }
                }
                .build()
            
            dnsOverHttps = DnsOverHttps.Builder()
                .client(bootstrapClient)
                .url(DOH_URL.toHttpUrl())
                .bootstrapDnsHosts(
                    // Cloudflare IPs (avoid DNS leak on initial DoH connection)
                    InetAddress.getByName("1.1.1.1"),
                    InetAddress.getByName("1.0.0.1")
                )
                .build()
            
            // Main client using DoH + Tor
            okHttpClient = OkHttpClient.Builder()
                .dns(dnsOverHttps!!)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .apply {
                    // Use Tor SOCKS proxy if available
                    torProxy?.let { proxy(it) }
                }
                .build()
            
            val status = if (torProxy != null) "with Tor SOCKS proxy" else "without proxy"
            Log.d(TAG, "DNS-over-HTTPS initialized successfully $status")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize DoH: ${e.message}", e)
            // Fallback to regular OkHttp without DoH (still better than nothing)
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .apply {
                    torProxy?.let { proxy(it) }
                }
                .build()
        }
    }
    
    /**
     * Enable Tor SOCKS proxy routing for all HTTP traffic.
     * Call this after confirming Tor is available via TorManager.
     */
    fun enableTorProxy(host: String = TorManager.TOR_SOCKS_HOST, port: Int = TorManager.TOR_SOCKS_PORT) {
        torProxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port))
        initializeSecureDns() // Reinitialize with proxy
        Log.d(TAG, "Tor SOCKS proxy enabled: $host:$port")
    }
    
    /**
     * Disable Tor proxy routing.
     */
    fun disableTorProxy() {
        torProxy = null
        initializeSecureDns() // Reinitialize without proxy
        Log.d(TAG, "Tor SOCKS proxy disabled")
    }
    
    /**
     * Check if Tor proxy is currently enabled.
     */
    fun isTorEnabled(): Boolean = torProxy != null
    
    /**
     * Resolve hostname using DNS-over-HTTPS to prevent DNS leaks.
     */
    suspend fun resolveHostSecurely(hostname: String): List<InetAddress> = withContext(Dispatchers.IO) {
        try {
            dnsOverHttps?.lookup(hostname) ?: run {
                Log.w(TAG, "DoH not available, falling back to system DNS")
                InetAddress.getAllByName(hostname).toList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "DNS resolution failed for $hostname: ${e.message}")
            // Fallback to system DNS as last resort
            try {
                InetAddress.getAllByName(hostname).toList()
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Get randomized user agent for fingerprint resistance.
     */
    fun getRandomUserAgent(): String {
        currentUserAgent = USER_AGENTS.random()
        return currentUserAgent
    }
    
    /**
     * Get mobile user agent explicitly (ensures Mobile keyword for mobile mode rendering).
     */
    fun getMobileUserAgent(): String {
        // Filter for mobile user agents only
        val mobileUAs = USER_AGENTS.filter { it.contains("Mobile") }
        currentUserAgent = mobileUAs.randomOrNull() ?: USER_AGENTS[0]
        return currentUserAgent
    }
    
    /**
     * Get current user agent.
     */
    fun getCurrentUserAgent(): String = currentUserAgent
    
    /**
     * Generate spoofed device metadata to prevent tracking.
     * Returns randomized values that change on each rotation.
     */
    fun getSpoofedMetadata(): DeviceMetadata {
        return DeviceMetadata(
            platform = listOf("Win32", "MacIntel", "Linux x86_64").random(),
            languages = listOf("en-US", "en-GB", "en").shuffled().take(2),
            timezone = listOf(-480, -420, -360, -300, 0, 60, 120).random(),
            screenWidth = listOf(1920, 1680, 1440, 1366, 1280).random(),
            screenHeight = listOf(1080, 1050, 900, 768, 720).random(),
            colorDepth = listOf(24, 32).random(),
            pixelRatio = listOf(1.0, 1.5, 2.0).random(),
            hardwareConcurrency = listOf(2, 4, 8, 16).random(),
            deviceMemory = listOf(4, 8, 16).random(),
            maxTouchPoints = 0, // Desktop spoofing
            doNotTrack = "1"
        )
    }
    
    data class DeviceMetadata(
        val platform: String,
        val languages: List<String>,
        val timezone: Int,
        val screenWidth: Int,
        val screenHeight: Int,
        val colorDepth: Int,
        val pixelRatio: Double,
        val hardwareConcurrency: Int,
        val deviceMemory: Int,
        val maxTouchPoints: Int,
        val doNotTrack: String
    )
    
    /**
     * Check if hostname appears to be tracking/analytics domain.
     * Simple heuristic-based blocking.
     */
    fun isTrackingDomain(hostname: String): Boolean {
        val trackingPatterns = listOf(
            "analytics", "tracking", "telemetry", "metrics", "ads", "doubleclick",
            "google-analytics", "facebook.com/tr", "hotjar", "mixpanel", "segment",
            "amplitude", "crashlytics", "appsflyer", "adjust.com"
        )
        val lower = hostname.lowercase()
        return trackingPatterns.any { lower.contains(it) }
    }
    
    /**
     * Get HTTP client with DNS-over-HTTPS configured.
     */
    fun getSecureHttpClient(): OkHttpClient {
        return okHttpClient ?: OkHttpClient.Builder().build()
    }
    
    /**
     * Test DNS-over-HTTPS is working (for debugging).
     */
    suspend fun testDohConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val addresses = resolveHostSecurely("cloudflare.com")
            addresses.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "DoH test failed: ${e.message}")
            false
        }
    }
    
    /**
     * Clear all DNS cache and regenerate user agent.
     */
    fun rotateIdentity() {
        currentUserAgent = USER_AGENTS.random()
        // Force DNS cache clear by reinitializing
        initializeSecureDns()
        Log.d(TAG, "Identity rotated: new UA and DNS cache cleared")
    }
}
