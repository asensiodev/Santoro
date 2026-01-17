plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)
}
