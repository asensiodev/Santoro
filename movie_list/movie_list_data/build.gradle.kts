import versions.daggerHilt
import versions.retrofit
import versions.room
import versions.test

plugins {
    `android-library`
    `kotlin-android`
}

apply<gradle_plugins.BaseGradlePlugin>()

android {
    namespace = "com.noirsonora.santoro_movie_list_data"
}

dependencies {
    //implementation(project(Modules.core))
    daggerHilt()
    retrofit()
    room()
    test()
}