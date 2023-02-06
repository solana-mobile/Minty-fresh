plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
}

android {
    namespace = "com.solanamobile.mintyfresh.mymints"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":ui:commonComposable"))
    implementation(project(":libs:mintycore"))
    implementation(project(":libs:persistence"))
    implementation(project(":libs:networkInterface"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.compose.foundation:foundation:1.4.0-alpha04")
    implementation("androidx.compose.material3:material3:1.1.0-alpha04")

    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")
    implementation("com.google.accompanist:accompanist-placeholder:0.28.0")
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.28.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")

    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    kapt("com.google.dagger:hilt-compiler:2.44.2")
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android:2.44.2")

    implementation("com.github.bumptech.glide:compose:1.0.0-alpha.1")
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:1.0.4")

    // SolanaKT & Metaplex
    implementation("com.github.metaplex-foundation:SolanaKT:2.0.0")
    implementation("com.github.metaplex-foundation:metaplex-android:1.3.4") {
        exclude("com.github.metaplex-foundation.kborsh", "kborsh-android")
    }

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}