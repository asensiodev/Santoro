plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.securestorage.impl"
}

dependencies {
    implementation(projects.library.secureStorage.api)
    implementation(libs.encrypted.shared.prefs)
}
