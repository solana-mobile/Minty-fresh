import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

val properties = Properties()
if (project.rootProject.file("local.properties").exists()) {
    properties.load(FileInputStream("local.properties"))
}

android {
    namespace = "com.solanamobile.mintyfresh.mintycore"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        //TODO: All of these values will come from the networking layer when that is refactored
        buildConfigField(
            "String",
            "NFTSTORAGE_KEY",
            "\"${properties.getProperty("NFTSTORAGE_API_KEY")}\""
        )
        buildConfigField("String", "API_BASE_URL", "\"https://api.nft.storage/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            buildConfigField(
                "String",
                "NFTSTORAGE_KEY",
                "\"${properties.getProperty("NFTSTORAGE_API_KEY")}\""
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.nft.storage/\"")
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
    implementation(project(":libs:persistence"))
    implementation(project(":libs:core"))
    implementation(project(":libs:networkInterface"))

    implementation("androidx.core:core-ktx:1.9.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
    kapt("com.google.dagger:hilt-compiler:2.44.2")
    implementation("com.google.dagger:dagger:2.44.2")
    kapt("com.google.dagger:dagger-compiler:2.44.2")
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android:2.44.2")

    // SolanaKT & Metaplex
    implementation("com.github.metaplex-foundation:SolanaKT:2.0.0")
    implementation("com.github.metaplex-foundation:metaplex-android:1.3.0b3")
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:1.0.4")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}