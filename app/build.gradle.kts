plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.noirsonora.santoro"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        applicationId = ProjectConfig.appId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName

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
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.composeCompilerVersion
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