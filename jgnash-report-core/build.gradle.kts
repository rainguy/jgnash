description = "jGnash Report Core"

val moduleName = "jgnash.report"

plugins {
    id("jgnash.java-conventions")
    `java-library`
}

dependencies {
    implementation(project(":jgnash-resources"))
    implementation(project(":jgnash-core"))

    implementation(libs.apache.poi) {
        exclude(module = "stax-api")
        exclude(module = "xml-apis")
    }

    implementation(libs.pdfbox)
    implementation(libs.pdfbox.tools)
    implementation(libs.commons.lang)
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
