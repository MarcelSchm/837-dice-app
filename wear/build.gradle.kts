plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Version derived from the release tag, same scheme as :app (keep in sync).
val releaseTagMatch = System.getenv("RELEASE_TAG")
    ?.let { Regex("""^v(\d+)\.(\d+)(?:\.(\d+))?$""").find(it) }

val appVersionName: String = releaseTagMatch?.let {
    val major = it.groupValues[1]
    val minor = it.groupValues[2]
    val patch = it.groupValues[3].toIntOrNull() ?: 0
    if (patch == 0) "$major.$minor" else "$major.$minor.$patch"
} ?: "0.0-dev"

val appVersionCode: Int = releaseTagMatch?.let {
    val major = it.groupValues[1].toInt()
    val minor = it.groupValues[2].toInt()
    val patch = it.groupValues[3].toIntOrNull() ?: 0
    major * 10_000 + minor * 100 + patch
} ?: 1

android {
    namespace = "de.gyrosbande.dice.wear"
    compileSdk = 36

    defaultConfig {
        // Same application id as the phone app - required for Play-side
        // pairing and the future Data Layer sync (phase 2). Watch and
        // phone are different devices, so there is no install conflict.
        applicationId = "de.gyrosbande.dice"
        minSdk = 30 // Wear OS 3+
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    // CI signing with debug fallback, same as :app.
    val ciKeystore = System.getenv("KEYSTORE_FILE")
    if (ciKeystore != null) {
        signingConfigs {
            create("release") {
                storeFile = file(ciKeystore)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (ciKeystore != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
}
