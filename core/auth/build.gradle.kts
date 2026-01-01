plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.auth"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.core)
}
