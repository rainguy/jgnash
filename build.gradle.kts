plugins {
    id("com.github.ben-manes.versions")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply(plugin = "java")
}

subprojects {
    group = "jgnash"
    version = "3.6.0"
}
