
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.charityapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.charityapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    // AndroidX
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)

    // Material Design
    implementation(libs.material)

    // Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Firebase Authentication
    implementation(libs.firebase.auth)

    // Firebase Cloud Messaging
    implementation(libs.firebase.messaging)

    // Firebase Cloud Firestore
    implementation(
        "com.google.firebase:firebase-firestore"
    )

    // Unit Testing
    testImplementation(libs.junit)

    // Android Instrumentation Testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
