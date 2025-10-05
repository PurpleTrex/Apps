plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.gaia.maps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gaia.maps"
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
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

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // OSMDroid for offline maps (OpenStreetMap)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Routing (GraphHopper for offline routing)
    implementation("com.graphhopper:graphhopper-core:8.0")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Local database for map tiles and user data
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    
    // JSON parsing for user reports
    implementation("com.google.code.gson:gson:2.10.1")
    
    // WorkManager for background downloads
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
