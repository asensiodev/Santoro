import versions.androidX
import versions.compose
import versions.daggerHilt
import versions.test

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.noirsonora.santoro"
    compileSdk = versions.ProjectConfig.compileSdk

    defaultConfig {
        applicationId = versions.ProjectConfig.appId
        minSdk = versions.ProjectConfig.minSdk
        targetSdk = versions.ProjectConfig.targetSdk
        versionCode = versions.ProjectConfig.versionCode
        versionName = versions.ProjectConfig.versionName

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
        kotlinCompilerExtensionVersion = versions.Compose.composeCompilerVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(versions.Modules.core))
    //implementation(project(Modules.movieListData))
    //implementation(project(Modules.movieListPresentation))
    androidX()
    compose()
    daggerHilt()
    test()
}