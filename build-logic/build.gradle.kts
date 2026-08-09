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
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.7.1")
}

tasks.test {
    useJUnitPlatform()
}
