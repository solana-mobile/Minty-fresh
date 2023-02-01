plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.solanamobile.mintyfresh.networkinterface"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "MINTY_FRESH_CREATOR_PDA",
            "\"3QFrGD1VHLKqeuCWUt6jgcM5ZESBzhY9dUvZcDbZFisB\""
        )
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
    implementation(project(":libs:persistence"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.android.material:material:1.8.0")

    implementation("com.github.metaplex-foundation:SolanaKT:2.0.0")
    implementation("com.github.metaplex-foundation:metaplex-android:1.3.0b3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}