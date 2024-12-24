plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.library.network.impl"
}

dependencies {
    implementation(projects.library.network.api)
    implementation(projects.core.domain)
    implementation(projects.library.remoteConfig.api)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.gson)
}
