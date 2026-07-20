plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// The app version is derived from the git tag that triggered the release
// build (see .github/workflows/android.yml's "Build APK" step), not hand-
// bumped here. CI passes RELEASE_TAG="v1.5" (or "v1.5.2") as an env var
// when building from a `v*` tag; local/non-tag builds (plain `gradlew
// assembleRelease`, pushes to main, PRs) fall back to a fixed dev version.
// versionCode = major*10_000 + minor*100 + patch, which keeps increasing
// as long as tags increase - required for clean app updates.
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
    namespace = "de.gyrosbande.dice"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.gyrosbande.dice"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    // CI signing: when the environment variables (GitHub secrets) are set,
    // the release keystore is used - otherwise the debug key, so the build
    // also works without secrets (forks, local builds).
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
}
