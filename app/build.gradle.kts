plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23)) // Java 17로 설정
    }
}

android {
    namespace = "com.example.financial_chat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.financial_chat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/native-image/native-image.properties"
            excludes += "META-INF/native-image/reflect-config.json" // 추가
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
//    implementation("org.mongodb:bson-kotlinx:5.2.0")
//    implementation("org.litote.kmongo:kmongo-serialization:4.10.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
//    implementation("org.mongodb:mongodb-driver-kotlin-sync:5.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // 최신 버전으로 설정
//    implementation("org.mongodb:mongodb-driver-legacy:4.7.0")
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics:21.2.0")
    implementation("com.google.firebase:firebase-messaging:23.1.0")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


//    implementation("org.tensorflow:tensorflow-lite:2.9.0")
//    implementation("org.tensorflow:tensorflow-lite-support:0.3.1")
//    implementation("com.github.google:sentencepiece:0.1.94")

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(kotlin("script-runtime"))
}