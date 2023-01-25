import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.nft.gallery"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.solanamobile.mintyfresh"
        minSdk = 26
        targetSdk = 33
        versionCode = 5
        versionName = "v0.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.addAll(listOf("en"))

        val properties = Properties()
        if (project.rootProject.file("local.properties").exists()) {
            properties.load(FileInputStream("local.properties"))
        }

        buildConfigField(
            "String",
            "NFTSTORAGE_KEY",
            "\"${properties.getProperty("NFTSTORAGE_API_KEY")}\""
        )
        buildConfigField("String", "NFTSTORAGE_API_BASE_URL", "\"https://api.nft.storage/\"")

        buildConfigField(
            "String",
            "MINTY_FRESH_CREATOR_PDA",
            "\"3QFrGD1VHLKqeuCWUt6jgcM5ZESBzhY9dUvZcDbZFisB\""
        )

        buildConfigField("String", "SOLANA_RPC_URL", "\"https://api.mainnet-beta.solana.com\"")
        buildConfigField(
            "com.solana.mobilewalletadapter.clientlib.RpcCluster",
            "RPC_CLUSTER",
            "com.solana.mobilewalletadapter.clientlib.RpcCluster.MainnetBeta.INSTANCE"
        )

        buildConfigField("String", "SHADOW_DRIVE_API_BASE_URL", "\"https://shadow-storage.genesysgo.net\"")

        buildConfigField("String", "IDENTITY_URI", "\"https://solanamobile.com\"")
        buildConfigField("String", "IDENTITY_ICO", "\"favicon.ico\"")
        buildConfigField("String", "IDENTITY_NAME", "\"Minty Fresh\"")
    }

    signingConfigs {
        create("release") {
            val storePath = findProperty("apkSigningKeystorePath") as String?
            storeFile = storePath?.let { file(it) }
            storePassword = findProperty("apkSigningKeystorePassword") as String?
            keyAlias = findProperty("apkSigningKeyAlias") as String?
            keyPassword = findProperty("apkSigningKeyPassword") as String?
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "SOLANA_RPC_URL", "\"https://api.mainnet-beta.solana.com\"")
            buildConfigField(
                "com.solana.mobilewalletadapter.clientlib.RpcCluster",
                "RPC_CLUSTER",
                "com.solana.mobilewalletadapter.clientlib.RpcCluster.MainnetBeta.INSTANCE"
            )

            val releaseSigningConfig = signingConfigs["release"]
            if (releaseSigningConfig.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    kapt {
        correctErrorTypes = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.material3:material3:1.1.0-alpha04")
    implementation("androidx.compose.ui:ui:1.3.3")
    implementation("androidx.compose.ui:ui-tooling:1.3.3")
    implementation("androidx.customview:customview-poolingcontainer:1.0.0")
    implementation("androidx.compose.foundation:foundation:1.4.0-alpha04")
    implementation("androidx.compose.material:material:1.4.0-alpha04")
    implementation("androidx.compose.material:material-icons-core:1.4.0-alpha04")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-alpha04")
    implementation("androidx.activity:activity-compose:1.7.0-alpha03")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.3")
    implementation("androidx.paging:paging-compose:1.0.0-alpha17")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:1.0.3")

    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")
    implementation("com.google.accompanist:accompanist-placeholder:0.28.0")
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.28.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")

    implementation("com.google.android.exoplayer:exoplayer:2.18.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.portto.solana:web3:0.1.3")

    implementation("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation("androidx.room:room-runtime:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")

    kapt("com.google.dagger:hilt-compiler:2.44.2")
    implementation("com.google.dagger:dagger:2.44.2")
    kapt("com.google.dagger:dagger-compiler:2.44.2")
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android:2.44.2")

    implementation("com.github.bumptech.glide:compose:1.0.0-alpha.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val cameraxVersion = "1.2.0"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-video:${cameraxVersion}")

    implementation("androidx.camera:camera-view:${cameraxVersion}")
    implementation("androidx.camera:camera-extensions:${cameraxVersion}")

    // Guava & Gradle interact badly, and this prevents
    // "cannot access ListenableFuture" errors [internal b/157225611].
    // More info: https://blog.gradle.org/guava
    implementation("com.google.guava:guava:24.1-jre")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    // SolanaKT & Metaplex
    implementation("com.github.metaplex-foundation:SolanaKT:2.0.0")
    implementation("com.github.metaplex-foundation:metaplex-android:1.3.0b3")
}