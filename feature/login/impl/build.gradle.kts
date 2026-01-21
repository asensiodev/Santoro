plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.feature.login.impl"
}

dependencies {
    implementation(projects.feature.login.api)
    implementation(projects.core.auth)
    implementation(projects.core.ui)
}
