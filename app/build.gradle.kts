plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.daily.cetaring"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.caterhub.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    val releaseKeystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
    val releaseKeystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
    val releaseKeyAlias = System.getenv("ANDROID_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")
    val hasReleaseSigning = !releaseKeystorePath.isNullOrBlank() &&
        !releaseKeystorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    val productionApiBaseUrl = "https://caterhub-2-production.up.railway.app/api/v1/"
    val developmentApiBaseUrl = System.getenv("CATERHUB_ANDROID_DEV_API_URL")
        ?: productionApiBaseUrl
    val prodApiBaseUrl = System.getenv("CATERHUB_ANDROID_PROD_API_URL")
        ?: productionApiBaseUrl

    buildTypes {
        debug {
            buildConfigField("String", "ENVIRONMENT", "\"DEVELOPMENT\"")
            buildConfigField("String", "API_BASE_URL", "\"$developmentApiBaseUrl\"")
            buildConfigField("boolean", "ALLOW_CLEARTEXT_LOCAL_TRAFFIC", developmentApiBaseUrl.startsWith("http://").toString())
            manifestPlaceholders["usesCleartextTraffic"] = developmentApiBaseUrl.startsWith("http://").toString()
        }

        create("stage") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
            buildConfigField("String", "API_BASE_URL", "\"$prodApiBaseUrl\"")
            buildConfigField("boolean", "ALLOW_CLEARTEXT_LOCAL_TRAFFIC", "false")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
        }

        release {
            isMinifyEnabled = false
            buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
            buildConfigField("String", "API_BASE_URL", "\"$prodApiBaseUrl\"")
            buildConfigField("boolean", "ALLOW_CLEARTEXT_LOCAL_TRAFFIC", "false")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Jetpack Compose & Material 3
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Retrofit & OkHttp for API calls
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // DataStore for local storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    annotationProcessor("com.google.dagger:hilt-compiler:2.50")

    // Firebase for notifications
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation("com.google.firebase:firebase-analytics-ktx:21.6.2")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.3.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}