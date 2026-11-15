plugins {
    alias(libs.plugins.dependency.updates)
    id("jgnash.java-conventions")
}

allprojects {
    apply(from = rootProject.file("gradle/dependency-policy.gradle.kts"))
}

tasks.named("check") {
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
}
