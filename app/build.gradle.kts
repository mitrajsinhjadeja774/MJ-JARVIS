plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mj.jarvis"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.mj.jarvis"
        minSdk = 26
        targetSdk = 35
        versionCode = 140
        versionName = "1.4.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
