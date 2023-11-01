import versions.compose
import versions.daggerHilt
import versions.test

plugins {
    `android-library`
    `kotlin-android`
}

apply<gradle_plugins.ComposeGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_core_ui"
}

dependencies {
    implementation(project(Modules.core))
    compose()
    daggerHilt()
    test()
}