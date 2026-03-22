import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val geminiApiKey: String = localProperties.getProperty("GEMINI_API_KEY")?.trim().orEmpty()

android {
    namespace = "com.example.qrsafe" // VERIFICĂ: Trebuie să fie la fel ca în google-services.json
    compileSdk = 34 // Folosim 34, e cel mai stabil acum

    defaultConfig {
        applicationId = "com.example.qrsafe"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Gemini: add GEMINI_API_KEY=your_key to local.properties (never commit keys)
        buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Compatibil cu Kotlin 1.9.24
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.safetynet)
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.1")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Scanner (Vechi)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    //implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:core:3.5.2")
    implementation("androidx.appcompat:appcompat:1.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.airbnb.android:lottie-compose:6.0.0")

    // Pentru a descărca text/XML simplu
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // Pentru a încărca imagini (Coil)
    implementation("io.coil-kt:coil-compose:2.5.0")
    // Scanare QR
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Navigare și UI
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // --- CAMERAX (OBLIGATORIU PENTRU ECRANUL NOU DE SCANARE) ---
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // --- GUAVA (OBLIGATORIU PENTRU EROAREA ListenableFuture) ---
    implementation("com.google.guava:guava:31.1-android")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // SDK-ul oficial Google AI pentru Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

}