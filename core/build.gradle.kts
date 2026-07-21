// Pure Kotlin module with the game logic (dice rules, menu, statistics).
// No Android dependencies - shared by the phone app (:app) and the watch
// app (:wear), and fully unit-testable on the JVM.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Multiplatform-friendly (also Kotlin/JS) so the wire format the watch
    // syncs on can be reused by the future PWA.
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
