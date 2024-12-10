import java.util.Properties

plugins {
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.asensiodev.santoro"

    signingConfigs {
        create("release") {
            val keystoreFile = project.rootProject.file("local.properties")
            val properties = Properties()
            properties.load(keystoreFile.inputStream())
            keyAlias = properties.getProperty("KEYSTORE_KEY_ALIAS")
            storeFile = file(properties.getProperty("KEYSTORE_STORE_FILE"))
            storePassword = properties.getProperty("KEYSTORE_STORE_PASSWORD")
            keyPassword = properties.getProperty("KEYSTORE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false // TODO: Change to true
            isShrinkResources = false // TODO: Change to true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    defaultConfig {
        applicationId = "com.asensiodev.santoro"
    }
}

dependencies {
    implementation(projects.core.stringResources)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.remote.config)
    implementation(libs.firebase.crashlytics)
}

tasks
    .named("preBuild")
    .configure {
        dependsOn(":copyGitHooks")
    }
