description = "jGnash Convert"

val moduleName = "jgnash.convert"
val commonsCsvVersion: String by project
val nashornVersion: String by project

plugins {
    id("jgnash.java-conventions")
    `java-library`
}

dependencies {
    implementation(project(":jgnash-resources"))
    implementation(project(":jgnash-core"))
    implementation(project(":jgnash-bayes"))

    implementation("org.apache.commons:commons-csv:$commonsCsvVersion")
    implementation("org.openjdk.nashorn:nashorn-core:$nashornVersion")
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
