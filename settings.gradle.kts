pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "Santoro"
include(":app")
include(":core:design-system")
include(":core:string-resources")
include(":core:testing")
include(":core:domain")
include(":core:network")
include(":core:build-config")
include(":core:data")
include(":feature:search-movies:api")
include(":feature:search-movies:impl")
include(":feature:watched-movies:api")
include(":feature:watched-movies:impl")
include(":feature:watchlist:api")
include(":feature:watchlist:impl")
include(":feature:movie-detail:api")
include(":feature:movie-detail:impl")
include(":library:remote-config:api")
include(":library:remote-config:impl")
