package jgnash.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarOutputStream

class RepositoryPolicyFunctionalTest {
    @TempDir
    lateinit var directory: Path

    @ParameterizedTest
    @ValueSource(strings = [".", "build-logic"])
    fun `settings allow only Central for dependencies and the plugin portal for plugins`(build: String) {
        val project = fixture(build)
        Files.writeString(
            project.resolve("settings.gradle.kts"),
            """

            check(dependencyResolutionManagement.repositoriesMode.get() == RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            check(dependencyResolutionManagement.repositories.map {
                (it as org.gradle.api.artifacts.repositories.MavenArtifactRepository).url.toString()
            } == listOf("https://repo.maven.apache.org/maven2/"))
            check(pluginManagement.repositories.map {
                (it as org.gradle.api.artifacts.repositories.MavenArtifactRepository).url.toString()
            } == listOf("https://plugins.gradle.org/m2"))
            """.trimIndent(),
            java.nio.file.StandardOpenOption.APPEND
        )
        assertEquals(TaskOutcome.SUCCESS, runner(project, "help").build().task(":help")?.outcome)
    }

    @ParameterizedTest
    @ValueSource(strings = [".", "build-logic"])
    fun `rejects repositories added by a module`(build: String) {
        val project = fixture(build)
        Files.writeString(project.resolve("settings.gradle.kts"), "\ninclude(\"rogue\")\n",
            java.nio.file.StandardOpenOption.APPEND)
        val module = Files.createDirectories(project.resolve("rogue"))
        Files.writeString(module.resolve("build.gradle.kts"), "repositories { mavenLocal() }\n")

        val result = runner(project, "help").buildAndFail()
        assertTrue(result.output.contains("repository 'MavenLocal' was added by build file"), result.output)
    }

    @ParameterizedTest
    @ValueSource(strings = [".", "build-logic"])
    fun `does not resolve an artifact present only in Maven Local`(build: String) {
        val project = fixture(build)
        val localRepository = directory.resolve("maven-local")
        val artifact = Files.createDirectories(localRepository.resolve("invalid/jgnash/repository-probe/local-only/1.0"))
        Files.writeString(artifact.resolve("local-only-1.0.pom"), """
            <project>
              <modelVersion>4.0.0</modelVersion>
              <groupId>invalid.jgnash.repository-probe</groupId>
              <artifactId>local-only</artifactId>
              <version>1.0</version>
            </project>
        """.trimIndent())
        JarOutputStream(Files.newOutputStream(artifact.resolve("local-only-1.0.jar"))).use { }
        Files.writeString(project.resolve("build.gradle.kts"), """
            val probe = configurations.create("probe")
            dependencies.add("probe", "invalid.jgnash.repository-probe:local-only:1.0")
            tasks.register("resolveProbe") {
                doLast { check(probe.singleFile.isFile) }
            }
        """.trimIndent())

        val result = runner(project, "resolveProbe", "--offline", "-Dmaven.repo.local=$localRepository").buildAndFail()
        assertTrue(result.output.contains("No cached version of invalid.jgnash.repository-probe:local-only:1.0"), result.output)
    }

    private fun fixture(build: String): Path {
        val source = Path.of(System.getProperty("jgnash.repositoryRoot"))
        val root = Files.createDirectories(directory.resolve("fixture"))
        for (relative in listOf("settings.gradle.kts", "build-logic/settings.gradle.kts", "gradle/libs.versions.toml")) {
            val target = root.resolve(relative)
            Files.createDirectories(target.parent)
            Files.copy(source.resolve(relative), target)
        }
        // Retain the production settings, including the composite build and module list,
        // while isolating repository policy from application plugins and dependencies.
        val includes = Files.readString(source.resolve("settings.gradle.kts")).substringAfter("include (")
        Regex("\"([^\"]+)\"").findAll(includes).forEach {
            Files.createDirectories(root.resolve(it.groupValues[1]))
        }
        return root.resolve(build)
    }

    private fun runner(project: Path, vararg arguments: String): GradleRunner = GradleRunner.create()
        .withProjectDir(project.toFile())
        .withArguments(*arguments, "--stacktrace", "--warning-mode=fail")
}
