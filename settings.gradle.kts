pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "jgnash"

include ("jgnash-bayes", "jgnash-resources", "jgnash-core", "jgnash-convert",
        "jgnash-plugin", "jgnash-fx", "jgnash-report-core", "jgnash-fx-test-plugin", "mt940", "jgnash-tests")
