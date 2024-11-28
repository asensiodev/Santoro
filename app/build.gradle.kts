plugins {
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.santoro"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    defaultConfig {
        applicationId = "com.asensiodev.santoro"
    }
}

dependencies {
    implementation(projects.core.stringResources)
}

tasks.named("preBuild")
    .configure {
        dependsOn(":copyGitHooks")
    }
