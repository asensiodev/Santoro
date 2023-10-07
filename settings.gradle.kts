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
include(":movie_list:data")
include(":movie_list:domain")
include(":movie_list:presentation")
