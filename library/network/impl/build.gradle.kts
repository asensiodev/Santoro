plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.asensiodev.library.network.impl"
}

dependencies {
    implementation(projects.library.network.api)
}
