plugins {
    alias(libs.plugins.dependency.updates)
    id("jgnash.java-conventions")
}

tasks.named("check") {
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
}
