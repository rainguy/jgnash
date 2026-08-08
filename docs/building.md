# Building jGnash

The current build requires a Java 21 JDK. The Gradle wrapper is pinned
to Gradle 8.14.3 and verifies the downloaded distribution before use.

## Java toolchain behavior

The root build declares a Java 21 toolchain for every Java project. This means:

- every Java compilation uses a Java 21 compiler;
- `--release 21` produces Java 21 class files without depending on the JVM that
  launched Gradle;
- every test task runs on the selected Java 21 toolchain;
- source, Javadoc, and generated documentation encodings are UTF-8;
- preview Java features are not enabled.

Gradle can discover JDK installations from standard operating-system
locations, `JAVA_HOME`, and Gradle's Java installation settings. If Java 21 is
not discoverable, the `verifyJava21Toolchain` task fails with installation and
configuration guidance before compilation starts.

To use an installation in a non-standard location, add its absolute path to a
user-level Gradle property; do not commit a developer-specific path:

```properties
org.gradle.java.installations.paths=/absolute/path/to/jdk-21
```

## Common commands

On Linux or macOS:

```shell
./gradlew verifyJava21Toolchain
./gradlew clean test --warning-mode fail
./gradlew :jgnash-fx:distZip --warning-mode fail
```

On Windows, use `gradlew.bat` with the same task names and options.

`JAVA_HOME` may point to a Gradle-compatible launcher JDK instead of the JDK
used for compilation, provided a Java 21 toolchain is discoverable. CI installs
Temurin 21 explicitly and treats Gradle deprecations as failures.

The historical Java 11 environment under `docs/baselines/legacy-java11` exists
only to reproduce the pre-modernization baseline. It is not the toolchain for
current development builds.
