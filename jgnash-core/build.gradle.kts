description = "jGnash Core"

var moduleName = "jgnash.core"

plugins {
    id("jgnash.java-conventions")
    `java-library`
}

dependencies {
    implementation(project(":jgnash-resources"))

    // required for HikariCP, override with modular version
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.jdk14)

    api(libs.hibernate.entitymanager)
    implementation(libs.hibernate.hikaricp)
    implementation(libs.hikari)

    implementation(libs.h2)
    implementation(libs.hsqldb)

    implementation(libs.xstream) {
        exclude(module = "xmlpull")
        exclude(module = "xpp3_min")
    }

    implementation(libs.xstream.hibernate) {
        exclude(module = "xmlpull")
        exclude(module = "xpp3_min")
    }

    implementation(libs.netty.codec)

    implementation(libs.commons.collections)
    implementation(libs.commons.csv)
    implementation(libs.commons.lang)
    implementation(libs.commons.math)
}

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = moduleName
}
