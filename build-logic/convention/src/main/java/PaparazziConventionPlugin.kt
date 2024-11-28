import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.asensiodev.buildlogic.convention.logic.libs

class PaparazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.cash.paparazzi")
            dependencies {
                "testImplementation"(libs.findLibrary("paparazzi").get())
            }
        }
    }
}
