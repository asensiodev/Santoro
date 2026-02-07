plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.settings.impl"
}

dependencies {
    implementation(projects.feature.settings.api)

    implementation(projects.core.auth)
    implementation(projects.core.domain)
    implementation(projects.core.ui)

    implementation(libs.bundles.coil)
}
