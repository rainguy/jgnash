plugins {
    alias(libs.plugins.dependency.updates)
    id("jgnash.java-conventions")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

tasks.named("check") {
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
}
