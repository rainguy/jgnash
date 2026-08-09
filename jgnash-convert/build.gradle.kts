description = "jGnash Convert"

val moduleName = "jgnash.convert"

plugins {
    id("jgnash.java-conventions")
    `java-library`
}

dependencies {
    implementation(project(":jgnash-resources"))
    implementation(project(":jgnash-core"))
    implementation(project(":jgnash-bayes"))

    implementation(libs.commons.csv)
    implementation(libs.nashorn)
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
