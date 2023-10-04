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
include(":news_feed")
include(":news_feed:data")
include(":news_feed:domain")
include(":news_feed:presentation")
