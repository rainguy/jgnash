package jgnash.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

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
            junit = "5.14.4"
            awaitility = "4.0.3"

            [libraries]
            junit-bom = { module = "org.junit:junit-bom", version.ref = "junit" }
            junit-jupiter-api = { module = "org.junit.jupiter:junit-jupiter-api" }
            junit-jupiter-params = { module = "org.junit.jupiter:junit-jupiter-params" }
            junit-jupiter-engine = { module = "org.junit.jupiter:junit-jupiter-engine" }
            junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }
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

            repositories {
                mavenCentral()
            }

            val configuredGroup = project.group
            val configuredVersion = project.version
            val java = extensions.getByType<JavaPluginExtension>()
            val compileJava = tasks.named<JavaCompile>("compileJava")
            val test = tasks.named<Test>("test")
            val jar = tasks.named<Jar>("jar")
            val testRuntimeClasspath = configurations.getByName("testRuntimeClasspath")

            tasks.register("assertConventions") {
                dependsOn(jar, test)

                doLast {
                    check(configuredGroup == "jgnash")
                    check(configuredVersion.toString() == "3.7.2")
                    check(java.toolchain.languageVersion.get().asInt() == 21)

                    check(compileJava.get().options.release.get() == 21)
                    check(compileJava.get().options.encoding == "UTF-8")

                    check(test.get().options.javaClass.simpleName.contains("JUnitPlatform"))
                    check(!test.get().testLogging.showStandardStreams)

                    check(!jar.get().isPreserveFileTimestamps)
                    check(jar.get().isReproducibleFileOrder)
                    JarFile(jar.get().archiveFile.get().asFile).use {
                        check(it.manifest.mainAttributes.getValue("Implementation-Title") == "Fixture module")
                        check(it.manifest.mainAttributes.getValue("Implementation-Version") == "3.7.2")
                    }

                    val testDependencies = testRuntimeClasspath.incoming.resolutionResult.allComponents
                        .mapNotNull { it.moduleVersion }
                        .map { "${'$'}{it.group}:${'$'}{it.name}:${'$'}{it.version}" }.toSet()
                    check("org.junit:junit-bom:5.14.4" in testDependencies)
                    check("org.junit.jupiter:junit-jupiter-api:5.14.4" in testDependencies)
                    check("org.junit.jupiter:junit-jupiter-params:5.14.4" in testDependencies)
                    check("org.junit.jupiter:junit-jupiter-engine:5.14.4" in testDependencies)
                    check("org.junit.platform:junit-platform-launcher:1.14.4" in testDependencies)
                    check("org.junit.platform:junit-platform-engine:1.14.4" in testDependencies)
                    check("org.junit.platform:junit-platform-commons:1.14.4" in testDependencies)
                    check(testDependencies.none { it.startsWith("io.github.glytching:") })
                    check("org.awaitility:awaitility:4.0.3" in testDependencies)
                }
            }
            """.trimIndent()
        )

        val testSources = Files.createDirectories(projectDirectory.resolve("src/test/java"))
        Files.writeString(
            testSources.resolve("FixtureTest.java"),
            """
            import java.nio.file.Files;
            import java.nio.file.Path;
            import org.junit.jupiter.api.io.TempDir;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.ValueSource;
            import static org.junit.jupiter.api.Assertions.assertEquals;
            import static org.junit.jupiter.api.Assertions.assertFalse;

            class FixtureTest {
                @TempDir
                Path directory;

                @ParameterizedTest
                @ValueSource(strings = {"first", "second"})
                void writesInTemporaryDirectory(String value) throws Exception {
                    Path file = directory.resolve("value.txt");
                    assertFalse(Files.exists(file));
                    Files.writeString(file, value);
                    assertEquals(value, Files.readString(file));
                }
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDirectory.toFile())
            .withArguments("assertConventions", "--stacktrace", "--warning-mode=fail")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":test")?.outcome)
        val report = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            projectDirectory.resolve("build/test-results/test/TEST-FixtureTest.xml").toFile()
        ).documentElement
        assertEquals("2", report.getAttribute("tests"))
        assertEquals("0", report.getAttribute("failures"))
        assertEquals("0", report.getAttribute("errors"))
    }
}
