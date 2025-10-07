plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.notepad"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.notepad"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xcontext-receivers"
        )
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DataStore (for preferences like sort)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Accompanist (optional) - in-app browser aids (not using custom tabs here) kept minimal
    // implementation("com.google.accompanist:accompanist-webview:0.34.0") // optional

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Coil for image attachments
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Biometric auth
    implementation("androidx.biometric:biometric:1.1.0")

    // Encryption (AndroidX Security Crypto)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Markdown (CommonMark)
    implementation("org.commonmark:commonmark:0.21.0")

    // WorkManager (background tasks potential)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Material Components (provides XML Theme.Material3.* parents referenced in themes.xml)
    implementation("com.google.android.material:material:1.11.0")
    
    // Core library desugaring for java.time APIs on API < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // OkHttp for DNS-over-HTTPS (DoH) privacy protection
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
    
    // Note: Tor routing via Orbot app (lightweight, user controls Tor)
    // App auto-detects and prompts to install Orbot if needed
    
    // Local AI Chat - Using OkHttp to interface with local inference server
    // We'll implement a lightweight GGML-based solution
    
    // JSON parsing for AI responses
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Download manager for AI models
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
