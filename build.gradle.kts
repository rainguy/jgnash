plugins {
    id("com.github.ben-manes.versions")
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
