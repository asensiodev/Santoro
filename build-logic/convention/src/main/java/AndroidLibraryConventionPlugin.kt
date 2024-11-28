@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import com.asensiodev.buildlogic.convention.logic.configureKotlinAndroid
import com.asensiodev.buildlogic.convention.logic.configureLibraryBuildTypes
import com.asensiodev.buildlogic.convention.logic.getTargetSdk
import com.asensiodev.buildlogic.convention.logic.libs

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("com.asensiodev.santoro.buildlogic.convention.jvm-test")
                apply("com.asensiodev.santoro.buildlogic.convention.android-test")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureLibraryBuildTypes(this)
                testOptions {
                    targetSdk = getTargetSdk()
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
                lint {
                    targetSdk = getTargetSdk()
                }
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            }
        }
    }
}
