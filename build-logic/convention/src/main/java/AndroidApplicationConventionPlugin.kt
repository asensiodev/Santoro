@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationExtension
import com.asensiodev.santoro.buildlogic.convention.configureAppBuildTypes
import com.asensiodev.santoro.buildlogic.convention.configureKotlinAndroid
import com.asensiodev.santoro.buildlogic.convention.getTargetSdk
import com.asensiodev.santoro.buildlogic.convention.getVersionCode
import com.asensiodev.santoro.buildlogic.convention.getVersionName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("com.asensiodev.santoro.buildlogic.convention.jvm-test")
                apply("com.asensiodev.santoro.buildlogic.convention.android-test")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureAppBuildTypes(this)
                defaultConfig {
                    targetSdk = getTargetSdk()
                    versionCode = getVersionCode()
                    versionName = getVersionName()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }
        }
    }
}
