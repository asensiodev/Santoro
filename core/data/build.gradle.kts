plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.gson)
}
