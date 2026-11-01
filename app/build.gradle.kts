
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "it.volumesmart.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.volumesmart.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
