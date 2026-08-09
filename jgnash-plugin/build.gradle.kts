description = "jGnash Plugin"

val moduleName = "jgnash.plugin"

plugins {
    id("jgnash.java-conventions")
    alias(libs.plugins.javafx)
}

dependencies {
    implementation(project(":jgnash-resources"))
}

javafx {
    version = libs.versions.javafx.get()
    modules("javafx.controls", "javafx.fxml")
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
