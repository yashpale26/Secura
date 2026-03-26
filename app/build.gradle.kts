plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.secura"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.secura"
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
    implementation(libs.viewpager2)
    implementation(libs.circleindicator)
    implementation(libs.material.v150)   // keep whichever Material version you want
    implementation(libs.cardview)
    implementation(libs.circleimageview)
    implementation(libs.lottie)

    implementation("com.google.code.gson:gson:2.13.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ✅ CameraX (include the missing Camera2 backend)
    implementation(libs.camera.core)
    implementation(libs.camera.lifecycle)
    implementation("androidx.camera:camera-camera2:1.4.2")   // <--- add this
    implementation("androidx.camera:camera-view:1.4.2")      // optional
    implementation("androidx.camera:camera-extensions:1.4.2")// optional

    implementation("androidx.exifinterface:exifinterface:1.4.1")

    implementation(libs.lifecycle.service)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
