plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services) // Plugin của Google Services
}

android {
    namespace = "com.example.app_music"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.app_music"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Khai báo Firebase BOM và Analytics
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Thư viện Firebase Auth (Đăng nhập/Đăng ký)
    implementation("com.google.firebase:firebase-auth")

    // ĐÃ THÊM: Thư viện Firebase Realtime Database (Lưu danh sách bài hát)
    implementation("com.google.firebase:firebase-database")

    // ĐÃ THÊM: Thư viện load ảnh mạng siêu tốc (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
}