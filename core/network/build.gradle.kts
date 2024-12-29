plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.core.network"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.buildConfig)
    implementation(projects.library.remoteConfig.api)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.gson)
    implementation(libs.okhttp.logging.interceptor)
}
