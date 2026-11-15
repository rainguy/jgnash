# Dependency locking and verification

The shared `gradle/dependency-policy.gradle.kts` script enables locking for all
project configurations, including configurations created later by plugins.
Strict mode requires committed lock state for every configuration that is
resolved. Each application project and the included `build-logic` build has its
own `gradle.lockfile`. Empty configurations are recorded as well.

`resolveAllDependencies` resolves every resolvable project configuration. CI
runs it in both builds so a configuration that ordinary tests do not use still
gets checked. The build-logic project produces JVM plugins only and disables
resolution of Kotlin's unused `kotlinNativeBundleConfiguration`; no native
compiler distribution is needed to build these plugins.

`gradle/verification-metadata.xml` records SHA-256 digests for external artifacts
and POM/Gradle metadata. Verification is enabled automatically by the presence
of this file and defaults to strict failure. Plugin artifacts and metadata are
covered too; plugin versions remain declared in the catalog or supplied by the
pinned Gradle distribution. No blanket artifact-trust exclusions are used.

The root verification file governs included builds when building the application.
`build-logic/gradle/verification-metadata.xml` protects standalone invocations
such as `./gradlew -p build-logic test`; maintain both files when their shared
dependencies change. CI checks standalone resolution too.

## JavaFX platforms

The JavaFX plugin normally chooses native variants for the current host.
The four isolated `javafxVerification*` configurations additionally resolve all
seven JavaFX modules for Linux x64 (`linux`), Windows x64 (`win`), macOS Intel
(`mac`), and macOS Apple Silicon (`mac-aarch64`). Each graph requests its seven
modules directly without transitives, avoiding conflicts between native variants.
These configurations are used for verification and maintenance and do not feed
the application runtime or distribution.

The legacy macOS packaging `runtime` alias is also always available to the
resolver. It inherits the runtime classpath's dependencies and variant attributes,
so lock refreshes cover it even when packaging tasks are not requested.

Lockfiles record module versions; verification metadata records individual
classifier artifacts and their checksums. Review both when changing JavaFX.

## Updating dependencies

1. Read the upstream release/migration notes and change the intended catalog
   entries. Keep persistence/security migrations in their own work items.
2. Generate lock and verification changes with Java 21 from the repository root:

   ```shell
   ./gradlew resolveAllDependencies build-logic:resolveAllDependencies --write-locks --write-verification-metadata sha256 --warning-mode=fail --no-daemon
   ./gradlew -p build-logic resolveAllDependencies test --write-locks --write-verification-metadata sha256 --warning-mode=fail --no-daemon
   ```

   For a targeted lock refresh, `--update-locks group:module` can replace
   `--write-locks`. Include all intentionally affected transitive modules.
   If task-specific detached dependencies need new checksums, run that task
   explicitly with `--write-verification-metadata sha256` and review its additions.
3. Review every changed module/version and every checksum against the intended
   dependency graph and artifacts fetched from the approved HTTPS repositories.
   Compare available upstream checksums/signatures independently. Initial
   checksum generation records the bytes fetched; it does not establish their
   publisher's identity or assess vulnerabilities. Metadata generation is
   incremental: inspect retained old entries and any added `also-trust` values.
   Do not accept a new checksum for an unchanged coordinate without explaining
   and verifying the upstream change. Do not bypass a failure using lenient/off
   verification modes, blanket trust rules, or an unreviewed regeneration.
4. Commit the catalog/build changes, affected lockfiles, and both relevant
   verification files together. The pull-request template requires this review.
5. Validate without generation flags:

   ```shell
   ./gradlew build-logic:test clean check --warning-mode=fail --no-daemon --stacktrace
   ./gradlew resolveAllDependencies build-logic:resolveAllDependencies --warning-mode=fail --no-daemon
   ./gradlew -p build-logic resolveAllDependencies --warning-mode=fail --no-daemon
   ```

   Require the complete hosted matrix, including fresh-cache verification.
   Use `gradlew.bat` with the same arguments on Windows.

## Fresh-cache CI

The weekly Monday CI run uses an isolated Gradle user home and disables
`setup-gradle` cache restoration and saving on all four platforms. It resolves
all configurations, runs the clean test gate, and verifies standalone build logic
using committed locks and checksums. CI never generates replacement lock or
verification state. Failure reports include dependency-verification reports.

The same path can be exercised immediately from Actions using **Run workflow →
Start with a fresh Gradle dependency cache**, or:

```shell
gh workflow run ci.yml --ref master -f fresh_dependencies=true
```

For a local check, set `GRADLE_USER_HOME` to a newly created empty directory before
running the validation commands. Do not copy a dependency cache into that directory.

The build-logic functional suite tests missing locks, an incompatible dependency
version, an explicit lock refresh, and altered JAR/POM bytes under unchanged
coordinates using an isolated synthetic repository.

See Gradle's [dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html)
and [dependency verification](https://docs.gradle.org/current/userguide/dependency_verification.html)
documentation and the Gradle action's [cache controls](https://github.com/gradle/actions/blob/v6/docs/setup-gradle.md#disabling-caching).
