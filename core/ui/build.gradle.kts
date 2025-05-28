plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.asensiodev.ui"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
}
