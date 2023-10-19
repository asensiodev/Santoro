import versions.androidX
import versions.compose
import versions.daggerHilt
import versions.test

plugins {
    `android-library`
    `kotlin-android`
}

apply<gradle_plugins.ComposeGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_login_presentation"
}

dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))
    androidX()
    compose()
    daggerHilt()
    test()
}