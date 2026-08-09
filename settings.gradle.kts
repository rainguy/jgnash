pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "jgnash"

include ("jgnash-bayes", "jgnash-resources", "jgnash-core", "jgnash-convert",
        "jgnash-plugin", "jgnash-fx", "jgnash-report-core", "jgnash-fx-test-plugin", "mt940", "jgnash-tests")
