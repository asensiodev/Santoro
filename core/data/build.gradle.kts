plugins {
    alias(libs.plugins.convention.jvm.library)
    alias(libs.plugins.convention.jvm.hilt)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.library.secureStorage.api)
    implementation(libs.gson)
}
