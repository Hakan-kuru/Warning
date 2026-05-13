import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}


android {
    namespace = "com.hakankuru.yanimda"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hakankuru.yanimda"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.4.0"

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildToolsVersion = "35.0.1"
}

dependencies {
    // google-maps-compose -> google.maps.compose
    // google-play-services-maps -> google.play.services.maps
    // libs... üzerinden değil, direkt adres üzerinden ekleyelim
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.maps.android:maps-compose:4.3.3")

    // Konum servisleri için (Opsiyonel ama önerilir)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // refresh için
    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // JSON converter (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")// En güncel stabil sürüm
    // OkHttp for HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Import the Firebase BoM
//    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))


    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")

    // TODO: Add the dependencies for any other Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.appcompat)
    // TODO: implementation(libs.firebase.functions.ktx)
    kapt(libs.room.compiler)


    // Firebase ürünleri (BOM varsa versiyon yazma!)
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    //firebase auth
//    implementation("com.google.firebase:firebase-auth-ktx")
//    implementation("com.google.firebase:firebase-messaging:20.1.6")


    // hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // build.gradle(:app)

    // Jetpack Compose BOM (tavsiye edilen)

    // Compose UI
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)

    implementation("io.coil-kt:coil-compose:2.6.0")
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Diğer
    //implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
