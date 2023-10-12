plugins {
    `android-library`
    `kotlin-android`
}

apply<BaseGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_core"
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    androidX()
    compose()
    daggerHilt()
    test()
}
