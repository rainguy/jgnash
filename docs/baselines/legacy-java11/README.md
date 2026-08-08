# Legacy Java 11 build baseline

This directory records the reproducible pre-modernization baseline required by
`BASE-03` in `MODERNIZATION_PLAN.md`. The baseline was captured on 2026-08-07
from source commit `bc5aa91fa27c7f668e1cfb5c67abaea4408fb8e3`. No production source was
changed while establishing it.

## Environment

| Component | Recorded value |
|---|---|
| Host | EndeavourOS Linux, kernel `7.1.5-arch1-2`, x86_64 |
| JDK | Eclipse Temurin `11.0.32+9`, HotSpot 64-bit |
| JDK archive | `OpenJDK11U-jdk_x64_linux_hotspot_11.0.32_9.tar.gz` |
| JDK archive SHA-256 | `5906e0339e9322a688b2375eaf40666e00a16e008b0067b0a9f9e4b6c5033720` |
| Gradle | Wrapper `6.8.2`, revision `b9bd4a5c6026ac52f690eaf2829ee26563cad426` |
| Wrapper JAR SHA-256 | `e996d452d2645e70c01c11143ca2d3742734a28da2bf61f25c82bdc288c9e637` |
| Isolated Gradle home | `/tmp/jgnash-gradle` |

The JDK archive was obtained from the official Eclipse Adoptium Temurin
`jdk-11.0.32+9` GitHub release and verified before extraction. The absolute
paths above are examples from the capture host; any private temporary directory
may be used when reproducing the run.

## Test baseline

Command:

```shell
env GRADLE_USER_HOME=/tmp/jgnash-gradle \
    JAVA_HOME=/tmp/jgnash-jdk11 \
    ./gradlew clean test --no-daemon
```

Result: `BUILD SUCCESSFUL in 4m 14s`; 39 actionable tasks, of which 30 were
executed and 9 were up-to-date.

| Module | Tests | Skipped | Failures | Errors | Suite time |
|---|---:|---:|---:|---:|---:|
| `jgnash-fx` | 1 | 0 | 0 | 0 | 1.531 s |
| `jgnash-tests` | 339 | 2 | 0 | 0 | 227.073 s |
| `mt940` | 5 | 0 | 0 | 0 | 0.108 s |
| **Total** | **345** | **2** | **0** | **0** | **228.712 s** |

The two skipped cases are the network-dependent
`YahooEventParserTest.testHistoricalDownload()` and
`YahooEventParserTest.testParser()` tests. They are existing skips, not new
regressions. The sanitized, class-level module reports are archived in
[`test-results.tsv`](test-results.tsv). Raw Gradle HTML/XML output is generated
under each module's ignored `build/reports/tests` and `build/test-results`
directories and is intentionally not versioned because standard output can
contain temporary paths and credential-bearing connection strings.

## Dependency baseline

The complete Gradle dependency report for every runtime-bearing project is
archived in [`runtime-dependencies.txt`](runtime-dependencies.txt). It covers:

- `jgnash-bayes`
- `jgnash-resources`
- `jgnash-core`
- `jgnash-convert`
- `jgnash-plugin`
- `jgnash-report-core`
- `jgnash-fx`
- `jgnash-fx-test-plugin`
- `mt940`

Reproduction command:

```shell
env GRADLE_USER_HOME=/tmp/jgnash-gradle \
    JAVA_HOME=/tmp/jgnash-jdk11 \
    ./gradlew \
      :jgnash-bayes:dependencies \
      :jgnash-resources:dependencies \
      :jgnash-core:dependencies \
      :jgnash-convert:dependencies \
      :jgnash-plugin:dependencies \
      :jgnash-report-core:dependencies \
      :jgnash-fx:dependencies \
      :jgnash-fx-test-plugin:dependencies \
      :mt940:dependencies \
      --configuration runtimeClasspath --console=plain --no-daemon
```

The dependency update snapshot is archived in
[`dependency-updates.txt`](dependency-updates.txt). It is advisory: version
discovery includes incompatible majors, alpha versions, and early-access
JavaFX releases, so none of its suggestions should be applied blindly.

Artifact checksums:

| File | SHA-256 |
|---|---|
| `runtime-dependencies.txt` | `11275f6452e8c689a8cbb65bb8931c5be1fcc3388e07cc33150ca925ffc9b9c8` |
| `dependency-updates.txt` | `9f0fe9235f18189c3668f5105a230dcd6bdcc84fbd41e8bd457f54b259c4307b` |
| `test-results.tsv` | `72f166ef917c1fa93e5fae120ab7873d92a94a106b9890c7a29294d6432d257a` |

## Distribution baseline

Command:

```shell
env GRADLE_USER_HOME=/tmp/jgnash-gradle \
    JAVA_HOME=/tmp/jgnash-jdk11 \
    ./gradlew :jgnash-fx:distZip --no-daemon
```

Result: `BUILD SUCCESSFUL in 5s`. The task produced
`jgnash-3.6.0-bin.zip`, containing 120 entries with 72,321,260 uncompressed
bytes. Its capture-time SHA-256 was
`a72ca09c76fa0cea1b15d64fe7cf66569ab71383543de1f82b1ce38048943a56`.
The 62 MiB generated archive remains ignored rather than being committed.

This result depends on the legacy inputs already present in the repository,
including the generated manual PDF and Windows Rust launcher. It proves that
the old task executes; it does not establish reproducibility across hosts.

## Expected limitations and observations

- Gradle reports deprecated features that are incompatible with Gradle 7.0.
- Running this Gradle version on Java 21 fails during Java compilation with an
  `IllegalAccessError` involving javac internals. Java 11 is therefore required
  solely for this historical baseline.
- Test output exposes a password-bearing H2 JDBC URL. The value is deliberately
  omitted here and remediation is tracked by `SEC-02`.
- XStream reports that its security framework is not explicitly initialized.
  Remediation is tracked by `SEC-06`.
- PDFBox builds a host font cache and uses fallback fonts for Symbol and
  ZapfDingbats. PDF rendering therefore needs explicit cross-platform coverage.
- Nashorn emits its Java 11 removal warning during the script filter test.
- No tests failed. The warnings above are baseline observations, not reasons to
  weaken or suppress tests.

## Reproduction rules

Use a clean checkout of the recorded source commit, a verified Temurin 11
archive, and an empty isolated `GRADLE_USER_HOME`. Do not reuse user-global
Gradle caches or credentials. Dependency reports are time-sensitive because
remote metadata changes; compare resolved coordinates first and available
updates second.
