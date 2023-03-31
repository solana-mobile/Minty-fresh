plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.solanamobile.mintyfresh.networkinterfaceimpl"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "SOLANA_RPC_URL", "\"https://api.devnet.solana.com\"")
        buildConfigField(
            "com.solana.mobilewalletadapter.clientlib.RpcCluster",
            "RPC_CLUSTER",
            "com.solana.mobilewalletadapter.clientlib.RpcCluster.Devnet.INSTANCE"
        )
    }

    buildTypes {
        release {
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
    implementation(project(":libs:networkInterface"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.android.material:material:1.8.0")

    kapt("com.google.dagger:hilt-compiler:2.44.2")
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android:2.44.2")

    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:1.1.0")

    // SolanaKT & Metaplex
    implementation("com.github.metaplex-foundation:SolanaKT:2.0.1")
    implementation("com.github.metaplex-foundation:metaplex-android:1.4.1")  {
        exclude("com.github.metaplex-foundation.kborsh", "kborsh-android")
    }

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}