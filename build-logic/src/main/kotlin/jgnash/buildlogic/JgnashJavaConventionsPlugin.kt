package jgnash.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
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
            version = "3.7.0"

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

            val libraries = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            dependencies.add(
                "testImplementation",
                libraries.findLibrary("junit-jupiter-api").get()
            )
            dependencies.add(
                "testImplementation",
                libraries.findLibrary("junit-jupiter-params").get()
            )
            dependencies.add(
                "testRuntimeOnly",
                libraries.findLibrary("junit-jupiter-engine").get()
            )
            dependencies.add(
                "testRuntimeOnly",
                libraries.findLibrary("junit-platform-launcher").get()
            )
            dependencies.add(
                "testImplementation",
                libraries.findLibrary("junit-extensions").get()
            )
            dependencies.add("testImplementation", libraries.findLibrary("awaitility").get())
        }
    }
}
