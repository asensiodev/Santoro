import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.asensiodev.buildlogic.convention.logic.configureAndroidCompose

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.asensiodev.santoro.buildlogic.convention.android-application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            extensions.configure<ApplicationExtension> {
                configureAndroidCompose(this)
            }
        }
    }
}
