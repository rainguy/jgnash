plugins {
    id("jgnash.java-conventions")
}

description = "jGnash Core Test Classes"

var moduleName = "jgnash.tests"

dependencies {
    testImplementation(libs.commons.text)

    testImplementation(project(":jgnash-resources"))
    testImplementation(project(":jgnash-core"))
    testImplementation(project(":jgnash-bayes"))
    testImplementation(project(":jgnash-convert"))
    testImplementation(project(":jgnash-report-core"))

    testImplementation(libs.netty.codec)
    testImplementation(libs.xstream)

    testImplementation(libs.commons.collections)
    testImplementation(libs.commons.math)
    testImplementation(libs.pdfbox)
    testImplementation(libs.pdfbox.tools)
}

tasks.register<JavaExec>("generateDataFormatFixtures") {
    group = "verification"
    description = "Regenerates the synthetic persistence fixture payloads"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("jgnash.engine.fixture.DataFormatFixtureGenerator")
    args(layout.projectDirectory.dir("src/test/resources/jgnash/engine/fixtures").asFile.absolutePath)
}

tasks.register<JavaExec>("printDataFormatFixtureSummaries") {
    group = "verification"
    description = "Opens copied fixtures and prints independently derived semantic summaries"
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("jgnash.engine.fixture.DataFormatFixtureSummaryPrinter")
    args(layout.projectDirectory.dir("src/test/resources/jgnash/engine/fixtures").asFile.absolutePath)
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
