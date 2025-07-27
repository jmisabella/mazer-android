plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" // Match Kotlin version
}

android {
    namespace = "com.jmisabella.mazeq"
    compileSdk = 36

    ndkVersion = "29.0.13599879"

    defaultConfig {
        applicationId = "com.jmisabella.mazeq"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64") // DEVELOP
            // abiFilters += listOf("armeabi-v7a", "arm64-v8a") // RELEASE
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
}

//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" // Adjust to match Kotlin version
//}
//
//android {
//    namespace = "com.jmisabella.mazeq"
//    compileSdk = 36
//
//    ndkVersion = "29.0.13599879"
//
//    defaultConfig {
//        applicationId = "com.jmisabella.mazeq"
//        minSdk = 24
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"
//
//        ndk {
//            abiFilters += listOf("arm64-v8a", "x86_64") // DEVELOP
//            // abiFilters += listOf("armeabi-v7a", "arm64-v8a") // RELEASE
//        }
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//        }
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//    buildFeatures {
//        compose = true
//    }
//    sourceSets {
//        main {
//            jniLibs.srcDirs("src/main/jniLibs")
//        }
//    }
//}
//
//dependencies {
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//    implementation("androidx.compose.material:material-icons-extended:1.6.8")
//}
//
////plugins {
////    alias(libs.plugins.android.application)
////    alias(libs.plugins.kotlin.android)
////    alias(libs.plugins.kotlin.compose)
////}
////
////android {
////    namespace = "com.jmisabella.mazeq"
////    compileSdk = 36
////
////    ndkVersion = "29.0.13599879"
////
////    defaultConfig {
////        applicationId = "com.jmisabella.mazeq"
////        minSdk = 24
////        targetSdk = 36
////        versionCode = 1
////        versionName = "1.0"
////
////        ndk {
////            abiFilters += listOf("arm64-v8a", "x86_64") // DEVELOP
//////            abiFilters += listOf("armeabi-v7a", "arm64-v8a") // RELEASE
////        }
////
////        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
////    }
////    externalNativeBuild {
////        cmake {
////            path = file("src/main/cpp/CMakeLists.txt")
////        }
////    }
////
////    buildTypes {
////        release {
////            isMinifyEnabled = false
////            proguardFiles(
////                getDefaultProguardFile("proguard-android-optimize.txt"),
////                "proguard-rules.pro"
////            )
////        }
////    }
////    compileOptions {
////        sourceCompatibility = JavaVersion.VERSION_11
////        targetCompatibility = JavaVersion.VERSION_11
////    }
////    kotlinOptions {
////        jvmTarget = "11"
////    }
////    buildFeatures {
////        compose = true
////    }
////}
////
////dependencies {
////
////    implementation(libs.androidx.core.ktx)
////    implementation(libs.androidx.lifecycle.runtime.ktx)
////    implementation(libs.androidx.activity.compose)
////    implementation(platform(libs.androidx.compose.bom))
////    implementation(libs.androidx.ui)
////    implementation(libs.androidx.ui.graphics)
////    implementation(libs.androidx.ui.tooling.preview)
////    implementation(libs.androidx.material3)
////    testImplementation(libs.junit)
////    androidTestImplementation(libs.androidx.junit)
////    androidTestImplementation(libs.androidx.espresso.core)
////    androidTestImplementation(platform(libs.androidx.compose.bom))
////    androidTestImplementation(libs.androidx.ui.test.junit4)
////    debugImplementation(libs.androidx.ui.tooling)
////    debugImplementation(libs.androidx.ui.test.manifest)
////    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
////    implementation("androidx.compose.material:material-icons-extended:1.6.8")
////}
