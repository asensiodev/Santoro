plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.retrofit)
}
