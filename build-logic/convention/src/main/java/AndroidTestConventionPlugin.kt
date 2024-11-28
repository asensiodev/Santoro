import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import com.asensiodev.buildlogic.convention.logic.libs

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                "testImplementation"(libs.findLibrary("junit").get())
                "testImplementation"(libs.findLibrary("junit.jupiter.vintage.engine").get())
                "testImplementation"(libs.findLibrary("androidx.compose.ui.test.junit4").get())
                "androidTestImplementation"(project(":core:testing"))
                "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
                "androidTestImplementation"(libs.findLibrary("androidx.compose.ui.test.junit4").get())
            }
        }
    }
}
