package jgnash.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

class JgnashJavaConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("java")

            group = "jgnash"
            version = "3.6.0"

            val javaVersion = JavaLanguageVersion.of(21)
            extensions.getByType(JavaPluginExtension::class.java).toolchain.languageVersion.set(javaVersion)

            val java21Launcher = extensions.getByType(JavaToolchainService::class.java).launcherFor {
                languageVersion.set(javaVersion)
            }

            val verifyJava21Toolchain = if (project == rootProject) {
                tasks.register("verifyJava21Toolchain") {
                    group = "verification"
                    description = "Verifies that a Java 21 JDK toolchain is available"

                    doLast {
                        try {
                            java21Launcher.get()
                        } catch (exception: Exception) {
                            throw GradleException(
                                "A Java 21 JDK toolchain is required. Install JDK 21 or configure " +
                                    "org.gradle.java.installations.paths before running the build.",
                                exception
                            )
                        }
                    }
                }
            } else {
                rootProject.tasks.named("verifyJava21Toolchain")
            }

            tasks.withType(JavaCompile::class.java).configureEach {
                dependsOn(verifyJava21Toolchain)
                options.release.set(21)
                options.encoding = "UTF-8"
            }

            tasks.withType(Javadoc::class.java).configureEach {
                dependsOn(verifyJava21Toolchain)
                (options as StandardJavadocDocletOptions).apply {
                    encoding = "UTF-8"
                    charSet = "UTF-8"
                    docEncoding = "UTF-8"
                }
            }

            tasks.withType(Test::class.java).configureEach {
                dependsOn(verifyJava21Toolchain)
                javaLauncher.set(java21Launcher)
                useJUnitPlatform()
                testLogging {
                    events("FAILED", "SKIPPED")
                    showStandardStreams = false
                }
            }

            tasks.withType(AbstractArchiveTask::class.java).configureEach {
                isPreserveFileTimestamps = false
                isReproducibleFileOrder = true
            }

            tasks.withType(Jar::class.java).configureEach {
                manifest.attributes(
                    mapOf(
                        "Implementation-Title" to provider { project.description ?: project.name },
                        "Implementation-Version" to provider { project.version.toString() },
                        "Implementation-Vendor" to "jGnash"
                    )
                )
            }

            dependencies.add(
                "testImplementation",
                propertyDependency("junitVersion", "org.junit.jupiter:junit-jupiter-api")
            )
            dependencies.add(
                "testImplementation",
                propertyDependency("junitVersion", "org.junit.jupiter:junit-jupiter-params")
            )
            dependencies.add(
                "testRuntimeOnly",
                propertyDependency("junitVersion", "org.junit.jupiter:junit-jupiter-engine")
            )
            dependencies.add(
                "testRuntimeOnly",
                propertyDependency("junitPlatformVersion", "org.junit.platform:junit-platform-launcher")
            )
            dependencies.add(
                "testImplementation",
                propertyDependency("junitExtensionsVersion", "io.github.glytching:junit-extensions")
            )
            dependencies.add("testImplementation", propertyDependency("awaitilityVersion", "org.awaitility:awaitility"))
        }
    }

    private fun Project.propertyDependency(propertyName: String, coordinates: String): String =
        "$coordinates:${providers.gradleProperty(propertyName).get()}"
}
