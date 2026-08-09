plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("javaConventions") {
            id = "jgnash.java-conventions"
            implementationClass = "jgnash.buildlogic.JgnashJavaConventionsPlugin"
        }
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
