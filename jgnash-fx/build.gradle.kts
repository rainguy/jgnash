import edu.sc.seis.macAppBundle.MacAppBundlePluginExtension
import org.gradle.api.attributes.Attribute

description = "jGnash"

plugins {
    id("jgnash.java-conventions")
    application // creates a task to run the full application
    `java-library`
    alias(libs.plugins.javafx)
    alias(libs.plugins.mac.app.bundle) apply false
}

val legacyMacPackagingRequested = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':') in setOf("createApp", "macDist", "macDistZip")
}

val jGnashVersion : String = version.toString()
val macAppDirectory = layout.buildDirectory.dir("macApp")

// Resolve every supported native variant during dependency maintenance and CI,
// independently of the host selected by the JavaFX plugin.
val javafxVerificationConfigurations = listOf("linux", "win", "mac", "mac-aarch64").associateWith { platform ->
    configurations.create("javafxVerification${platform.replace("-", "_")}") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false // All seven modules are requested explicitly below.
    }
}

application {
    mainClass.set("jgnash.app.jGnash")
}

dependencies {
    for (library in listOf(libs.javafx.base, libs.javafx.fxml, libs.javafx.controls,
            libs.javafx.graphics, libs.javafx.media, libs.javafx.swing, libs.javafx.web)) {
        for ((platform, configuration) in javafxVerificationConfigurations) {
            add(configuration.name, variantOf(library) { classifier(platform) })
        }
    }

    implementation(project(":jgnash-resources"))
    implementation(project(":jgnash-core"))
    implementation(project(":jgnash-convert"))
    implementation(project(":jgnash-report-core"))
    implementation(project(":jgnash-plugin"))

    implementation(libs.picocli)

    implementation(libs.commons.lang)
    implementation(libs.commons.math)

    // Hack to include all javafx platforms in the classpath
    // The platform specific libraries are excluded when the distribution is assembled
    implementation(libs.bundles.javafx)

    runtimeOnly(variantOf(libs.javafx.base) { classifier("linux") })
    runtimeOnly(variantOf(libs.javafx.fxml) { classifier("linux") })
    runtimeOnly(variantOf(libs.javafx.controls) { classifier("linux") })
    runtimeOnly(variantOf(libs.javafx.graphics) { classifier("linux") })
    runtimeOnly(variantOf(libs.javafx.media) { classifier("linux") })
    runtimeOnly(variantOf(libs.javafx.swing) { classifier("linux") })
    runtimeOnly(variantOf(libs.javafx.web) { classifier("linux") })

    runtimeOnly(variantOf(libs.javafx.base) { classifier("win") })
    runtimeOnly(variantOf(libs.javafx.fxml) { classifier("win") })
    runtimeOnly(variantOf(libs.javafx.controls) { classifier("win") })
    runtimeOnly(variantOf(libs.javafx.graphics) { classifier("win") })
    runtimeOnly(variantOf(libs.javafx.media) { classifier("win") })
    runtimeOnly(variantOf(libs.javafx.swing) { classifier("win") })
    runtimeOnly(variantOf(libs.javafx.web) { classifier("win") })

    runtimeOnly(variantOf(libs.javafx.base) { classifier("mac") })
    runtimeOnly(variantOf(libs.javafx.fxml) { classifier("mac") })
    runtimeOnly(variantOf(libs.javafx.controls) { classifier("mac") })
    runtimeOnly(variantOf(libs.javafx.graphics) { classifier("mac") })
    runtimeOnly(variantOf(libs.javafx.media) { classifier("mac") })
    runtimeOnly(variantOf(libs.javafx.swing) { classifier("mac") })
    runtimeOnly(variantOf(libs.javafx.web) { classifier("mac") })
    // end hack

    // required of Unit testing JavaFX
    testImplementation(libs.testfx.junit5)
    testImplementation(libs.testfx.monocle)
}

javafx {
    version = libs.versions.javafx.get()
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing",
            "javafx.graphics", "javafx.media")
}

tasks.processResources {
    val javaFxVersion = libs.versions.javafx.get()
    inputs.property("javaFxVersion", javaFxVersion)
    filesMatching("jgnash/bootloader/bootloader.properties") {
        expand("javaFxVersion" to javaFxVersion)
    }
}

tasks.test {
    systemProperty("jgnash.test.javafx.version", libs.versions.javafx.get())
}

tasks.startScripts {
    applicationName = "bootloader"
}

tasks.distZip {
    destinationDirectory.set(file(rootDir))

    // this "should" work according to Gradle Doc but mangles the content of the zip file
    //archiveFileName.set("jgnash-${archiveVersion.get()}-bin.${archiveExtension.get()}")

    // build the mt940 plugin prior to creating the zip file without creating a circular loop
    dependsOn(":mt940:jar")

    // add the mt940 plugin
    into("jGnash-${archiveVersion.get()}") {
        from("../mt940/build/libs")
        include("*")
        into("jGnash-${archiveVersion.get()}/plugins")
    }

    into("jGnash-${archiveVersion.get()}") {
        from(".")
        include("scripts/*")
    }

    doLast {
        // delete the old renamed build
        file("${destinationDirectory.get()}/jgnash-${archiveVersion.get()}-bin.${archiveExtension.get()}").delete()

        file("${destinationDirectory.get()}/${archiveFileName.get()}").renameTo(file("${destinationDirectory.get()}/jgnash-${archiveVersion.get()}-bin.${archiveExtension.get()}"))
    }
}

distributions {
    main {
        distributionBaseName.set("jGnash")

        contents {
            from("../jgnash-manual/src/Manual.pdf")
            from("../changelog.adoc")
            from("../rust-launcher/target/release/jGnash.exe")
            from("../README.html")
            from("../README.adoc")
            from("../jGnash")
            exclude("**/*-linux*")  // excludes linux specific JavaFx modules from cross platform zip
            exclude("**/*-win*")    // excludes windows specific JavaFx modules from cross platform zip
            exclude("**/*-mac*")    // excludes mac specific JavaFx modules from cross platform zip
        }
    }
}

// Keep this compatibility graph visible to locking even when packaging is not
// requested. macAppBundle reads the removed Java plugin "runtime" configuration.
configurations.create("runtime") {
    val source = configurations.runtimeClasspath.get()
    extendsFrom(source)
    isCanBeConsumed = false
    isCanBeResolved = true
    // Configuration inheritance covers dependencies, but not variant attributes.
    fun <T : Any> copyAttribute(key: Attribute<T>) {
        source.attributes.getAttribute(key)?.let { attributes.attribute(key, it) }
    }
    source.attributes.keySet().forEach { copyAttribute(it) }
}

if (legacyMacPackagingRequested) {
    apply(plugin = "edu.sc.seis.macAppBundle")

    configure<MacAppBundlePluginExtension> {
        appStyle = "universalJavaApplicationStub"
        appName = "jGnash-$jGnashVersion"
        mainClassName = "jgnash.app.jGnash"
        icon = "../deployfx/gnome-money.icns"
        javaProperties["apple.laf.useScreenMenuBar"] = "true"
    }
}

/**
 * Returns a proper Class-Path entry for the manifest file
 * @return classpath relative to the installation root point to the jars in the lib directory
 */
fun generateManifestClassPath(): String {
    val path = StringBuilder()

    configurations.runtimeClasspath.get().files.forEach {
        path.append("lib/")
        path.append(it.name)
        path.append(" ")
    }

    return path.toString()
}

tasks.jar {
    // Keep jar clean:
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.MF")

    manifest {
        attributes(mapOf("Main-Class" to "jGnash", "Class-Path" to generateManifestClassPath()))
    }
}

tasks.register("macDist") {
    description = "Creates a Mac compatible .app distribution directory"
    dependsOn("createApp", "distZip")

    doLast {
        configurations.runtimeClasspath.get().files.forEach {
            // copy all files in the class path, but ignore windows and linux specific files
            if (!it.name.contains("linux.jar") && !it.name.contains("win.jar")) {
                val javaDirectory = macAppDirectory.get()
                    .dir("jGnash-$jGnashVersion.app/Contents/Java")
                    .asFile
                it.copyTo(javaDirectory.resolve(it.name), true)
            }
        }
    }
}

tasks.register<Zip>("macDistZip") {
    description = "Creates a Mac compatible archive of the .app distribution directory"

    dependsOn("clean", "macDist")
    archiveFileName.set("jGnash-$jGnashVersion.App.zip")
    destinationDirectory.set(rootDir)

    from(macAppDirectory)

    from("../jgnash-manual/src/Manual.pdf") {
        into("jGnash-$jGnashVersion.app/Contents/SharedSupport")
    }

    from("../changelog.adoc") {
        into("jGnash-$jGnashVersion.app/Contents/SharedSupport")
    }

    from("../README.adoc") {
        into("jGnash-$jGnashVersion.app/Contents/SharedSupport")
    }

    from("../README.html") {
        into("jGnash-$jGnashVersion.app/Contents/SharedSupport")
    }
}
