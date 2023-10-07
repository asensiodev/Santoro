plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.noirsonora.santoro"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.noirsonora.santoro"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(AndroidX.coreKtx)
    //implementation(AndroidX.lifecycleRuntime)


    implementation(Compose.viewModelCompose)

    //implementation("androidx.activity:activity-compose:1.7.2")

    implementation(platform(Compose.composeBom))
    implementation(Compose.activityCompose)
    implementation(Compose.ui)
    implementation(Compose.uiGrapichs)
    implementation(Compose.uiToolingPreview)
    implementation(Compose.material3)


    testImplementation(Testing.junit4)
    testImplementation(Testing.junitAndroidExt)
    testImplementation(Testing.junitAndroidExt)

    androidTestImplementation(platform(Compose.composeBom))
    androidTestImplementation(Testing.composeUiTest)
    androidTestImplementation(Testing.espresso)
    debugImplementation(Compose.composeUiTooling)
    debugImplementation(Testing.composeTestManifest)
}