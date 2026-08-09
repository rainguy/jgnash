plugins {
    id("jgnash.java-conventions")
}

description = "jGnash Bayes"

val moduleName = "jgnash.bayes"

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
