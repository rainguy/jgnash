plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("javaConventions") {
            id = "jgnash.java-conventions"
            implementationClass = "jgnash.buildlogic.JgnashJavaConventionsPlugin"
        }
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("jgnash.repositoryRoot", rootDir.parentFile.absolutePath)
    inputs.files(
        rootDir.parentFile.resolve("settings.gradle.kts"),
        rootDir.resolve("settings.gradle.kts"),
        rootDir.parentFile.resolve("gradle/libs.versions.toml")
    ).withPropertyName("repositoryPolicyFiles").withPathSensitivity(PathSensitivity.RELATIVE)
}
