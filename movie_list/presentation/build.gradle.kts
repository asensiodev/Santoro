plugins {
    `android-library`
    `kotlin-android`
}

apply<BaseGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_movie_list_presentation"
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    //implementation(project(Modules.core))
    androidX()
    compose()
    daggerHilt()
    test()
}