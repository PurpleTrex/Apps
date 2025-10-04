# Private Browser - Privacy & Encryption Features

## 🔒 Privacy Protection Layers

### 1. DNS-over-HTTPS (DoH)
- **Enabled by default** for all browser traffic
- Encrypts DNS queries to prevent ISP/network tracking
- Uses Cloudflare's privacy-focused DNS (1.1.1.1)
- Prevents DNS hijacking and manipulation

### 2. Tor Integration (Optional - Requires Orbot)
For maximum anonymity, install **Orbot** (Tor for Android):

#### Setup Instructions:
1. **Install Orbot** from F-Droid or Google Play
2. **Enable Orbot** and start Tor
3. **Open PurplePad Private Browser** - it auto-detects Orbot
4. **Look for green lock icon** 🔒 showing "Encrypted connection via Tor"

#### What Tor Provides:
- ✅ IP address masking through 3+ relay hops
- ✅ Traffic encryption through multiple layers (onion routing)
- ✅ Geographic location obfuscation
- ✅ Prevents website fingerprinting (combined with our WebView hardening)
- ✅ Bypass censorship and geo-restrictions

### 3. WebView Hardening
Automatically configured privacy settings:
- ❌ Cookies disabled
- ❌ DOM storage disabled
- ❌ Cache disabled (LOAD_NO_CACHE)
- ❌ Geolocation disabled
- ❌ Database storage disabled
- ❌ App cache disabled
- ✅ All history/cache cleared on exit
- ✅ Custom user agent (reduces fingerprinting)

### 4. No Tracking
- No analytics
- No telemetry
- No user profiling
- No browsing history saved
- No search queries logged

## 📱 Usage

### Basic Privacy (No Orbot)
- DNS-over-HTTPS enabled automatically
- Prevents DNS tracking
- Clears all data on exit
- Good for: Private searches, avoiding tracking cookies

### Maximum Privacy (With Orbot)
1. Install & start Orbot
2. Open Private Browser
3. See "🔒 Encrypted connection via Tor" banner
4. Your IP is now hidden from websites
5. Good for: Anonymous browsing, whistleblowing, censored regions

## 🛡️ Technical Details

### DNS-over-HTTPS Implementation
```kotlin
// Uses OkHttp's DnsOverHttps module
val dohDns = DnsOverHttps.Builder()
    .client(bootstrapClient)
    .url("https://1.1.1.1/dns-query")
    .build()
```

### Tor SOCKS Proxy Detection
```kotlin
// Auto-detects Orbot running on localhost:9050
if (PrivacyProxyManager.isOrbotRunning()) {
    // Routes traffic through Tor
    PrivacyProxyManager.socksProxyHost = "127.0.0.1"
    PrivacyProxyManager.socksProxyPort = 9050
}
```

### WebView Proxy Injection
Attempts to inject SOCKS proxy into WebView using reflection (best-effort on Android):
```kotlin
PrivacyProxyManager.setWebViewProxy(webView, "127.0.0.1", 8118)
```

**Note**: WebView proxy injection may not work on all Android versions (system restrictions). For guaranteed Tor routing:
- Use Orbot's VPN mode (routes all device traffic through Tor), OR
- Use a custom HTTP proxy (Privoxy) that forwards to Tor SOCKS

## 🚨 Limitations & Disclaimers

### What This Browser DOES:
✅ Encrypts DNS queries (prevents ISP/network seeing what sites you visit)  
✅ Auto-routes through Tor if Orbot is running (hides your IP from websites)  
✅ Blocks cookies, tracking, and fingerprinting attempts  
✅ Clears all browsing data on exit (no forensic traces)  

### What This Browser CANNOT Do:
❌ **Cannot guarantee anonymity** without Orbot/Tor  
❌ **Cannot bypass advanced fingerprinting** (WebView still leaks some device info)  
❌ **Cannot protect against malware** on visited websites  
❌ **Cannot prevent correlation attacks** (if you log into accounts, you can be tracked)  
❌ **Not a replacement for Tor Browser** (which has extensive hardening)  

### Security Best Practices:
1. **Never log into personal accounts** while using Tor (defeats anonymity)
2. **Use HTTPS websites** (check for padlock in address bar)
3. **Don't download/run files** from untrusted sources
4. **Clear app data regularly** (Settings → Apps → PurplePad → Clear Data)
5. **For high-risk scenarios**, use desktop Tor Browser or Tails OS

## 🔧 Advanced Configuration

### Custom SOCKS Proxy (Manual)
Edit `PrivacyProxyManager.kt`:
```kotlin
PrivacyProxyManager.socksProxyHost = "your.proxy.host"
PrivacyProxyManager.socksProxyPort = 1080
```

### Alternative DoH Providers
Change in `PrivacyProxyManager.kt`:
```kotlin
private const val DOH_CLOUDFLARE = "https://1.1.1.1/dns-query"  // Default
// private const val DOH_GOOGLE = "https://dns.google/dns-query"
// private const val DOH_QUAD9 = "https://dns.quad9.net/dns-query"
```

### Increase Timeout (Slow Connections)
Adjust in `PrivacyProxyManager.kt`:
```kotlin
.connectTimeout(60, TimeUnit.SECONDS)  // Default: 30
.readTimeout(60, TimeUnit.SECONDS)
```

## 📚 Resources

- **Orbot Download**: https://guardianproject.info/apps/org.torproject.android/
- **Tor Project**: https://www.torproject.org/
- **DNS-over-HTTPS Info**: https://developers.cloudflare.com/1.1.1.1/encryption/dns-over-https/
- **WebView Privacy**: https://developer.android.com/reference/android/webkit/WebView

## 🆘 Troubleshooting

### "Tor not detected" (but Orbot is running)
- Ensure Orbot shows "Connected" status
- Check Orbot settings: "Start on Boot" enabled
- Restart PurplePad app
- Check Orbot logs for errors

### Slow browsing with Tor
- Normal (Tor routes through multiple relays worldwide)
- Wait 10-30 seconds for page loads
- Avoid large downloads
- Consider using "New Identity" in Orbot if stuck

### Websites blocking Tor
- Some sites block Tor exit nodes
- Use a different Tor circuit (Orbot → New Identity)
- Or temporarily disable Tor for that site (not recommended)

### WebView proxy warning
- "WebView proxy injection failed" → Traffic may not route through Tor
- Solution: Enable Orbot's VPN mode (routes all apps through Tor)
- Settings in Orbot → VPN Mode → Enable

## 🤝 Contributing

Want to improve privacy features? Consider adding:
- [ ] User-configurable DoH provider selection
- [ ] Built-in Tor circuit management
- [ ] WebRTC leak prevention
- [ ] Embedded Tor library (no Orbot dependency)
- [ ] Fingerprinting resistance (canvas, WebGL, fonts)
- [ ] HTTPS-everywhere enforcement
- [ ] Certificate pinning for DoH providers

---

**Remember**: Privacy is a spectrum. This browser provides strong privacy for most use cases, but true anonymity requires operational security (OPSEC) practices beyond technical tools.
