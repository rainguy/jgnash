package jgnash.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class DependencyIntegrityFunctionalTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `requires lock state and rejects a version change until locks are explicitly refreshed`() {
        fixture()
        val missing = runner().buildAndFail()
        assertTrue(missing.output.contains("does not have lock state"), missing.output)

        runner("--write-locks").build()
        assertEquals(TaskOutcome.SUCCESS, runner().build().task(":resolveAllDependencies")?.outcome)
        val changed = runner("-PprobeVersion=2.0").buildAndFail()
        assertTrue(changed.output.contains("Dependency version enforced by Dependency Locking"), changed.output)

        runner("-PprobeVersion=2.0", "--write-locks").build()
        val locks = Files.readString(directory.resolve("gradle.lockfile"))
        assertTrue(locks.contains("sample:library:2.0=probe"), locks)
        assertEquals(TaskOutcome.SUCCESS, runner("-PprobeVersion=2.0").build().task(":resolveAllDependencies")?.outcome)
    }

    @ParameterizedTest
    @ValueSource(strings = ["jar", "pom"])
    fun `rejects altered artifacts and metadata with unchanged coordinates`(extension: String) {
        fixture()
        runner("--write-locks", "--write-verification-metadata", "sha256").build()
        assertEquals(TaskOutcome.SUCCESS, runner().build().task(":resolveAllDependencies")?.outcome)

        val artifact = directory.resolve("repository/sample/library/1.0/library-1.0.$extension")
        if (extension == "jar") {
            writeJar(artifact, "tampered artifact with the same coordinates")
        } else {
            Files.writeString(artifact, "\n<!-- altered metadata -->\n", APPEND)
        }
        val result = runner("--refresh-dependencies").buildAndFail()
        assertTrue(result.output.contains("Dependency verification failed"), result.output)
        assertTrue(result.output.contains("library-1.0.$extension"), result.output)
    }

    private fun fixture() {
        val policy = Path.of(System.getProperty("jgnash.repositoryRoot"))
            .resolve("gradle/dependency-policy.gradle.kts")
        Files.copy(policy, directory.resolve("dependency-policy.gradle.kts"))
        Files.writeString(directory.resolve("settings.gradle.kts"), """
            rootProject.name = "integrity-fixture"
            dependencyResolutionManagement {
                repositories { maven { url = uri("repository") } }
            }
        """.trimIndent())
        Files.writeString(directory.resolve("build.gradle.kts"), """
            apply(from = "dependency-policy.gradle.kts")
            configurations.create("probe")
            dependencies.add("probe", "sample:library:" + providers.gradleProperty("probeVersion").getOrElse("1.0"))
        """.trimIndent())
        for (version in listOf("1.0", "2.0")) {
            val location = Files.createDirectories(directory.resolve("repository/sample/library/$version"))
            Files.writeString(location.resolve("library-$version.pom"), """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>sample</groupId>
                  <artifactId>library</artifactId>
                  <version>$version</version>
                </project>
            """.trimIndent())
            writeJar(location.resolve("library-$version.jar"), "original $version")
        }
    }

    private fun writeJar(path: Path, contents: String) {
        JarOutputStream(Files.newOutputStream(path)).use {
            it.putNextEntry(JarEntry("payload.txt"))
            it.write(contents.toByteArray(Charsets.UTF_8))
            it.closeEntry()
        }
    }

    private fun runner(vararg arguments: String): GradleRunner = GradleRunner.create()
        .withProjectDir(directory.toFile())
        .withArguments("resolveAllDependencies", *arguments, "--stacktrace", "--warning-mode=fail")
}
