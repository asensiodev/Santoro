import versions.androidX
import versions.daggerHilt
import versions.test

plugins {
    `android-library`
    `kotlin-android`
}

apply<gradle_plugins.BaseGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_onboarding_domain"
}

dependencies {
    implementation(project(Modules.core))
    androidX()
    daggerHilt()
    test()
}