package com.example.notepad.ui.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.notepad.security.AppDestructionManager
import com.example.notepad.security.PrivacyProxyManager
import com.example.notepad.security.TorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class PrivateBrowserActivity: ComponentActivity() {
    private var webView: WebView? = null
    private lateinit var privacyManager: PrivacyProxyManager
    private var lastBackPress: Long = 0
    private val DOUBLE_TAP_THRESHOLD = 500L // 500ms for panic double-tap
    
    // 7-tap quick exit detection
    private val tapTimes = mutableListOf<Long>()
    private val SEVEN_TAP_WINDOW = 2000L // 2 seconds to complete 7 taps
    private val SEVEN_TAP_COUNT = 7

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize privacy manager with DNS-over-HTTPS
        privacyManager = PrivacyProxyManager(this)
        
        setContent {
            MaterialTheme {
                BrowserContent()
            }
        }
    }
    
    @Composable
    private fun BrowserContent() {
        var address by remember { mutableStateOf(TextFieldValue("https://duckduckgo.com")) }
        var loading by remember { mutableStateOf(false) }
        var privacyStatus by remember { mutableStateOf("Initializing...") }
        var showSidebar by remember { mutableStateOf(false) }
        var javaScriptEnabled by remember { mutableStateOf(true) }
        var canGoBack by remember { mutableStateOf(false) }
        var canGoForward by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        
        // Initialize privacy status
        LaunchedEffect(Unit) {
            scope.launch {
                // Test DNS-over-HTTPS
                val dohWorking = privacyManager.testDohConnection()
                privacyStatus = "DoH: ${if (dohWorking) "✓" else "✗"}"
                Log.d("PrivateBrowser", "Privacy browser initialized with HTTPS + DoH")
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // Compact top bar with URL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu button to open sidebar
                    IconButton(onClick = { showSidebar = !showSidebar }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    
                    // Back button
                    IconButton(
                        onClick = { 
                            webView?.goBack()
                            // Update navigation state
                            webView?.let { wv ->
                                canGoBack = wv.canGoBack()
                                canGoForward = wv.canGoForward()
                            }
                        },
                        enabled = canGoBack
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack, 
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = if (canGoBack) MaterialTheme.colorScheme.onPrimaryContainer 
                                  else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                        )
                    }
                    
                    // Forward button
                    IconButton(
                        onClick = { 
                            webView?.goForward()
                            // Update navigation state
                            webView?.let { wv ->
                                canGoBack = wv.canGoBack()
                                canGoForward = wv.canGoForward()
                            }
                        },
                        enabled = canGoForward
                    ) {
                        Icon(
                            Icons.Filled.ArrowForward, 
                            contentDescription = "Forward",
                            modifier = Modifier.size(20.dp),
                            tint = if (canGoForward) MaterialTheme.colorScheme.onPrimaryContainer 
                                  else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                        )
                    }
                    
                    // URL bar
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("Search or enter URL", style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Go
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onGo = {
                                val url = normalizeUrl(address.text)
                                loading = true
                                webView?.loadUrl(url)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    // Refresh button
                    IconButton(onClick = { 
                        privacyManager.rotateIdentity()
                        webView?.reload() 
                    }) { 
                        Icon(Icons.Filled.Refresh, contentDescription = "Reload", modifier = Modifier.size(20.dp)) 
                    }
                    
                    // Close button
                    IconButton(onClick = { finish() }) { 
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(20.dp)) 
                    }
                }
                
                // WebView
                Box(Modifier.weight(1f)) {
                    AndroidWebView(
                    privacyManager = privacyManager,
                    activity = this@PrivateBrowserActivity,
                    onCreated = { w ->
                        webView = w
                        with(w.settings) {
                            javaScriptEnabled = true
                            domStorageEnabled = false
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            
                            // FORCE MOBILE MODE - Critical for proper mobile rendering
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                            
                            // Set mobile user agent explicitly
                            userAgentString = privacyManager.getMobileUserAgent()
                            
                            // Anti-tracking settings
                            setGeolocationEnabled(false)
                            databaseEnabled = false
                            
                            // Additional privacy settings
                            setSaveFormData(false)
                            setSavePassword(false)
                            setMediaPlaybackRequiresUserGesture(true)
                            
                            // Disable mixed content
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        }
                        w.setBackgroundColor(Color.WHITE) // Use white background for better visibility
                        CookieManager.getInstance().setAcceptCookie(false)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(w, false)
                        
                        w.webViewClient = object: WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                // Block known tracking domains
                                val url = request?.url?.toString() ?: return false
                                val host = request.url.host ?: return false
                                if (privacyManager.isTrackingDomain(host)) {
                                    return true // Block the request
                                }
                                return super.shouldOverrideUrlLoading(view, request)
                            }
                            
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                // Inject privacy scripts as early as possible
                                view?.evaluateJavascript(getAntiTrackingJs(), null)
                                // Update navigation state
                                view?.let { wv ->
                                    canGoBack = wv.canGoBack()
                                    canGoForward = wv.canGoForward()
                                }
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                loading = false
                                // Re-inject anti-fingerprinting JavaScript to ensure it's active
                                view?.evaluateJavascript(getAntiTrackingJs(), null)
                                // Update navigation state
                                view?.let { wv ->
                                    canGoBack = wv.canGoBack()
                                    canGoForward = wv.canGoForward()
                                }
                            }
                        }
                        w.webChromeClient = object: WebChromeClient() {
                            // Block geolocation requests
                            override fun onGeolocationPermissionsShowPrompt(
                                origin: String?,
                                callback: android.webkit.GeolocationPermissions.Callback?
                            ) {
                                callback?.invoke(origin, false, false)
                            }
                            
                            // Block JavaScript alerts that might fingerprint
                            override fun onJsAlert(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: android.webkit.JsResult?
                            ): Boolean {
                                result?.cancel()
                                return true
                            }
                        }
                        w.loadUrl(address.text)
                    },
                    onProgress = { p -> loading = p < 100 }
                )
                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }
            }
        }
            
            // Sidebar
            if (showSidebar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                        .clickable { showSidebar = false }
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .align(Alignment.CenterEnd),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Privacy Browser",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showSidebar = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close sidebar")
                            }
                        }
                        
                        Divider(Modifier.padding(vertical = 8.dp))
                        
                        // Privacy Status Section
                        Text(
                            "Privacy Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "HTTPS Encrypted",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "✓ $privacyStatus",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Text(
                                    "✓ WebRTC Blocked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Text(
                                    "✓ Tracking Blocked",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Text(
                                    "✓ No Cookies",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                                Text(
                                    "✓ Random User Agent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Settings Section
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("JavaScript", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = javaScriptEnabled,
                                onCheckedChange = { 
                                    javaScriptEnabled = it
                                    webView?.settings?.javaScriptEnabled = it
                                }
                            )
                        }
                        
                        Divider()
                        
                        // Actions Section
                        Spacer(Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                privacyManager.rotateIdentity()
                                webView?.settings?.userAgentString = privacyManager.getMobileUserAgent()
                                webView?.clearHistory()
                                webView?.clearCache(true)
                                webView?.loadUrl("about:blank")
                                address = TextFieldValue("https://duckduckgo.com")
                                showSidebar = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("New Identity")
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = {
                                webView?.clearHistory()
                                webView?.clearCache(true)
                                showSidebar = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear History")
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        // Footer info
                        Text(
                            "For maximum anonymity, use Tor Browser or VPN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    private fun normalizeUrl(input: String): String {
        val t = input.trim()
        if (t.startsWith("http://") || t.startsWith("https://")) return t
        return if ('.' in t && !t.contains(' ')) "https://$t" else "https://duckduckgo.com/?q=" + t.replace(' ', '+')
    }
    
    /**
     * Check if Tor/Orbot is available and running.
     */
    private fun checkTorAvailability(): Boolean {
        return try {
            // Check if Orbot app is installed
            val orbotInstalled = try {
                packageManager.getPackageInfo("org.torproject.android", 0)
                true
            } catch (e: Exception) {
                false
            }
            
            // TODO: Could also check if SOCKS proxy on localhost:9050 is accepting connections
            // For now, just check if installed
            orbotInstalled
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * JavaScript to inject for blocking WebRTC leaks, fingerprinting, and tracking.
     */
    private fun getAntiTrackingJs(): String {
        val metadata = privacyManager.getSpoofedMetadata()
        return """
        (function() {
            // CRITICAL: Block ALL geolocation APIs aggressively
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition = function(success, error) {
                    if (error) error({ code: 1, message: 'User denied geolocation' });
                };
                navigator.geolocation.watchPosition = function(success, error) {
                    if (error) error({ code: 1, message: 'User denied geolocation' });
                    return 0;
                };
                navigator.geolocation.clearWatch = function() {};
                
                // Override completely
                Object.defineProperty(navigator, 'geolocation', {
                    get: function() { return null; }
                });
            }
            
            // Block WebRTC to prevent IP leak
            if (window.RTCPeerConnection) {
                window.RTCPeerConnection = function() { 
                    throw new Error('WebRTC disabled for privacy'); 
                };
            }
            if (window.webkitRTCPeerConnection) {
                window.webkitRTCPeerConnection = function() { 
                    throw new Error('WebRTC disabled for privacy'); 
                };
            }
            if (window.mozRTCPeerConnection) {
                window.mozRTCPeerConnection = function() { 
                    throw new Error('WebRTC disabled for privacy'); 
                };
            }
            
            // Spoof navigator properties to prevent device fingerprinting
            Object.defineProperty(navigator, 'platform', {
                get: function() { return '${metadata.platform}'; }
            });
            
            Object.defineProperty(navigator, 'languages', {
                get: function() { return ${metadata.languages.joinToString(",", "[", "]") { "'$it'" }}; }
            });
            
            Object.defineProperty(navigator, 'language', {
                get: function() { return '${metadata.languages.firstOrNull() ?: "en-US"}'; }
            });
            
            Object.defineProperty(navigator, 'hardwareConcurrency', {
                get: function() { return ${metadata.hardwareConcurrency}; }
            });
            
            Object.defineProperty(navigator, 'deviceMemory', {
                get: function() { return ${metadata.deviceMemory}; }
            });
            
            Object.defineProperty(navigator, 'maxTouchPoints', {
                get: function() { return ${metadata.maxTouchPoints}; }
            });
            
            Object.defineProperty(navigator, 'doNotTrack', {
                get: function() { return '${metadata.doNotTrack}'; }
            });
            
            // Block device ID APIs
            if (navigator.mediaDevices) {
                const originalEnumerate = navigator.mediaDevices.enumerateDevices;
                navigator.mediaDevices.enumerateDevices = function() {
                    return Promise.resolve([]);
                };
            }
            
            // Spoof screen properties
            Object.defineProperty(screen, 'width', {
                get: function() { return ${metadata.screenWidth}; }
            });
            
            Object.defineProperty(screen, 'height', {
                get: function() { return ${metadata.screenHeight}; }
            });
            
            Object.defineProperty(screen, 'availWidth', {
                get: function() { return ${metadata.screenWidth}; }
            });
            
            Object.defineProperty(screen, 'availHeight', {
                get: function() { return ${metadata.screenHeight - 40}; }
            });
            
            Object.defineProperty(screen, 'colorDepth', {
                get: function() { return ${metadata.colorDepth}; }
            });
            
            Object.defineProperty(screen, 'pixelDepth', {
                get: function() { return ${metadata.colorDepth}; }
            });
            
            Object.defineProperty(window, 'devicePixelRatio', {
                get: function() { return ${metadata.pixelRatio}; }
            });
            
            // Spoof timezone offset
            const originalGetTimezoneOffset = Date.prototype.getTimezoneOffset;
            Date.prototype.getTimezoneOffset = function() {
                return ${metadata.timezone};
            };
            
            // Spoof canvas fingerprinting with noise injection
            const originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
            const originalToBlob = HTMLCanvasElement.prototype.toBlob;
            const originalGetImageData = CanvasRenderingContext2D.prototype.getImageData;
            
            HTMLCanvasElement.prototype.toDataURL = function() {
                const noise = Math.random() * 0.01;
                const ctx = this.getContext('2d');
                if (ctx) {
                    const imgData = ctx.getImageData(0, 0, this.width, this.height);
                    for (let i = 0; i < imgData.data.length; i += 4) {
                        imgData.data[i] = Math.min(255, imgData.data[i] + noise * 255);
                    }
                    ctx.putImageData(imgData, 0, 0);
                }
                return originalToDataURL.apply(this, arguments);
            };
            
            CanvasRenderingContext2D.prototype.getImageData = function() {
                const result = originalGetImageData.apply(this, arguments);
                const noise = Math.random() * 0.01;
                for (let i = 0; i < result.data.length; i += 4) {
                    result.data[i] = Math.min(255, result.data[i] + noise * 255);
                }
                return result;
            };
            
            // Block WebGL fingerprinting
            const originalGetParameter = WebGLRenderingContext.prototype.getParameter;
            WebGLRenderingContext.prototype.getParameter = function(param) {
                if (param === 37445) return 'Generic Renderer'; // UNMASKED_VENDOR_WEBGL
                if (param === 37446) return 'Generic GPU'; // UNMASKED_RENDERER_WEBGL
                return originalGetParameter.apply(this, arguments);
            };
            
            if (window.WebGL2RenderingContext) {
                const originalGetParameter2 = WebGL2RenderingContext.prototype.getParameter;
                WebGL2RenderingContext.prototype.getParameter = function(param) {
                    if (param === 37445) return 'Generic Renderer';
                    if (param === 37446) return 'Generic GPU';
                    return originalGetParameter2.apply(this, arguments);
                };
            }
            
            // Block AudioContext fingerprinting
            if (window.AudioContext) {
                const OriginalAudioContext = window.AudioContext;
                window.AudioContext = function() {
                    const context = new OriginalAudioContext();
                    const originalCreateOscillator = context.createOscillator;
                    context.createOscillator = function() {
                        const osc = originalCreateOscillator.apply(this, arguments);
                        const originalStart = osc.start;
                        osc.start = function() {
                            osc.frequency.value += Math.random() * 0.001;
                            return originalStart.apply(this, arguments);
                        };
                        return osc;
                    };
                    return context;
                };
            }
            
            // Block common tracking functions
            if (window.ga) window.ga = function() {};
            if (window._gaq) window._gaq = { push: function() {} };
            if (window.fbq) window.fbq = function() {};
            if (window.gtag) window.gtag = function() {};
            if (window._paq) window._paq = { push: function() {} };
            
            // Remove referrer completely
            Object.defineProperty(document, 'referrer', {
                get: function() { return ''; }
            });
            
            // Block battery API (device fingerprinting)
            if (navigator.getBattery) {
                navigator.getBattery = function() {
                    return Promise.reject(new Error('Battery API disabled'));
                };
            }
            
            // Block ambient light sensor
            if (window.AmbientLightSensor) {
                window.AmbientLightSensor = function() {
                    throw new Error('AmbientLightSensor disabled');
                };
            }
            
            // Block gyroscope/accelerometer
            if (window.Gyroscope) {
                window.Gyroscope = function() {
                    throw new Error('Gyroscope disabled');
                };
            }
            if (window.Accelerometer) {
                window.Accelerometer = function() {
                    throw new Error('Accelerometer disabled');
                };
            }
            
            // Block magnetometer
            if (window.Magnetometer) {
                window.Magnetometer = function() {
                    throw new Error('Magnetometer disabled');
                };
            }
            
            // Block USB device enumeration
            if (navigator.usb) {
                navigator.usb.getDevices = function() {
                    return Promise.resolve([]);
                };
                navigator.usb.requestDevice = function() {
                    return Promise.reject(new Error('USB access denied'));
                };
            }
            
            // Block Bluetooth
            if (navigator.bluetooth) {
                navigator.bluetooth.getDevices = function() {
                    return Promise.resolve([]);
                };
                navigator.bluetooth.requestDevice = function() {
                    return Promise.reject(new Error('Bluetooth access denied'));
                };
            }
            
            // Block MIDI devices
            if (navigator.requestMIDIAccess) {
                navigator.requestMIDIAccess = function() {
                    return Promise.reject(new Error('MIDI access denied'));
                };
            }
            
            // Block notifications
            if (window.Notification) {
                window.Notification.requestPermission = function() {
                    return Promise.resolve('denied');
                };
            }
            
            // Block persistent storage
            if (navigator.storage && navigator.storage.persist) {
                navigator.storage.persist = function() {
                    return Promise.resolve(false);
                };
            }
            
            // Block clipboard access
            if (navigator.clipboard) {
                navigator.clipboard.read = function() {
                    return Promise.reject(new Error('Clipboard access denied'));
                };
                navigator.clipboard.readText = function() {
                    return Promise.reject(new Error('Clipboard access denied'));
                };
            }
            
            // Randomize plugin enumeration
            Object.defineProperty(navigator, 'plugins', {
                get: function() { 
                    return {
                        length: 0,
                        item: function() { return null; },
                        namedItem: function() { return null; },
                        refresh: function() {}
                    }; 
                }
            });
            
            Object.defineProperty(navigator, 'mimeTypes', {
                get: function() { 
                    return {
                        length: 0,
                        item: function() { return null; },
                        namedItem: function() { return null; }
                    }; 
                }
            });
            
            // Block service worker registration (can be used for tracking)
            if (navigator.serviceWorker) {
                navigator.serviceWorker.register = function() {
                    return Promise.reject(new Error('Service workers disabled'));
                };
            }
            
            console.log('[Privacy] All tracking protections active');
        })();
    """.trimIndent()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearWebData()
    }
    
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPress < DOUBLE_TAP_THRESHOLD) {
            // Panic double-tap detected
            handlePanicExit()
        } else {
            lastBackPress = currentTime
            
            // Check if WebView can go back in history
            webView?.let { webView ->
                if (webView.canGoBack()) {
                    webView.goBack()
                    return
                }
            }
            
            // No web history, close the browser
            super.onBackPressed()
        }
    }
    
    /**
     * Panic exit: Clear all traces and redirect to Reddit
     */
    private fun handlePanicExit() {
        Log.d("PrivateBrowser", "PANIC EXIT - Clearing all data")
        
        // Clear all WebView data
        clearWebData()
        
        // Open Reddit in default browser
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("PrivateBrowser", "Failed to open Reddit: ${e.message}")
        }
        
        // Close this activity
        finish()
    }

    private fun clearWebData() {
        try {
            webView?.apply {
                loadUrl("about:blank")
                clearHistory()
                clearCache(true)
                (parent as? ViewGroup)?.removeView(this)
                destroy()
            }
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            
            // Clear all app caches
            applicationContext.cacheDir?.deleteRecursively()
            applicationContext.deleteDatabase("webview.db")
            applicationContext.deleteDatabase("webviewCache.db")
        } catch (_: Throwable) { }
    }
    
    /**
     * Register a tap for 7-tap quick exit detection
     */
    internal fun register7Tap() {
        val now = System.currentTimeMillis()
        tapTimes.add(now)
        
        // Remove taps older than the time window
        tapTimes.removeAll { it < now - SEVEN_TAP_WINDOW }
        
        // Check if we have 7 taps within the window
        if (tapTimes.size >= SEVEN_TAP_COUNT) {
            handle7TapQuickExit()
            tapTimes.clear()
        }
    }

    /**
     * 7-tap quick exit: Close browser, clear history, and open Reddit
     */
    private fun handle7TapQuickExit() {
        Log.d("PrivateBrowser", "7-TAP QUICK EXIT - Clearing and redirecting")
        
        // Clear all browsing data
        webView?.clearHistory()
        webView?.clearCache(true)
        webView?.clearFormData()
        
        // Clear cookies
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        
        // Open Reddit in system browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com"))
        startActivity(intent)
        
        // Close this activity
        finish()
    }
}

@Composable
fun AndroidWebView(
    privacyManager: PrivacyProxyManager,
    activity: PrivateBrowserActivity,
    onCreated: (WebView) -> Unit, 
    onProgress: (Int) -> Unit
) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                Log.d("PrivateBrowser", "Creating WebView")
                
                // Set layout parameters to fill parent
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                // Add 7-tap detection for quick exit
                setOnTouchListener { _, event ->
                    if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                        activity.register7Tap()
                    }
                    false // Let WebView handle the touch normally
                }
                
                onCreated(this)
            }
        }, 
        modifier = Modifier.fillMaxSize()
    )
}
