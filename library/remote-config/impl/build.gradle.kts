plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.library.remoteconfig.impl"
}

dependencies {
    implementation(projects.library.remoteConfig.api)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.remote.config)
}
