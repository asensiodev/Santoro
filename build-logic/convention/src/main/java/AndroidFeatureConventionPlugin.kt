import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.asensiodev.buildlogic.convention.logic.libs

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.asensiodev.santoro.buildlogic.convention.android-library-compose")
                apply("scom.asensiodev.santoro.buildlogic.convention.android-hilt")
                apply("com.asensiodev.santoro.buildlogic.convention.android-test")
            }

            dependencies {
                add("implementation", project(":core:design-system"))
                add("implementation", project(":core:string-resources"))

                "ksp"(libs.findLibrary("hilt-compiler").get())
                "implementation"(libs.findLibrary("hilt-navigation-compose").get())
            }
        }
    }
}
