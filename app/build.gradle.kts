plugins { id("com.android.application") }

android {
    namespace = "com.example.minesweeper"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "com.example.minesweeper"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
