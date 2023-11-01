import versions.daggerHilt
import versions.test

plugins {
    `android-library`
    `kotlin-android`
}

apply<gradle_plugins.BaseGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_login_data"
}

dependencies {
    //implementation(project(Modules.core))
    daggerHilt()
    test()
}