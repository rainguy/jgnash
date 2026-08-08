import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    id("com.github.ben-manes.versions")
}

val javaLanguageVersion = JavaLanguageVersion.of(21)

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(javaLanguageVersion)
    }
}

val javaToolchains = extensions.getByType<JavaToolchainService>()
val java21Launcher = javaToolchains.launcherFor {
    languageVersion.set(javaLanguageVersion)
}

val verifyJava21Toolchain by tasks.registering {
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

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        dependsOn(rootProject.tasks.named("verifyJava21Toolchain"))
        options.release.set(21)
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>().configureEach {
        dependsOn(rootProject.tasks.named("verifyJava21Toolchain"))
        (options as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            charSet = "UTF-8"
            docEncoding = "UTF-8"
        }
    }

    tasks.withType<Test>().configureEach {
        dependsOn(rootProject.tasks.named("verifyJava21Toolchain"))
        javaLauncher.set(java21Launcher)
    }
}

subprojects {
    group = "jgnash"
    version = "3.6.0"
}
