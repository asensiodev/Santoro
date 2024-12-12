plugins {
    alias(libs.plugins.convention.android.library.compose)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.core.designsystem"
}

dependencies {
    implementation(projects.core.stringResources)
    implementation(libs.androidx.ui.text.google.fonts)
}
