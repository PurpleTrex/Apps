package com.example.notepad.ui.browser

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class PrivateBrowserActivity: ComponentActivity() {
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var address by remember { mutableStateOf(TextFieldValue("https://duckduckgo.com")) }
                    var loading by remember { mutableStateOf(false) }

                    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        TopAppBar(
                            title = { Text("Private Browser") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                            },
                            actions = {
                                IconButton(onClick = { webView?.reload() }) { Icon(Icons.Filled.Refresh, contentDescription = "Reload") }
                            }
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            placeholder = { Text("Enter URL or search") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    val url = normalizeUrl(address.text)
                                    loading = true
                                    webView?.loadUrl(url)
                                }) { Icon(Icons.Filled.ArrowForward, contentDescription = "Go") }
                            }
                        )
                        Box(Modifier.weight(1f)) {
                            AndroidWebView(
                                onCreated = { w ->
                                    webView = w
                                    with(w.settings) {
                                        javaScriptEnabled = true
                                        domStorageEnabled = false
                                        cacheMode = WebSettings.LOAD_NO_CACHE
                                        setSupportZoom(true)
                                        builtInZoomControls = true
                                        displayZoomControls = false
                                    }
                                    w.setBackgroundColor(Color.BLACK)
                                    CookieManager.getInstance().setAcceptCookie(false)
                                    w.webViewClient = object: WebViewClient() {
                                    }
                                    w.webChromeClient = object: WebChromeClient() {}
                                    w.loadUrl(address.text)
                                },
                                onProgress = { p -> loading = p < 100 }
                            )
                            if (loading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                            }
                        }
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

    override fun onDestroy() {
        super.onDestroy()
        clearWebData()
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
            // Additional clearing can be implemented if needed (databases, etc.)
        } catch (_: Throwable) { }
    }
}

@Composable
private fun AndroidWebView(onCreated: (WebView) -> Unit, onProgress: (Int) -> Unit) {
    AndroidViewWithLifecycle(onCreated = onCreated, onProgress = onProgress)
}

@Composable
private fun AndroidViewWithLifecycle(onCreated: (WebView) -> Unit, onProgress: (Int) -> Unit) {
    androidx.compose.ui.viewinterop.AndroidView(factory = { ctx ->
        WebView(ctx).apply {
            settings.userAgentString = settings.userAgentString + " Incog/1.0"
            setBackgroundColor(Color.BLACK)
            onCreated(this)
        }
    }, update = { })
}

// Removed custom Icons wrapper – using material icons directly
