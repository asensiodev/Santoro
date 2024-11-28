@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.asensiodev.buildlogic.convention.logic.configureAppBuildTypes
import com.asensiodev.buildlogic.convention.logic.configureKotlinAndroid
import com.asensiodev.buildlogic.convention.logic.getTargetSdk
import com.asensiodev.buildlogic.convention.logic.getVersionCode
import com.asensiodev.buildlogic.convention.logic.getVersionName

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
