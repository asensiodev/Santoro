package gradle_plugins

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project

class ComposeGradlePlugin : BaseGradlePlugin() {

    override fun apply(project: Project) {
        super.apply(project)
        setComposeProjectConfiguration(project)
    }

    private fun setComposeProjectConfiguration(project: Project) {
        project.extensions.configure<LibraryExtension>(ANDROID_EXTENSION_CONFIG) {
            buildFeatures {
                compose = true
            }
            composeOptions {
                kotlinCompilerExtensionVersion = versions.Compose.composeCompilerVersion
            }
        }
    }

}