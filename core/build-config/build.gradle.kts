plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.jvm.hilt)
}

android {
    namespace = "com.asensiodev.core.buildconfig"
    buildFeatures {
        buildConfig = true
    }
}
