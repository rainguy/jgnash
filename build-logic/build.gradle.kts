plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

apply(from = "../gradle/dependency-policy.gradle.kts")

// This build produces JVM plugins only. Kotlin registers a native-toolchain
// downloader even here; keep it out of dependency maintenance on every host.
configurations.named("kotlinNativeBundleConfiguration") {
    isCanBeResolved = false
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
        rootDir.parentFile.resolve("gradle/libs.versions.toml"),
        rootDir.parentFile.resolve("gradle/dependency-policy.gradle.kts")
    ).withPropertyName("repositoryPolicyFiles").withPathSensitivity(PathSensitivity.RELATIVE)
}
