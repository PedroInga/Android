plugins {
    id("com.android.application")
}

android {
    namespace = "com.intoc.sistemacitasmedicas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.intoc.sistemacitasmedicas"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)

    // Material Design
    implementation(libs.material)

    // Navigation Component
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Retrofit + Gson (Conexión API REST)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // RecyclerView y CardView
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Google Maps
    implementation(libs.play.services.maps)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
}
