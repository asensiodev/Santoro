import java.util.Properties

plugins {
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
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
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    defaultConfig {
        applicationId = "com.asensiodev.santoro"
    }
}

dependencies {
    implementation(projects.santoro.feature.searchMovies.api)
    implementation(projects.santoro.feature.searchMovies.impl)
    implementation(projects.santoro.feature.watchedMovies.api)
    implementation(projects.santoro.feature.watchedMovies.impl)
    implementation(projects.santoro.feature.watchlist.api)
    implementation(projects.santoro.feature.watchlist.impl)
    implementation(projects.santoro.feature.movieDetail.api)
    implementation(projects.santoro.feature.movieDetail.impl)
    implementation(projects.santoro.library.remoteConfig.api)
    implementation(projects.santoro.library.remoteConfig.impl)

    implementation(projects.core.designSystem)
    implementation(projects.core.stringResources)
    implementation(projects.core.network)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

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
