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
        maven {
            url = uri("https://archiva.breautek.com/repository/breautek")
        }
    }
}

rootProject.name = "FuseFilesystem"
include(":testapp")
include(":filesystem")
