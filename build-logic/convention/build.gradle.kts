import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.asensiodev.santoro.buildlogic.convention"

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("android-application") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("android-application-compose") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-application-compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("android-library") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("android-library-compose") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-library-compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("android-feature") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvm-library") {
            id = "com.asensiodev.santoro.buildlogic.convention.jvm-library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("android-hilt") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("jvm-hilt") {
            id = "com.asensiodev.santoro.buildlogic.convention.jvm-hilt"
            implementationClass = "JvmHiltConventionPlugin"
        }
        register("android-test") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("jvm-test") {
            id = "com.asensiodev.santoro.buildlogic.convention.jvm-test"
            implementationClass = "JvmTestConventionPlugin"
        }
        register("paparazzi") {
            id = "com.asensiodev.santoro.buildlogic.convention.paparazzi"
            implementationClass = "PaparazziConventionPlugin"
        }
        register("android-room") {
            id = "com.asensiodev.santoro.buildlogic.convention.android-room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
    }
}
