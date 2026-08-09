description = "jGnash Test Plugin"

plugins {
    id("jgnash.java-conventions")
    alias(libs.plugins.javafx)
}

dependencies {
  implementation(project(":jgnash-core"))
  implementation(project(":jgnash-plugin"))
  implementation(project(":jgnash-fx"))
}

javafx {
  version = libs.versions.javafx.get()
  modules("javafx.controls")
}

tasks.jar {
  // Keep jar clean:
  exclude ("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.MF")

  // required by the plugin interface
  manifest {
    attributes(mapOf("Plugin-Activator" to "jgnash.uifx.plugin.TestFxPlugin", "Plugin-Version" to "2.25"))
  }
}
