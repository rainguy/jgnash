package jgnash.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class JgnashJavaConventionsPluginFunctionalTest {
    @TempDir
    lateinit var projectDirectory: Path

    @Test
    fun `configures a Java project consistently`() {
        Files.writeString(projectDirectory.resolve("settings.gradle.kts"), "rootProject.name = \"fixture\"\n")
        Files.createDirectories(projectDirectory.resolve("gradle"))
        Files.writeString(
            projectDirectory.resolve("gradle/libs.versions.toml"),
            """
            [versions]
            junit = "5.7.1"
            junit-platform = "1.7.1"
            junit-extensions = "2.4.0"
            awaitility = "4.0.3"

            [libraries]
            junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "junit" }
            junit-jupiter-params = { module = "org.junit.jupiter:junit-jupiter-params", version.ref = "junit" }
            junit-jupiter-engine = { module = "org.junit.jupiter:junit-jupiter-engine", version.ref = "junit" }
            junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }
            junit-extensions = { module = "io.github.glytching:junit-extensions", version.ref = "junit-extensions" }
            awaitility = { module = "org.awaitility:awaitility", version.ref = "awaitility" }
            """.trimIndent()
        )
        Files.writeString(
            projectDirectory.resolve("build.gradle.kts"),
            """
            import org.gradle.api.plugins.JavaPluginExtension
            import org.gradle.api.tasks.bundling.Jar
            import org.gradle.api.tasks.compile.JavaCompile
            import org.gradle.api.tasks.testing.Test
            import java.util.jar.JarFile

            plugins {
                id("jgnash.java-conventions")
            }

            description = "Fixture module"

            val configuredGroup = project.group
            val configuredVersion = project.version
            val java = extensions.getByType<JavaPluginExtension>()
            val compileJava = tasks.named<JavaCompile>("compileJava")
            val test = tasks.named<Test>("test")
            val jar = tasks.named<Jar>("jar")
            val testRuntimeClasspath = configurations.getByName("testRuntimeClasspath")

            tasks.register("assertConventions") {
                dependsOn(jar)

                doLast {
                    check(configuredGroup == "jgnash")
                    check(configuredVersion.toString() == "3.7.0")
                    check(java.toolchain.languageVersion.get().asInt() == 21)

                    check(compileJava.get().options.release.get() == 21)
                    check(compileJava.get().options.encoding == "UTF-8")

                    check(test.get().options.javaClass.simpleName.contains("JUnitPlatform"))
                    check(!test.get().testLogging.showStandardStreams)

                    check(!jar.get().isPreserveFileTimestamps)
                    check(jar.get().isReproducibleFileOrder)
                    JarFile(jar.get().archiveFile.get().asFile).use {
                        check(it.manifest.mainAttributes.getValue("Implementation-Title") == "Fixture module")
                        check(it.manifest.mainAttributes.getValue("Implementation-Version") == "3.7.0")
                    }

                    val testDependencies = testRuntimeClasspath.allDependencies
                        .map { "${'$'}{it.group}:${'$'}{it.name}:${'$'}{it.version}" }.toSet()
                    check("org.junit.jupiter:junit-jupiter-api:5.7.1" in testDependencies)
                    check("org.awaitility:awaitility:4.0.3" in testDependencies)
                }
            }
            """.trimIndent()
        )

        GradleRunner.create()
            .withProjectDir(projectDirectory.toFile())
            .withArguments("assertConventions", "--stacktrace", "--warning-mode=fail")
            .withPluginClasspath()
            .build()
    }
}
