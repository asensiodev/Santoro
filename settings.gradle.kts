pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Santoro"
include(":app")
include(":core")
include(":movie_list")
include(":movie_list:movie_list_data")
include(":movie_list:movie_list_domain")
include(":movie_list:movie_list_presentation")
include(":core_ui")
