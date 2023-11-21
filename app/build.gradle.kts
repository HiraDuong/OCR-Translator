plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.img2text2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.img2text2"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


}

//text recognise
dependencies {
    // To recognize Latin script
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    // To recognize Chinese script
    implementation ("com.google.mlkit:text-recognition-chinese:16.0.0")

    // To recognize Devanagari script
    implementation ("com.google.mlkit:text-recognition-devanagari:16.0.0")

    // To recognize Japanese script
    implementation ("com.google.mlkit:text-recognition-japanese:16.0.0")

    // To recognize Korean script
    implementation ("com.google.mlkit:text-recognition-korean:16.0.0")
}

// text  recognize
dependencies {
    // To recognize Latin script
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    // To recognize Chinese script
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.0")

    // To recognize Devanagari script
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-devanagari:16.0.0")

    // To recognize Japanese script
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-japanese:16.0.0")

    // To recognize Korean script
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.0")
}
//translate
dependencies {
    // ...

    implementation("com.google.mlkit:translate:17.0.2")
}
