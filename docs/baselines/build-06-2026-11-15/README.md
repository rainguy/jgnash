# BUILD-06 dependency integrity baseline

Recorded on 2026-11-15 on Linux x64 using OpenJDK 21.0.13.0.3+1 and Gradle 8.14.3.

## Lock review

Twelve project lockfiles cover the root project, ten application subprojects,
and the included build-logic project. The generated settings lock records the
local version-catalog configuration. All resolvable project configurations use
strict mode, including empty configurations and four JavaFX verification graphs.

The selected `testRuntimeClasspath` modules were compared with the archived
[DEP-02 dependency trees](../dep-02-2026-11-12/test-runtime-after.txt):

| Project | Baseline modules | Locked modules | Version/addition/removal differences |
|---|---:|---:|---:|
| jgnash-tests | 73 | 73 | 0 |
| jgnash-fx | 84 | 84 | 0 |
| build-logic | 12 | 12 | 0 |

The legacy macOS `runtime` alias and `runtimeClasspath` contain the same 69
locked module versions. The alias now shares variant attributes with its source
and is visible during ordinary lock maintenance.

## Verification review

- Root metadata contains 206 components and 376 artifact/metadata SHA-256 entries.
- Standalone build-logic metadata contains 51 components and 100 entries; every
  entry agrees with the corresponding root checksum.
- All seven JavaFX modules have checksums for all four supported native
  classifiers: `linux`, `win`, `mac`, and `mac-aarch64` (28 native JARs).
- Metadata verification is enabled. No blanket trust rules, ignored artifacts,
  alternate checksums, or unused Kotlin native compiler distributions are recorded.
- Initial hashes were generated from approved repository resolutions. The full
  validation gate was then run with a new dependency cache, without write/update
  flags. Only the previously checksum-verified wrapper distribution was reused.

## Local evidence

```shell
./gradlew -p build-logic test --tests '*DependencyIntegrityFunctionalTest' --warning-mode=fail --no-daemon --stacktrace
GRADLE_USER_HOME=<fresh-directory> ./gradlew build-logic:test resolveAllDependencies build-logic:resolveAllDependencies clean check --warning-mode=fail --no-daemon --stacktrace
GRADLE_USER_HOME=<same-directory> ./gradlew -p build-logic resolveAllDependencies --warning-mode=fail --no-daemon --stacktrace
```

The three focused cases pass: missing locks/version drift/explicit refresh,
altered JAR bytes, and altered POM bytes. The complete gate passes with 376
discovered tests, zero failures/errors, and two expected Yahoo network skips.
This preserves the preceding 373 tests and adds three dependency-integrity
cases. Per-suite counts are recorded in [test-discovery.tsv](test-discovery.tsv).

The [dependency policy](../../dependency-policy.md) describes intentional
refreshes and the scheduled/manual fresh-cache hosted matrix. Hosted acceptance
is recorded in the modernization execution ledger after the matrix completes.
