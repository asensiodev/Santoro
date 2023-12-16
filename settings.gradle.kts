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
include(":core_ui")
include(":onboarding")
include(":onboarding:onboarding_domain")
include(":onboarding:onboarding_presentation")
include(":login")
include(":login:login_domain")
include(":login:login_data")
include(":login:login_presentation")
include(":movie_list")
include(":movie_list:movie_list_domain")
include(":movie_list:movie_list_data")
include(":movie_list:movie_list_presentation")
