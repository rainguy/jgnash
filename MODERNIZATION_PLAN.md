# jGnash Modernization Plan

Status: in progress

Prepared: 2026-08-07

Scope: the complete jGnash repository, including Java modules, JavaFX application, persistence formats, plugins, tests, CI, packaging, documentation, and the Rust launcher

## 1. Purpose

This document is the reusable implementation plan for modernizing jGnash. It is intended to be usable as:

- a roadmap for maintainers;
- a source for GitHub issues and milestones;
- a review checklist for modernization pull requests;
- a record of sequencing and compatibility constraints;
- a safety plan for user financial data and network-facing features;
- a definition of done for a modernized release.

The plan deliberately separates build recovery, dependency upgrades, persistence migration, security work, architecture work, and user-facing changes. These areas should not be combined into one large pull request.

## Execution tracking

This document is also the authoritative execution ledger. A work item is marked complete only after its acceptance criteria have been implemented and validated. The implementation commit is recorded here; the plan annotation normally follows in a separate tracking commit so that the implementation hash is stable and known. Work items not listed in this table remain pending.

| Work item | Status | Implementation commit | Completed | Notes |
|---|---|---|---|---|
| `BASE-01` | Complete | [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56) | 2026-08-07 | Added the active support policy and linked it from the README. |
| `BASE-02` | Complete | [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56) | 2026-08-07 | Added the ADR process, template, index, and seven initial accepted decisions. |
| `BASE-03` | Complete | [`5c7038746`](https://github.com/rainguy/jgnash/commit/5c7038746) | 2026-08-07 | Archived the verified Temurin 11 environment, 345-test result set, nine runtime dependency reports, dependency-update snapshot, and successful legacy distribution fingerprint. |
| `BASE-04` | Complete | [`499c7d06a`](https://github.com/rainguy/jgnash/commit/499c7d06a) | 2026-08-07 | Added 10 valid and five truncated synthetic fixtures spanning all five persistence stores, the three SQL password variants, reviewed semantic summaries, payload hashes with full manifest coverage, and documented generation/provenance. The 20-test catalog suite passes on both Java 21 and the recorded Java 11 baseline; the full Java 21 suite passes with 365 tests and two skips. |
| `BASE-05` | Complete | [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56) | 2026-08-07 | Added the modernization pull-request scope, compatibility, data-safety, security, validation, rollback, and documentation checklist. |
| `BUILD-01` | Complete | [`211c8d16e`](https://github.com/rainguy/jgnash/commit/211c8d16e) (Gradle 7 bridge), [`f49a0aaba`](https://github.com/rainguy/jgnash/commit/f49a0aaba) (Gradle 8 target) | 2026-08-07 | Completed the isolated 6.8.2 → 7.6.6 → 8.14.3 path with official wrapper checksums, fatal deprecation gates, public Gradle APIs, explicit JUnit launchers, and successful legacy distribution builds. |
| `BUILD-02` | Complete | [`a85f599c9`](https://github.com/rainguy/jgnash/commit/a85f599c9) | 2026-08-08 | Declared Java 21 toolchains and release bytecode, documented discovery, and fixed Java 21 Nashorn, PDF locale-spacing, and Javadoc compatibility. Local toolchain and bytecode proofs pass; [hosted run 31290582782](https://github.com/rainguy/jgnash/actions/runs/31290582782) validates clean Temurin 21 builds on Ubuntu, Windows, macOS Intel, and macOS Apple Silicon. |
| `BUILD-03` | Complete | [`f0f07d2b0`](https://github.com/rainguy/jgnash/commit/f0f07d2b0) | 2026-08-08 | Added an included `build-logic` convention plugin for Java 21, compilation, Javadoc, JUnit Platform, shared test dependencies, concise test logging, reproducible archives, coordinates, and manifest metadata. All modules apply it explicitly; its warning-clean TestKit functional test is wired into `check`. The full suite and legacy distribution build pass. |
| `BUILD-04` | Complete | [`b16130cf2`](https://github.com/rainguy/jgnash/commit/b16130cf2) | 2026-08-08 | Moved every production library and external plugin version to `gradle/libs.versions.toml`, documented compatibility pins, shared the catalog with `build-logic`, converted JavaFX platform classifiers to catalog variants, and removed `gradle.properties`. Updated the dependency-report plugin to its current stable ID and version; catalog-aware update reporting, a clean full check, and the legacy distribution build pass with fatal Gradle deprecation handling. |
| `CI-01` | Complete | [`0df7d686b`](https://github.com/rainguy/jgnash/commit/0df7d686b), [`2bef88df2`](https://github.com/rainguy/jgnash/commit/2bef88df2), [`73ec99a11`](https://github.com/rainguy/jgnash/commit/73ec99a11), [`ec5ce8c51`](https://github.com/rainguy/jgnash/commit/ec5ce8c51), [`d58232890`](https://github.com/rainguy/jgnash/commit/d58232890) | 2026-08-08 | Replaced duplicated workflows with one least-privilege, concurrency-controlled matrix for pushes, pull requests, and manual runs. Actions use reviewed full SHAs, failed tests upload reports, fixture behavior is deterministic across hosts, and [run 31290582782](https://github.com/rainguy/jgnash/actions/runs/31290582782) passes on all four required OS/architecture targets. |
| `CI-02` | Complete | [`0df7d686b`](https://github.com/rainguy/jgnash/commit/0df7d686b) | 2026-08-08 | Replaced obsolete wrapper validation with the maintained Gradle action pinned to a reviewed full SHA. It runs for pull requests, pushes, and manual dispatch; [run 31290582782](https://github.com/rainguy/jgnash/actions/runs/31290582782) passed wrapper validation. |
| `DEP-02` | Partially implemented | [`f31311aec`](https://github.com/rainguy/jgnash/commit/f31311aec) | — | Updated JUnit 5, JUnit Platform, Awaitility, TestFX, Monocle, and junit-extensions on Java 21-compatible lines. The clean suite passes with 359 tests and two expected network skips; BOM adoption and the junit-extensions removal assessment remain open. |
| `DEP-03` | Partially implemented | [`f31311aec`](https://github.com/rainguy/jgnash/commit/f31311aec) | — | Updated Picocli, Nashorn, and Apache Commons Lang, CSV, Collections, and Text. Compilation and the complete suite pass; focused CSV behavior fixtures and dependency-removal review remain open. |
| `DEP-05` | Partially implemented | [`f31311aec`](https://github.com/rainguy/jgnash/commit/f31311aec) | — | Updated POI to 5.5.1 and PDFBox on the compatible 2.0 line to 2.0.37. Existing spreadsheet semantic and PDF raster tests pass; representative office-application validation and the separate PDFBox 3 migration remain open. |
| `DEP-06` | Partially implemented | [`f31311aec`](https://github.com/rainguy/jgnash/commit/f31311aec) | — | Updated XStream and xstream-hibernate to 1.4.21. The data-format catalog and complete suite pass without fixture changes; explicit permission and malicious-input acceptance remain open. |
| `DEP-07` | Partially implemented | [`f31311aec`](https://github.com/rainguy/jgnash/commit/f31311aec) | — | Updated Netty to stable 4.1.137.Final and passed distributed-lock, encrypted-lock, and file-transfer tests. Backpressure and the later remote-security suite remain open. |
| `DEP-08` | Partially implemented | [`f31311aec`](https://github.com/rainguy/jgnash/commit/f31311aec) | — | Updated JavaFX to the 21.0.12 line and aligned TestFX/Monocle compatibility dependencies. Automated JavaFX tests and the Linux distribution pass; the Windows/macOS native matrix, packaged smoke tests, and visual/accessibility review remain open. |
| `SEC-07` | Complete | [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56) | 2026-08-07 | Added private reporting routes, response targets, disclosure rules, and the current remote-mode warning. |

Current milestone: `M2 - Modern CI and dependency hygiene`

- `M0 - Baseline and safety net` is complete; all five `BASE` work items have accepted implementation commits.
- `M1 - Build restored on Java 21` is complete; BUILD-01 through BUILD-04 have accepted implementation and validation evidence.
- `CI-01` and `CI-02` establish the cross-platform hosted safety net for the remaining M2 work.

## 2. Current repository baseline

The following observations were verified from the repository on 2026-08-07.

### 2.1 Project shape

- The repository contains approximately 92,000 lines of Java in 516 Java source files.
- The Gradle build contains these Java subprojects:
  - `jgnash-bayes`
  - `jgnash-resources`
  - `jgnash-core`
  - `jgnash-convert`
  - `jgnash-plugin`
  - `jgnash-fx`
  - `jgnash-report-core`
  - `jgnash-fx-test-plugin`
  - `mt940`
  - `jgnash-tests`
- The repository also contains:
  - a Rust launcher under `rust-launcher`;
  - Rhino example scripts under `rhino-scripts`;
  - a LaTeX user manual under `jgnash-manual`;
  - GitHub Actions workflows for Linux, macOS, Windows, and wrapper validation.
- The separation between the UI-independent modules and `jgnash-fx` is a useful foundation and should be preserved.
- There are no `module-info.java` files. Several jars only declare `Automatic-Module-Name`.

### 2.2 Build and runtime baseline

- The wrapper pins Gradle 6.8.2 in `gradle/wrapper/gradle-wrapper.properties`.
- The documented runtime baseline is Java 11.
- The build does not define a Java toolchain or `--release` target.
- Consequently, produced bytecode and API usage depend on whichever JDK happens to run Gradle.
- A verification run using Java 21 failed in `:jgnash-bayes:compileJava` before tests ran:
  - Gradle 6.8.2 attempted internal access to `com.sun.tools.javac.code.Symbol$TypeSymbol`;
  - Java 21 rejected that access with `IllegalAccessError`;
  - Gradle also reported that deprecated build features make the build incompatible with Gradle 7.
- The root build still declares `jcenter()` and `mavenLocal()` in addition to Maven Central.
- Version values are spread across `gradle.properties` and individual build scripts.
- The resource build embeds the current date, build JVM, and operating system into artifacts, preventing reproducible output.

### 2.3 Dependency baseline

The dependency versions are predominantly from 2020-2021. Important examples include:

- JavaFX 15.0.1;
- Hibernate ORM 5.4.28.Final and `javax.persistence`;
- H2 1.4.200;
- HSQLDB 2.5.1;
- XStream 1.4.15;
- Netty 4.1.59.Final;
- SLF4J 1.8.0-beta4;
- JUnit 5.7.1;
- TestFX 4.0.16-alpha and an old Monocle build;
- Apache POI 5.0.0;
- PDFBox 2.0.22.

The repository's dependency-update task reports newer releases for nearly every managed dependency. That report also proposes alpha, beta, early-access, and incompatible major versions, so it must be treated as discovery output, not as an automatic upgrade prescription.

### 2.4 Persistence baseline

- `jgnash-core` supports several durable formats:
  - binary XStream;
  - XML XStream;
  - old H2 page-store files identified as H2 1.3;
  - H2 MVStore files identified as H2 1.4;
  - HSQLDB files.
- Approximately 49 source files import `javax.persistence`.
- `persistence.xml` uses the Java Persistence 2.0 namespace.
- Hibernate schema handling uses `hibernate.hbm2ddl.auto=update`.
- The code contains explicit compatibility behavior for old H2 storage formats.
- The shared `EngineTest` contract is exercised by multiple storage implementations. This is a strong starting point for storage migration testing.
- Existing user files are financial records. Preserving the ability to open, back up, validate, and recover those records is the highest compatibility requirement in this plan.

### 2.5 Test baseline

- The repository has 47 Java test source files and about 173 JUnit test methods.
- Core engine behavior has substantial reusable contract coverage through `EngineTest`.
- There is no configured coverage report or coverage threshold.
- There is no explicit split between unit, integration, migration, network, and UI test suites.
- Several tests use wall-clock time, random values, or `Thread.sleep`, which can cause slow or nondeterministic CI.
- `jgnash-fx` contains 227 production Java files but only two files under `src/test`; one is primarily an interactive JavaFX control demonstration rather than an automated test.
- Test logging prints started and passed events plus standard streams, creating noisy CI output.

### 2.6 CI and release baseline

- The CI workflows use mutable `@master` references for several actions.
- The Linux, macOS, and Windows workflows are mostly duplicated.
- Normal CI is triggered on `push`, while only wrapper validation explicitly runs on pull requests.
- CI installs Java 11 and does not test a supported JDK matrix.
- The wrapper validation action and workflow versions are old.
- There is no visible dependency review, static analysis, SBOM generation, artifact provenance, or automated release pipeline.
- Packaging manually assembles cross-platform JavaFX jars, renames zip output in `doLast`, and includes a prebuilt Rust Windows executable.
- The application may download platform-specific JavaFX jars on first launch and then request a restart.
- The README still describes obsolete platform instructions, AdoptOpenJDK links, Travis CI, Java 12-14 testing, and manual AppleScript installation.

### 2.7 Security-sensitive baseline

The following items require security treatment, not only general refactoring:

- `BootLoader` downloads executable JavaFX jars at runtime and verifies them using an MD5 file downloaded from the same location.
- `EncryptionManager`:
  - derives a key by applying unsalted SHA-256 directly to the password;
  - requests provider-default `AES`, normally resulting in ECB mode;
  - does not authenticate ciphertext;
  - converts `char[]` passwords to immutable `String` values;
  - does not fully clear temporary password material.
- H2 remote access explicitly uses TCP while the SSL path is commented out.
- Database passwords are appended to JDBC URLs.
- One remote HSQLDB URL containing a password is logged.
- JPA configuration starts from `System.getProperties()` and then adds database configuration and credentials to that shared object.
- Proxy passwords are stored directly in Java Preferences.
- Remote database, message bus, lock, and attachment services need a single documented threat model and transport-security design.

These observations do not by themselves prove exploitation. They do justify treating remote access and credential handling as a priority audit and remediation workstream.

## 3. Modernization goals

### 3.1 Required outcomes

- A clean checkout builds and tests reproducibly with one documented command.
- The build runs on a maintained Gradle version and uses a declared Java toolchain.
- CI validates pull requests on Linux, macOS, and Windows.
- Shipped applications include a suitable runtime and all JavaFX components; first launch does not download executable dependencies.
- Dependencies are on maintained release lines or have documented exceptions.
- Existing supported jGnash data files can be migrated without silent data loss.
- Every destructive or format-changing migration creates a verified backup first.
- Storage schema changes are explicit, versioned, testable, and recoverable.
- Remote access is either secured with standard authenticated transport security or disabled by default until it is secure.
- Passwords and secrets do not appear in logs, URLs, global properties, generated reports, or ordinary preferences.
- CI produces useful test, coverage, dependency, and security reports.
- Release artifacts are platform-specific, checksummed, and generated by CI from a tagged commit.
- Documentation describes current supported platforms, data migration, backup behavior, and recovery steps.

### 3.2 Desired outcomes

- Core services can be tested without global singletons.
- UI controllers depend on narrow application services instead of calling `EngineFactory` throughout the UI.
- Storage implementations are selected through an explicit provider registry rather than reflective enum construction.
- Dependency declarations and common build behavior are centralized.
- Logging is structured enough to diagnose failures without leaking user data.
- Automated tests cover migration fixtures and high-value JavaFX workflows.
- The plugin API has an explicit compatibility/versioning policy.
- Contributors can understand the architecture from concise repository documentation.

### 3.3 Non-goals for the first modernization release

- Rewriting the application in another language.
- Replacing JavaFX with a web or mobile UI.
- Replacing every static factory in one change.
- Introducing a large dependency-injection framework solely to remove singletons.
- Removing old data formats before a tested migration path exists.
- Redesigning accounting behavior while infrastructure is changing.
- Combining UI redesign with the persistence migration.
- Enabling preview Java features in production.
- Selecting early-access, alpha, or beta dependencies without a written reason.

## 4. Guiding principles

- Protect user data before improving developer convenience.
- Establish a green baseline before broad refactoring.
- Change one major compatibility axis at a time.
- Keep upgrade pull requests small enough to bisect.
- Add characterization tests before changing legacy behavior.
- Treat file-format compatibility as a public API.
- Prefer standard JDK security and packaging capabilities over custom implementations.
- Prefer immutable, explicit configuration over process-global state.
- Prefer maintained release lines over the numerically newest artifact.
- Keep production and migration code observable, but never log secrets or full financial records.
- Make rollback possible at every storage and release milestone.
- Record consequential decisions as short architecture decision records under `docs/adr/`.

## 5. Proposed target baseline

These targets should be confirmed in ADRs before implementation. They are intentionally conservative bridge targets.

### 5.1 First supported modernization baseline

- Java language/runtime baseline: Java 21.
- Gradle bridge target: latest stable Gradle 8.14 patch release.
- Gradle final target: current supported Gradle 9 release after plugin and script compatibility is proven.
- JavaFX bridge target: JavaFX 21 release line.
- Packaging: platform-specific `jpackage` installers/images containing a trimmed or complete runtime.
- Persistence namespace: Jakarta Persistence.
- Hibernate: a maintained Hibernate line selected after the migration fixture suite passes; do not jump directly from 5.4 without an intermediate compatibility step.
- Test framework: a maintained JUnit Jupiter release compatible with the chosen Gradle and Java baseline.
- CI runtime: Java 21 for the main build, with an additional compatibility lane for the next intended runtime.

### 5.2 Why use bridge targets

- Java 21 is a stable modernization target and is sufficient to remove the current Gradle/JDK build failure.
- Moving build tooling, Java language level, JavaFX, Hibernate, database engines, and packaging simultaneously would make failures difficult to isolate.
- Gradle 8.5 and later can run on Java 21; an 8.14 patch release provides a practical bridge before adopting Gradle 9.
- Matching Java and JavaFX major lines first reduces JavaFX compatibility variables.
- A later Java 25/JavaFX 25 move can be evaluated once storage migration and packaging are stable.

## 6. Delivery structure

Create these milestones, in order:

1. `M0 - Baseline and safety net`
2. `M1 - Build restored on Java 21`
3. `M2 - Modern CI and dependency hygiene`
4. `M3 - Security-critical packaging and credential fixes`
5. `M4 - Maintained non-persistence dependencies`
6. `M5 - Persistence migration tooling`
7. `M6 - Jakarta Persistence and maintained databases`
8. `M7 - Service boundaries and testability`
9. `M8 - JavaFX quality and accessibility`
10. `M9 - Modern release candidate`
11. `M10 - Post-modernization cleanup`

Each issue should use an ID from the work packages below, for example `BUILD-01`, `SEC-04`, or `DATA-07`.

## 7. Phase 0: baseline and governance

### BASE-01: Record the support policy

Status: **Complete** — implementation commit [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56)

- Add `docs/support-policy.md`.
- Define:
  - supported Java runtime;
  - supported operating-system versions and architectures;
  - supported data-file formats;
  - supported import/export formats;
  - plugin compatibility guarantees;
  - how long old releases receive security/data-migration fixes.
- Explicitly state whether remote multi-user mode is supported during modernization.
- Acceptance criteria:
  - policy is reviewed before removing any compatibility path;
  - README links to it;
  - release checklist refers to it.

### BASE-02: Add architecture decision records

Status: **Complete** — implementation commit [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56)

- Create `docs/adr/README.md` with a lightweight ADR template.
- Create initial ADRs for:
  - Java 21 baseline;
  - Gradle bridge and final targets;
  - JavaFX packaging strategy;
  - persistence migration strategy;
  - remote-access security disposition;
  - JPMS adoption or deferral;
  - plugin compatibility policy.
- Every ADR must include context, decision, alternatives, consequences, and rollback/revisit conditions.

### BASE-03: Capture a legacy build baseline

Status: **Complete** — implementation commit [`5c7038746`](https://github.com/rainguy/jgnash/commit/5c7038746)

- Provision a reproducible Java 11 environment solely for the legacy baseline.
- Run and archive:
  - `./gradlew clean test`;
  - module-level test reports;
  - dependency tree for every runtime module;
  - `dependencyUpdates` output;
  - a distribution build if it currently works.
- Record expected failures separately from new regressions.
- Do not weaken tests merely to obtain a green baseline.
- Acceptance criteria:
  - legacy baseline results are attached to the milestone or stored as CI artifacts;
  - the exact JDK vendor/version and operating system are recorded;
  - no production sources change in this issue.

### BASE-04: Build a data-format fixture catalog

Status: **Complete** — implementation commit [`499c7d06a`](https://github.com/rainguy/jgnash/commit/499c7d06a)

- Inventory every supported extension and on-disk variant.
- Include fixtures for:
  - XML XStream;
  - binary XStream;
  - H2 page store;
  - H2 MVStore;
  - HSQLDB;
  - password-protected variants;
  - attachments;
  - multiple currencies;
  - investments, splits, reminders, budgets, tags, and reconciled transactions;
  - empty and minimal files;
  - deliberately corrupt/truncated files.
- Use synthetic or rigorously anonymized data only.
- For each valid fixture, store a machine-readable expected summary:
  - account count and hierarchy;
  - commodity/security count;
  - transaction and transaction-entry counts;
  - debit/credit totals by currency;
  - earliest/latest dates;
  - reminder/budget/tag counts;
  - attachment names, sizes, and hashes;
  - file-format version.
- Decide whether large or sensitive fixtures belong in Git LFS or a protected test-data store.
- Acceptance criteria:
  - all fixtures can be opened by the legacy release;
  - expected summaries are independently generated and reviewed;
  - fixture licenses/provenance are documented.

### BASE-05: Define modernization pull-request rules

Status: **Complete** — implementation commit [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56)

- Require each modernization PR to include:
  - one primary change category;
  - compatibility impact;
  - data-format impact;
  - security impact;
  - tests executed;
  - rollback plan;
  - documentation impact.
- Do not combine dependency major upgrades unless they are inseparable.
- Require before/after dependency trees for dependency changes.
- Require migration fixtures for persistence changes.
- Require screenshots only for visible JavaFX changes.

## 8. Phase 1: restore and modernize the build

### BUILD-01: Upgrade Gradle in controlled hops

Status: **Complete** — Gradle 7 bridge commit [`211c8d16e`](https://github.com/rainguy/jgnash/commit/211c8d16e); Gradle 8.14.3 target commit [`f49a0aaba`](https://github.com/rainguy/jgnash/commit/f49a0aaba)

- Perform wrapper upgrades in isolated commits or PRs:
  1. Gradle 6.8.2 to the latest 7.6 patch using Java 11;
  2. resolve all Gradle 7 deprecations;
  3. Gradle 7.6 to the selected Gradle 8.14 patch;
  4. resolve all Gradle 8 warnings;
  5. defer Gradle 9 until obsolete plugins and packaging tasks are replaced.
- Update wrapper distribution checksums.
- Regenerate wrapper scripts and jar using the wrapper task; do not download an arbitrary wrapper jar manually.
- Run `./gradlew help --warning-mode all` after each hop.
- Remove use of Gradle internal APIs such as `org.gradle.internal.jvm.Jvm` and `org.gradle.internal.os.OperatingSystem`.
- Replace deprecated properties such as `mainClassName` and `buildDir` access with supported APIs.
- Acceptance criteria:
  - `./gradlew --version` runs on Java 21;
  - `./gradlew help --warning-mode fail` succeeds on the bridge target;
  - wrapper validation succeeds;
  - no unrelated dependency major upgrades are included.

### BUILD-02: Declare Java toolchains and bytecode target

Status: **Complete** — implementation commit [`a85f599c9`](https://github.com/rainguy/jgnash/commit/a85f599c9); clean hosted acceptance on Ubuntu, Windows, macOS Intel, and macOS Apple Silicon in [run 31290582782](https://github.com/rainguy/jgnash/actions/runs/31290582782).

- Add a Java toolchain convention for Java 21 to every Java project.
- Set `options.release` to 21 for all `JavaCompile` tasks.
- Make source encoding explicit as UTF-8.
- Make Javadoc encoding explicit as UTF-8.
- Configure test JVMs through the toolchain, not `JAVA_HOME` assumptions.
- Fail early with a useful message if a required toolchain is unavailable.
- Acceptance criteria:
  - build output is Java 21 bytecode regardless of the launcher JDK;
  - CI proves the build can run from a clean machine;
  - README documents the toolchain behavior.

### BUILD-03: Centralize build conventions

Status: **Complete** — implementation commit [`f0f07d2b0`](https://github.com/rainguy/jgnash/commit/f0f07d2b0)

- Create an included `build-logic` build or convention plugins.
- Centralize:
  - Java toolchain;
  - compiler options;
  - JUnit Platform configuration;
  - test logging;
  - reproducible archive settings;
  - static-analysis configuration;
  - group/version conventions;
  - common manifest metadata.
- Remove repeated JUnit/TestFX/Awaitility declarations where a test fixture or convention is appropriate.
- Keep module-specific dependencies in module build files.
- Acceptance criteria:
  - subproject build files become declarative;
  - behavior is covered by at least one build-logic functional test;
  - module boundaries remain unchanged in this issue.

### BUILD-04: Introduce a version catalog

Status: **Complete** — implementation commit [`b16130cf2`](https://github.com/rainguy/jgnash/commit/b16130cf2)

- Move library and plugin versions to `gradle/libs.versions.toml`.
- Use bundles only where the dependency set is genuinely cohesive.
- Distinguish stable from pre-release dependencies.
- Add comments or documentation for pinned exceptions.
- Remove obsolete version properties from `gradle.properties`.
- Acceptance criteria:
  - a dependency version has one source of truth;
  - no alpha/beta/EA upgrade is selected accidentally;
  - dependency update tooling understands the catalog.

### BUILD-05: Make repositories deterministic

- Move repository declarations to dependency resolution management in `settings.gradle.kts`.
- Remove `jcenter()`.
- Remove `mavenLocal()` from normal builds.
- If local publication is needed for plugin development, enable it only via an explicit property and never in CI.
- Set repository mode to reject module-level repository declarations.
- Add content filters if a non-Central repository becomes necessary.
- Acceptance criteria:
  - a clean build resolves from an explicit allowlist of repositories;
  - CI cannot silently use artifacts from a developer's local Maven cache.

### BUILD-06: Enable dependency locking and verification

- Enable dependency locking for resolvable configurations.
- Generate and review lock state.
- Enable Gradle dependency verification with SHA-256 checksums.
- Document the approved process for refreshing locks and verification metadata.
- Ensure platform-specific JavaFX artifacts are represented correctly.
- Acceptance criteria:
  - unexpected artifact changes fail the build;
  - dependency update PRs include intentional lock/metadata changes;
  - CI starts from a clean dependency cache at least in a scheduled job.

### BUILD-07: Make artifacts reproducible

- Remove wall-clock `Date()` expansion from normal resources.
- Do not embed the build operating system or launcher JVM unless explicitly requested.
- If build metadata is needed, derive it from:
  - project version;
  - source revision;
  - optional `SOURCE_DATE_EPOCH`.
- Configure stable file order and normalized timestamps for archives.
- Compare hashes from two clean builds in CI.
- Acceptance criteria:
  - two clean builds of the same revision on the same target produce identical unsigned archives;
  - signing/notarization is treated as a later non-reproducible envelope.

### BUILD-08: Add compiler and API checks

- Initially enable useful warnings without failing the build.
- Establish and burn down a warning baseline.
- Later enable failure for newly introduced warnings.
- Evaluate:
  - `-Xlint:all` with documented exclusions;
  - forbidden API checks for insecure/deprecated APIs;
  - API compatibility checks for `jgnash-plugin` and intentionally public core APIs.
- Do not mix mass warning cleanup with behavioral changes.

## 9. Phase 2: CI, dependency automation, and quality gates

### CI-01: Replace duplicated CI with a matrix workflow

Status: **Complete** — primary implementation commit [`0df7d686b`](https://github.com/rainguy/jgnash/commit/0df7d686b), with cross-platform reliability hardening through [`d58232890`](https://github.com/rainguy/jgnash/commit/d58232890); all required jobs pass in [run 31290582782](https://github.com/rainguy/jgnash/actions/runs/31290582782).

- Create one primary CI workflow with a matrix for:
  - Ubuntu x64;
  - Windows x64;
  - macOS x64;
  - macOS arm64 when runner availability permits.
- Trigger on:
  - pull requests;
  - pushes to protected branches;
  - manual dispatch.
- Use concurrency cancellation for superseded branch builds.
- Grant the workflow minimal permissions.
- Upload test reports on failure.
- Use the official Gradle setup action for caching instead of a hand-built mutable cache step.
- Pin actions to reviewed full commit SHAs and annotate the corresponding release tag in comments.
- Acceptance criteria:
  - all required platforms run on pull requests;
  - one workflow controls the common build logic;
  - platform-specific failures remain independently visible.

### CI-02: Modernize wrapper validation

Status: **Complete** — implementation commit [`0df7d686b`](https://github.com/rainguy/jgnash/commit/0df7d686b); wrapper validation passes in [run 31290582782](https://github.com/rainguy/jgnash/actions/runs/31290582782).

- Replace the obsolete wrapper validation action with the maintained Gradle action.
- Pin it to a reviewed commit SHA.
- Validate wrapper changes on every pull request.
- Prevent merging a wrapper jar or distribution URL change without validation.

### CI-03: Split fast and slow test lanes

- Define Gradle suites or tagged tasks:
  - `test`: fast unit tests;
  - `integrationTest`: persistence and network tests;
  - `migrationTest`: old-file migration fixtures;
  - `uiTest`: automated JavaFX tests;
  - `compatibilityTest`: selected runtime/database compatibility checks.
- Pull-request requirements:
  - fast tests on every platform;
  - integration and migration tests at least on Linux;
  - UI smoke tests on relevant platform runners.
- Nightly requirements:
  - complete platform matrix;
  - clean dependency cache;
  - all fixtures;
  - extended security/dependency analysis.

### CI-04: Add dependency update automation

- Configure Renovate or Dependabot for:
  - Gradle dependencies;
  - Gradle wrapper;
  - GitHub Actions;
  - Rust Cargo dependencies.
- Group only compatible patch/minor updates.
- Keep JavaFX platform artifacts aligned.
- Do not auto-merge persistence engine, serialization, crypto, Netty, JavaFX, PDFBox, or POI major updates.
- Require the normal CI and migration suite before merging.
- Schedule updates to avoid flooding maintainers.

### CI-05: Add dependency and static security checks

- Enable GitHub dependency review on pull requests.
- Add a JVM dependency vulnerability scanner with a documented suppression policy.
- Add CodeQL or an equivalent maintained Java analysis workflow.
- Add secret scanning and push protection where repository settings permit.
- Generate a CycloneDX or SPDX SBOM for release artifacts.
- Treat findings as:
  - blocking when exploitable in the shipped application;
  - time-bounded exceptions when not reachable or when no compatible fix exists.
- Every suppression must include owner, rationale, evidence, and expiry date.

### CI-06: Add coverage reporting

- Add JaCoCo or another maintained coverage tool.
- Publish module-level HTML/XML reports.
- Start with reporting only.
- Set thresholds only after measuring the baseline.
- Prefer changed-line coverage and coverage of critical packages over a repository-wide vanity target.
- Minimum critical coverage expectations should eventually include:
  - transaction invariants;
  - import parsing;
  - storage migration;
  - encryption compatibility;
  - backup/restore;
  - remote authentication and authorization.

## 10. Phase 3: improve the test safety net

### TEST-01: Stabilize time-dependent tests

- Introduce `Clock` or a small `TimeProvider` at domain boundaries.
- Replace production `LocalDate.now()` calls where deterministic behavior matters.
- Use a fixed clock in tests.
- Add explicit tests around:
  - month/year boundaries;
  - leap days;
  - daylight-saving transitions where local time is used;
  - locale changes;
  - reminder recurrence boundaries.

### TEST-02: Remove sleep-based synchronization

- Replace arbitrary `Thread.sleep` calls with:
  - Awaitility conditions;
  - latches/barriers;
  - futures with timeouts;
  - explicit event acknowledgements.
- Apply strict upper timeouts so genuine deadlocks fail promptly.
- Make random seeds fixed and printed on failure.
- Acceptance criteria:
  - repeated test execution has no intermittent failures over a documented run count;
  - test duration decreases or stays bounded.

### TEST-03: Convert the engine contract to parameterized tests where practical

- Preserve the valuable shared engine behavior suite.
- Evaluate JUnit test templates or parameterized providers for storage backends.
- Keep backend-specific tests separate.
- Make cleanup resilient when setup fails.
- Use `@TempDir` rather than manually named files where possible.
- Do not run incompatible old and new H2 engines in the same JVM if classpath conflicts exist; use forked processes when necessary.

### TEST-04: Add accounting invariant/property tests

- Add generated tests for:
  - balanced debit/credit entries;
  - split transaction totals;
  - currency conversion round trips within defined precision;
  - reconciliation status transitions;
  - investment buy/sell/split/dividend sequences;
  - serialization round trips.
- Record the seed for every failure.
- Keep generated ranges realistic and bounded.

### TEST-05: Add migration golden tests

- For every valid fixture:
  1. copy the fixture to a temporary directory;
  2. calculate and retain the original checksum;
  3. run read-only inspection;
  4. create and verify a backup;
  5. migrate;
  6. open using the new engine;
  7. compare the expected summary;
  8. export to the interchange format;
  9. reopen the exported data;
  10. verify invariant totals again.
- For corrupt fixtures:
  - fail without modifying the original;
  - show an actionable message;
  - retain diagnostic logs without financial data.

### TEST-06: Establish a practical JavaFX test pyramid

- Extract presentation logic from large controllers into testable view models/services.
- Unit test formatting, validation, command enablement, and transformations without launching JavaFX.
- Keep a small TestFX suite for high-value flows:
  - create/open/close a file;
  - add/edit/delete a transaction;
  - reconcile an account;
  - import a representative file;
  - create a backup;
  - verify migration prompt behavior.
- Maintain a manual visual control gallery, but do not count it as an automated test.
- Add accessibility checks where tooling permits.

### TEST-07: Test plugin compatibility

- Build test plugins against the documented plugin API.
- Load plugins from packaged application images.
- Cover:
  - valid plugin activation;
  - incompatible API version;
  - missing dependency;
  - malformed manifest;
  - plugin exception isolation;
  - duplicate plugin identifiers.

## 11. Phase 4: security-critical work

### SEC-01: Remove runtime JavaFX downloads

- Replace first-run dependency download with platform-specific packaged images.
- Remove `BootLoader` download and MD5 verification behavior.
- Remove the restart-on-first-launch contract.
- Ensure artifacts are resolved and verified at build time.
- If a transitional zip remains, include all target-platform JavaFX dependencies in that zip and publish separate zips per platform.
- Acceptance criteria:
  - application starts offline on a clean supported system;
  - no executable jar is downloaded at runtime;
  - packaged contents are covered by release checksums/SBOM.

### SEC-02: Stop leaking credentials through configuration and logs

- Replace `System.getProperties()` use in JPA configuration with a new dedicated `Properties` instance.
- Do not put credentials in JDBC URLs when a driver property is available.
- Remove or redact URL logging.
- Add a centralized redaction utility for known secret keys and URI user-info/query parameters.
- Add tests that capture logs and assert passwords/tokens are absent.
- Audit exception messages from database drivers before displaying them to users.
- Acceptance criteria:
  - a canary password never appears in logs, system properties, reports, or exception dialogs;
  - configuration objects have clear ownership and limited lifetime.

### SEC-03: Replace custom message encryption with versioned authenticated encryption

- First document every use of `EncryptionManager` and the exact wire/storage compatibility requirement.
- Define a versioned encrypted envelope containing at least:
  - format version;
  - KDF identifier and parameters;
  - random salt;
  - cipher identifier;
  - random nonce;
  - ciphertext and authentication tag.
- Use a standard password KDF supported by the project security policy:
  - Argon2id through a reviewed dependency, or
  - PBKDF2-HMAC-SHA-256/512 through the JDK if minimizing dependencies.
- Use AES-GCM with a unique nonce for each message, or use TLS and remove application-level transport encryption where redundant.
- Authenticate protocol metadata as additional authenticated data.
- Maintain legacy decryption only as long as needed to migrate existing data/protocol peers.
- Never re-encrypt with the legacy format.
- Clear mutable password/key buffers in `finally` blocks where possible.
- Do not promise erasure of immutable `String` objects; avoid creating them from secrets.
- Obtain an independent security review before enabling remote use.

### SEC-04: Secure or temporarily restrict remote multi-user mode

- Produce a threat model covering:
  - database service;
  - message bus;
  - distributed locks;
  - attachment transfer;
  - authentication;
  - remote shutdown;
  - replay, downgrade, tampering, and brute-force threats.
- Choose one disposition:
  1. secure every channel with TLS 1.3 and certificate validation, plus explicit authentication and authorization; or
  2. bind to loopback and mark remote mode unsupported until the secure design ships.
- Never treat password-derived AES alone as transport authentication.
- Add connection limits, authentication failure backoff, maximum message/file sizes, and protocol timeouts.
- Define protocol version negotiation and reject unsafe downgrade.
- Add integration tests using invalid certificates, wrong passwords, replayed messages, and truncated frames.
- Acceptance criteria:
  - no plaintext remote database connection is enabled by default;
  - remote shutdown requires authenticated authorization;
  - all channels share a documented security posture.

### SEC-05: Move proxy credentials out of ordinary Preferences

- Prefer operating-system credential storage through a small abstraction.
- If a portable secure store cannot be provided, do not persist the proxy password by default.
- Allow session-only credentials.
- Migrate or remove existing plaintext preference values after user confirmation.
- Ensure backups and preference exports do not include secrets.

### SEC-06: Harden deserialization and plugin loading

- Audit every XStream instance for an explicit minimum type allowlist.
- Treat imported files and network payloads as untrusted.
- Set maximum file, nesting, collection, string, and attachment sizes where libraries permit.
- Add malicious/oversized input tests.
- Audit plugin jar paths, symlink handling, manifest values, and classloader isolation.
- Consider signed plugins only if there is a sustainable trust and key-management model.

### SEC-07: Add a vulnerability disclosure process

Status: **Complete** — implementation commit [`4c4707d56`](https://github.com/rainguy/jgnash/commit/4c4707d56)

- Add `SECURITY.md` with:
  - supported versions;
  - private reporting route;
  - expected response window;
  - disclosure coordination policy;
  - data-corruption severity guidance.
- Do not put active vulnerability details into public issues before a fix is available.

## 12. Phase 5: non-persistence dependency upgrades

### DEP-01: Define dependency upgrade lanes

- Lane A: patch/minor upgrades expected to be source-compatible.
- Lane B: major upgrades with localized API changes.
- Lane C: data-format, rendering, networking, or security-sensitive upgrades.
- Lane D: build plugins and packaging tools.
- Require stronger tests and rollback detail for lanes C and D.

### DEP-02: Upgrade test libraries first

- Upgrade JUnit Jupiter, Awaitility, TestFX, and Monocle to versions compatible with Java 21/JavaFX 21.
- Remove `junit-extensions` if its used features now exist in JUnit or a maintained alternative.
- Use the JUnit BOM to keep components aligned.
- Confirm test discovery counts before and after the upgrade.
- Do not accept a lower test count without an explanation.

### DEP-03: Upgrade low-risk utility libraries

- Upgrade Picocli and Apache Commons libraries in small groups.
- Review release notes for behavior changes in CSV parsing, escaping, locale handling, and string interpolation.
- Add focused import/export tests before Commons CSV changes.
- Remove dependencies whose used functionality is now in the JDK.

### DEP-04: Normalize logging

- Choose one application logging facade and backend strategy.
- A practical option is SLF4J 2.x with the JUL bridge or a single lightweight backend, but avoid bridge loops.
- Route third-party logs consistently.
- Add rolling file behavior and user-accessible diagnostics.
- Redact secrets and financial payloads.
- Remove the old beta SLF4J line.

### DEP-05: Upgrade document libraries separately

- Upgrade Apache POI in its own PR.
- Verify spreadsheet output with semantic assertions and representative office applications.
- Upgrade PDFBox 2 to 3 in a separate PR.
- Verify:
  - page count;
  - text extraction;
  - fonts;
  - images;
  - pagination;
  - paper sizes;
  - locale-specific numbers.
- Keep golden rendering comparisons tolerant of non-semantic metadata differences.

### DEP-06: Upgrade XStream with compatibility tests

- Upgrade to a maintained XStream release before broader persistence changes.
- Confirm explicit type permissions.
- Run every XML and binary fixture through round-trip and malicious-input tests.
- Do not rewrite user files merely because the library version changes.

### DEP-07: Upgrade Netty without selecting an alpha major

- Stay on a maintained stable Netty 4.1 release unless a protocol redesign explicitly adopts a later stable major.
- Prefer a Netty BOM if multiple Netty modules are introduced.
- Test framing, maximum payloads, disconnects, timeouts, and backpressure.
- Run the remote security suite before and after.

### DEP-08: Upgrade JavaFX

- Move from JavaFX 15 to JavaFX 21 only after Java 21 and packaging foundations are green.
- Update CSS, FXML, removed APIs, WebView behavior, media dependencies, and native platform classifiers.
- Verify Linux, Windows, macOS x64, and macOS arm64 independently.
- Add visual smoke tests for major screens.
- Record native accessibility and input-method issues separately from functional regressions.

## 13. Phase 6: persistence and data migration

This is the highest-risk technical phase. No database engine or ORM major upgrade should merge until the migration fixture catalog and backup rules are in place.

### DATA-01: Define the canonical interchange/backup format

- Decide whether the existing compressed XML export is sufficient as the long-term migration bridge.
- Document:
  - schema/version marker;
  - encoding;
  - ordering guarantees;
  - numeric precision and scale;
  - date/time semantics;
  - attachment representation;
  - checksum strategy;
  - forward/backward compatibility behavior.
- If XStream XML remains the interchange format, lock it down with a narrow model and explicit compatibility tests.
- Consider a purpose-built, versioned export format if XStream object graphs are too coupled to implementation classes.

### DATA-02: Add read-only preflight inspection

- Before migration, inspect without modifying:
  - detected format;
  - format version;
  - encryption/password validity;
  - file completeness;
  - required companion files;
  - available disk space;
  - write permissions for backup destination.
- Return a structured preflight result to the UI and CLI.
- Never invoke ORM automatic schema update during preflight.

### DATA-03: Implement verified backup-before-migrate

- Copy every primary and companion file to a versioned backup directory.
- Flush/close the legacy engine before copying.
- Calculate SHA-256 checksums for source and backup.
- Reopen or otherwise validate the backup with the legacy reader when practical.
- Write a manifest containing source format/version, application version, timestamp, and checksums.
- Never overwrite an existing backup.
- Abort migration if backup verification fails.

### DATA-04: Create a standalone migration command

- Add a CLI that can run without the JavaFX UI.
- Suggested interface:
  - `jgnash migrate inspect <source>`
  - `jgnash migrate backup <source> --destination <dir>`
  - `jgnash migrate convert <source> --destination <file>`
  - `jgnash migrate verify <source> <destination>`
  - `jgnash migrate restore <backup-manifest>`
- Support dry-run and machine-readable JSON output.
- Return stable exit codes.
- Never delete the source automatically.
- Make the UI call this service rather than duplicate migration behavior.

### DATA-05: Isolate legacy H2 readers

- H2 1.x and modern H2 file formats may not be directly compatible.
- Do not place incompatible H2 versions on one application classpath.
- Evaluate a forked legacy-reader process that:
  - runs the old compatible H2 engine;
  - opens the copied legacy database;
  - exports canonical interchange data;
  - exits;
  - never listens on a network interface.
- Import the canonical output into the modern database engine.
- Package the legacy reader only for the supported migration window.
- Test migration from both page-store and MVStore fixture generations.

### DATA-06: Replace implicit schema updates with explicit migrations

- Set normal ORM behavior to schema validation, not `update`.
- Introduce explicit versioned migrations using Flyway, Liquibase, or a small reviewed internal mechanism.
- Every migration must be:
  - deterministic;
  - transactional where the database supports it;
  - idempotence-aware;
  - covered by forward migration tests;
  - paired with recovery instructions.
- Record schema version separately from application/file-format version.
- Do not rely on Hibernate to infer production schema changes.

### DATA-07: Stage the Hibernate/Jakarta migration

- Suggested sequence:
  1. update Hibernate 5.4 to the final compatible 5.x bridge line;
  2. remove use of the obsolete `hibernate-entitymanager` artifact where supported;
  3. make queries and mappings pass all deprecation checks;
  4. transform `javax.persistence` imports/settings/XML namespaces to `jakarta.persistence`;
  5. move to a maintained Hibernate release selected for the Java baseline;
  6. resolve query, type, sequence, dialect, and schema behavior changes;
  7. compare generated schema and SQL against reviewed expectations.
- Use official migration guides for every skipped major/minor line.
- Do not perform namespace replacement without compiling and testing each module.
- Acceptance criteria:
  - every migration fixture opens and balances;
  - no automatic schema mutation occurs on ordinary open;
  - database queries have no deprecated API warnings targeted for removal.

### DATA-08: Upgrade HSQLDB independently

- Upgrade HSQLDB in a separate work item from H2 and Hibernate where possible.
- Test embedded and remote modes independently.
- Verify shutdown/flush semantics and companion-file handling.
- Decide whether HSQLDB remains a supported creation format or becomes migration-only.

### DATA-09: Rationalize supported storage formats

- After migration telemetry and user feedback, classify each format as:
  - preferred for new files;
  - supported read/write;
  - migration-only read;
  - removed after a documented date.
- Prefer one durable primary format for new files.
- Keep export formats independent of the primary implementation.
- Never remove a reader until supported releases can migrate the known installed base.

### DATA-10: Add post-migration verification and user report

- Verify counts, balances, references, attachments, and schema version.
- Present a concise migration report with:
  - source/target paths;
  - backup location;
  - source/target versions;
  - validation result;
  - warnings;
  - recovery command.
- Store the report beside the backup without credentials or transaction descriptions.

## 14. Phase 7: packaging and release engineering

### PKG-01: Produce platform-specific application images

- Use `jpackage` for Windows, macOS, and Linux.
- Bundle:
  - Java runtime;
  - JavaFX native modules;
  - application jars;
  - plugins shipped by the project;
  - licenses;
  - manual/documentation.
- Do not ship all operating-system classifiers in one archive.
- Decide whether to use a custom `jlink` runtime after measuring size and module complexity.
- Full JPMS modularization is optional for the first packaged release.

### PKG-02: Retire the Rust launcher

- Once packaged images launch reliably, remove runtime Java discovery and the Rust launcher from normal distributions.
- Before removal, preserve any launcher behavior still required as packaging configuration or Java startup logic.
- Specifically test:
  - paths with spaces/non-ASCII characters;
  - command-line argument preservation;
  - file associations;
  - second-instance behavior;
  - exit codes;
  - application data directories.
- Remove the checked-in prebuilt executable after the replacement ships.

### PKG-03: Add signing and notarization

- Windows: sign executables/installers with a protected code-signing identity.
- macOS: sign, harden runtime as required, notarize, and staple.
- Linux: publish checksums and package signatures; evaluate native package formats based on maintainer capacity.
- Keep signing credentials outside pull-request workflows.
- Use protected release environments and least-privilege tokens.

### PKG-04: Automate tagged releases

- A release workflow should:
  1. validate the tag/version relationship;
  2. run the full clean test matrix;
  3. build each platform artifact on its native runner;
  4. generate SBOMs;
  5. sign/notarize;
  6. generate SHA-256 checksum manifests;
  7. create provenance/attestations;
  8. upload artifacts to a draft release;
  9. require human approval before publication.
- Never rebuild an already published version from a different commit.

### PKG-05: Define application data locations

- Use standard per-user application directories on each OS.
- Separate:
  - user financial files;
  - preferences;
  - caches;
  - logs;
  - downloaded market data;
  - plugins;
  - backups.
- Document portable mode explicitly if retained.
- Never write dependencies into the installation directory at runtime.

## 15. Phase 8: architecture and maintainability

### ARCH-01: Introduce an explicit application context

- Create a small composition root at application startup.
- Own long-lived services such as:
  - engine/session service;
  - message/event service;
  - preferences service;
  - network client factory;
  - clock;
  - executor/scheduler;
  - migration service;
  - plugin registry.
- Pass narrow interfaces to controllers/services.
- Do not introduce a framework until constructor-based composition is proven insufficient.

### ARCH-02: Reduce direct `EngineFactory` access from JavaFX

- There are hundreds of references to `EngineFactory`, `MessageBus`, or Preferences across core/UI code.
- Migrate feature by feature:
  1. define a narrow use-case interface;
  2. implement it using the existing engine;
  3. inject it into the controller/view model;
  4. add headless tests;
  5. remove the direct global lookup.
- Prioritize transaction entry, file open/close, imports, security price updates, and reconciliation.

### ARCH-03: Replace reflective storage enum construction

- Replace `DataStoreType` holding implementation classes and invoking reflection with an explicit `DataStoreProvider` registry.
- Provider metadata should include:
  - stable identifier;
  - display name;
  - supported extensions/magic;
  - read/write/migration capability;
  - remote support;
  - provider version.
- Built-in providers may be registered directly.
- External providers may use `ServiceLoader` if the plugin security model permits.

### ARCH-04: Clarify module APIs

- Review each project dependency as `api`, `implementation`, `compileOnly`, or `runtimeOnly`.
- Avoid exposing Hibernate as a public API unless consumers truly require it.
- Add package/API compatibility checks for intended plugin APIs.
- Move test fixtures into a dedicated test-fixtures module when shared.
- Document allowed dependency direction.
- Prevent cycles through an architecture test or build rule.

### ARCH-05: Decide on JPMS explicitly

- Evaluate JPMS only after packaging and dependency upgrades are stable.
- If adopted:
  - add `module-info.java` one leaf module at a time;
  - minimize `opens` to JavaFX FXML and Hibernate needs;
  - test plugin loading and reflection;
  - use `jdeps` in CI.
- If deferred:
  - retain only useful `Automatic-Module-Name` declarations;
  - document why classpath packaging is intentional.

### ARCH-06: Modernize HTTP access

- Replace ad hoc `URLConnection`/`HttpURLConnection` code with a shared JDK `HttpClient` abstraction where practical.
- Centralize:
  - connect/request timeouts;
  - proxy/authentication;
  - redirects;
  - user agent;
  - TLS policy;
  - cancellation;
  - rate limiting/retry rules;
  - response-size limits.
- Inject the client into market/currency services.
- Use local mock HTTP servers in tests; do not depend on live providers.

### ARCH-07: Version the plugin contract

- Replace the hard-coded plugin manifest version convention with a documented API compatibility value.
- Define lifecycle, threading, error isolation, and resource ownership.
- Provide a small plugin SDK/test harness.
- Decide whether plugins can access internal engine objects or only stable service interfaces.
- Reject incompatible plugins with a clear message before activation.

### ARCH-08: Establish concurrency ownership

- Inventory manually created threads, executors, JavaFX tasks, server loops, and blocking event calls.
- Give every executor an owner and shutdown path.
- Use bounded pools/queues for untrusted or remote work.
- Add uncaught-exception handling.
- Avoid busy-wait loops; use synchronization primitives or asynchronous completion.
- Add shutdown tests that detect lingering non-daemon threads.

## 16. Phase 9: code quality and observability

### QUAL-01: Add formatting with a ratchet

- Choose a Java formatter and Kotlin DSL formatting strategy.
- Avoid a repository-wide formatting commit mixed with logic.
- Options:
  - one isolated mechanical formatting commit; or
  - format only changed files until coverage is complete.
- Enforce formatting in CI after the initial application.

### QUAL-02: Add static analysis gradually

- Evaluate SpotBugs, Error Prone, NullAway, Checkstyle, or PMD based on JavaFX/Gradle compatibility and maintenance status.
- Start with high-confidence correctness/security rules.
- Create a baseline for existing findings.
- New findings fail CI; baseline findings are burned down separately.
- Do not suppress findings without a local explanation.

### QUAL-03: Standardize nullability and exceptions

- Replace custom null annotations with a maintained annotation standard if tooling benefits justify it.
- Prefer explicit result/error types for expected open/import/migration failures.
- Do not return `null` after logging when the caller cannot distinguish failure causes.
- Preserve user-actionable context without exposing secrets.
- Define exception boundaries at UI, plugin, network, and persistence layers.

### QUAL-04: Adopt modern Java features selectively

- After Java 21 is established, consider:
  - records for immutable DTOs/results;
  - sealed interfaces for closed result hierarchies;
  - pattern matching for clearer dispatch;
  - switch expressions;
  - text blocks for fixtures/SQL;
  - try-with-resources and immutable collection factories.
- Do not mechanically convert persistent entities to records.
- Keep modernization behavior-neutral and covered by tests.

### QUAL-05: Improve diagnostics

- Add a diagnostics export that includes:
  - application version/revision;
  - Java/JavaFX versions;
  - OS/architecture;
  - enabled plugin identifiers/versions;
  - non-secret configuration;
  - recent sanitized logs.
- Exclude:
  - passwords/tokens;
  - full paths if privacy-sensitive;
  - account/transaction/payee/memo contents;
  - raw imported financial files.
- Use correlation IDs for migration and remote-session logs.

## 17. Phase 10: JavaFX UX and accessibility

### UI-01: Establish a UI compatibility checklist

- Verify each major view at standard and high-DPI scaling.
- Verify keyboard-only navigation.
- Verify focus order and visible focus.
- Verify screen-reader names/roles for primary controls.
- Verify light/dark or theme variants if supported.
- Verify long translations and right-to-left layout where applicable.
- Verify locale-specific dates, decimals, currencies, and negative values.

### UI-02: Separate view models from controls

- Start with controllers that have complex validation or direct engine access.
- Move business decisions and data loading to injected services/view models.
- Keep JavaFX thread updates explicit.
- Cancel background tasks when views close.
- Test view-model behavior without JavaFX startup.

### UI-03: Modernize long-running task feedback

- Provide progress and cancellation for:
  - file opening;
  - backup/migration;
  - imports;
  - report generation;
  - price/currency updates.
- Never leave files half-migrated on cancellation.
- Show recovery paths for failures.
- Avoid modal stack-trace dialogs; offer a concise message plus sanitized details.

### UI-04: Improve migration UX

- Clearly distinguish open, backup, migrate, verify, and restore.
- Show the source file will remain untouched.
- Require explicit destination selection if migration produces a new file.
- Show backup location and verification status before continuing.
- Never imply success until post-migration invariants pass.

## 18. Phase 11: documentation and contributor experience

### DOC-01: Make one README the source of truth

- Choose Markdown or AsciiDoc as the maintained source.
- Generate alternate HTML/PDF forms in the build if they are still needed.
- Remove contradictory hand-maintained copies.
- Update:
  - Java requirement;
  - supported OS/architectures;
  - build commands;
  - package installation;
  - migration/backup behavior;
  - current CI badges;
  - support links.

### DOC-02: Add contributor documentation

- Add `CONTRIBUTING.md` covering:
  - prerequisites;
  - build/test commands;
  - module map;
  - style/formatting;
  - test suite selection;
  - fixture privacy rules;
  - dependency update rules;
  - PR checklist.
- Add commands that work on a clean checkout.

### DOC-03: Document architecture

- Add `docs/architecture.md` with:
  - module dependency diagram;
  - application startup/composition;
  - engine and storage abstractions;
  - message flow;
  - plugin loading;
  - network services;
  - backup and migration flow;
  - security boundaries.
- Link relevant ADRs.

### DOC-04: Add release and recovery runbooks

- Release runbook:
  - version/tagging;
  - full test matrix;
  - signing/notarization;
  - artifact/SBOM/checksum verification;
  - publication;
  - rollback/yank procedure.
- Data recovery runbook:
  - locate backups;
  - verify manifest/checksums;
  - restore to a new path;
  - open with compatible version;
  - collect sanitized diagnostics.

### DOC-05: Refresh the user manual

- Replace obsolete Java installation and AppleScript instructions.
- Add platform installer instructions.
- Add backup/migration screenshots after UI stabilizes.
- Document remote-mode security and support status accurately.
- Regenerate the checked-in PDF only through a documented reproducible process, or publish it as a release artifact instead.

## 19. Recommended issue backlog

The table below is suitable for initial issue creation. Sizes are relative and assume one experienced maintainer.

| ID | Work item | Size | Risk | Depends on |
|---|---|---:|---:|---|
| BASE-01 | Support policy | S | Low | None |
| BASE-02 | Initial ADR set | M | Low | BASE-01 |
| BASE-03 | Legacy build baseline | M | Medium | None |
| BASE-04 | Data fixture catalog | XL | High | BASE-01 |
| BASE-05 | Modernization PR template/rules | S | Low | None |
| BUILD-01 | Gradle 7 then 8 bridge | L | High | BASE-03 |
| BUILD-02 | Java 21 toolchain and release target | M | Medium | BUILD-01 |
| BUILD-03 | Convention plugins/build logic | L | Medium | BUILD-01 |
| BUILD-04 | Version catalog | M | Low | BUILD-01 |
| BUILD-05 | Repository cleanup | S | Medium | BUILD-01 |
| BUILD-06 | Locking and dependency verification | M | Medium | BUILD-04, BUILD-05 |
| BUILD-07 | Reproducible outputs | M | Medium | BUILD-03 |
| BUILD-08 | Compiler/API checks | M | Medium | BUILD-02 |
| CI-01 | Cross-platform matrix CI | M | Medium | BUILD-01, BUILD-02 |
| CI-02 | Wrapper validation | S | Low | BUILD-01 |
| CI-03 | Test suite separation | L | Medium | BUILD-03 |
| CI-04 | Dependency automation | S | Medium | BUILD-04, CI-01 |
| CI-05 | Security/dependency analysis | M | Medium | CI-01 |
| CI-06 | Coverage reporting | M | Low | BUILD-03 |
| TEST-01 | Inject controllable time | L | Medium | BUILD-02 |
| TEST-02 | Remove sleep/random flakes | M | Medium | BUILD-01 |
| TEST-03 | Engine contract modernization | L | High | BASE-04, CI-03 |
| TEST-04 | Accounting property tests | L | High | TEST-03 |
| TEST-05 | Migration golden suite | XL | Critical | BASE-04, DATA-01 |
| TEST-06 | JavaFX test pyramid | XL | Medium | DEP-02, DEP-08 |
| TEST-07 | Plugin compatibility tests | L | Medium | ARCH-07 |
| SEC-01 | Remove runtime JavaFX download | L | Critical | PKG-01 |
| SEC-02 | Remove credential leakage | M | Critical | BUILD-01 |
| SEC-03 | Versioned authenticated encryption | XL | Critical | BASE-04, SEC-04 decision |
| SEC-04 | Secure/restrict remote mode | XL | Critical | BASE-02 |
| SEC-05 | Proxy credential storage | M | High | ARCH-01 |
| SEC-06 | Deserialization/plugin hardening | L | Critical | DEP-06, ARCH-07 |
| SEC-07 | Security policy | S | Low | BASE-01 |
| DEP-02 | Test dependency upgrades | M | Medium | BUILD-04 |
| DEP-03 | Utility dependency upgrades | M | Medium | TEST-03 |
| DEP-04 | Logging normalization | M | High | SEC-02 |
| DEP-05 | POI and PDFBox upgrades | L | High | TEST-03 |
| DEP-06 | XStream upgrade | L | Critical | BASE-04, TEST-05 |
| DEP-07 | Stable Netty upgrade | M | High | SEC-04 |
| DEP-08 | JavaFX 21 upgrade | XL | High | BUILD-02, PKG-01, DEP-02 |
| DATA-01 | Canonical interchange format | XL | Critical | BASE-04 |
| DATA-02 | Read-only migration preflight | L | Critical | DATA-01 |
| DATA-03 | Verified backup mechanism | L | Critical | DATA-02 |
| DATA-04 | Migration CLI | XL | Critical | DATA-01, DATA-03 |
| DATA-05 | Isolated legacy H2 reader | XL | Critical | DATA-04 |
| DATA-06 | Explicit schema migrations | XL | Critical | TEST-05 |
| DATA-07 | Hibernate/Jakarta migration | XL | Critical | DATA-06 |
| DATA-08 | HSQLDB upgrade | L | High | TEST-05, DATA-06 |
| DATA-09 | Storage format policy | M | High | DATA-05, DATA-08 |
| DATA-10 | Migration verification/report | L | Critical | DATA-04, TEST-05 |
| PKG-01 | Platform-specific jpackage images | XL | High | BUILD-02, BUILD-03 |
| PKG-02 | Retire Rust launcher | M | Medium | PKG-01, SEC-01 |
| PKG-03 | Signing/notarization | XL | High | PKG-01 |
| PKG-04 | Automated release workflow | L | High | PKG-03, CI-01 |
| PKG-05 | Standard application directories | M | Medium | PKG-01 |
| ARCH-01 | Composition root/application context | L | High | BUILD-02 |
| ARCH-02 | Reduce global engine access | XL | High | ARCH-01 |
| ARCH-03 | DataStore provider registry | L | High | TEST-03 |
| ARCH-04 | Module API cleanup | L | Medium | BUILD-03 |
| ARCH-05 | JPMS decision/implementation | XL | High | PKG-01, ARCH-04 |
| ARCH-06 | Shared modern HTTP client | L | Medium | ARCH-01 |
| ARCH-07 | Versioned plugin contract | XL | High | ARCH-01, TEST-07 |
| ARCH-08 | Concurrency ownership | XL | High | TEST-02, ARCH-01 |
| QUAL-01 | Formatting ratchet | M | Low | BUILD-03 |
| QUAL-02 | Static-analysis ratchet | L | Medium | BUILD-08 |
| QUAL-03 | Null/error strategy | XL | High | ARCH-01 |
| QUAL-04 | Selective Java 21 refactors | XL | Medium | BUILD-02 |
| QUAL-05 | Sanitized diagnostics | L | High | DEP-04, SEC-02 |
| UI-01 | UI/accessibility checklist | M | Low | DEP-08 |
| UI-02 | View-model extraction | XL | High | ARCH-01, TEST-06 |
| UI-03 | Long-running task UX | L | Medium | UI-02 |
| UI-04 | Migration UX | L | Critical | DATA-10, UI-02 |
| DOC-01 | README consolidation | M | Low | BUILD-02, PKG-01 |
| DOC-02 | Contributor guide | M | Low | CI-03 |
| DOC-03 | Architecture guide | L | Medium | ARCH-01 |
| DOC-04 | Release/recovery runbooks | M | High | PKG-04, DATA-10 |
| DOC-05 | Manual refresh | XL | Medium | UI-04, PKG-01 |

## 20. Recommended first pull requests

Use this order to create momentum without risking user data:

1. Documentation-only: add support policy, ADR template, PR checklist, and `SECURITY.md`.
2. Baseline-only: capture legacy Java 11 test/dependency results.
3. Build-only: update Gradle to the latest 7.6 patch and eliminate Gradle 7 deprecations.
4. Build-only: update Gradle to the selected 8.14 patch.
5. Build-only: add Java 21 toolchains and explicit `--release 21`.
6. Build-only: remove JCenter/Maven Local defaults and introduce a version catalog.
7. CI-only: add pull-request matrix CI and modern wrapper validation.
8. Test-only: remove obvious `Thread.sleep` and wall-clock flakes.
9. Security-only: remove password-bearing URL logs and stop mutating `System.getProperties()`.
10. Packaging spike: create unsigned platform-specific `jpackage` images without changing the published distribution.
11. Test-data work: add the first synthetic fixtures and expected summaries.
12. Security/package change: make packaged JavaFX the default and remove first-run downloads.

Do not begin the Hibernate/H2 major migration until at least items 1-11 are complete and green.

## 21. Release sequence

### Release A: build-recovery preview

- Modern Gradle bridge.
- Java 21 toolchain.
- Current CI.
- No file-format changes.
- Intended for contributors, not broad end-user migration.

### Release B: packaged-runtime preview

- JavaFX 21.
- Platform-specific application images.
- No runtime downloads.
- Existing persistence versions unchanged.
- Remote mode explicitly retains its existing support warning or is temporarily loopback-only.

### Release C: migration preview

- Standalone migration CLI.
- Verified backup and restore.
- Read-only legacy inspection.
- New preferred storage format available but not silently selected for existing files.

### Release D: persistence release candidate

- Jakarta Persistence and maintained ORM/database engine.
- Explicit schema migrations.
- Complete golden fixture validation.
- Old formats remain migration-readable according to policy.

### Release E: modernized stable release

- Signed/notarized artifacts.
- SBOM/checksums/provenance.
- Current documentation and recovery runbook.
- Security review completed for remote mode, or remote mode remains disabled/unsupported.
- Upgrade telemetry/feedback window completed without unresolved data-loss issues.

## 22. Definition of done

### 22.1 Build and dependency definition of done

- Clean checkout builds with the documented command.
- Java toolchain and bytecode target are explicit.
- No Gradle deprecation warnings on the selected target.
- No JCenter or implicit Maven Local resolution.
- Dependency locks and verification metadata are current.
- No unexplained pre-release dependencies.
- Reproducibility check passes for unsigned artifacts.

### 22.2 Test definition of done

- Unit, integration, migration, and UI suites are separately runnable.
- Required suites pass on the documented OS matrix.
- Test discovery count is monitored.
- Migration fixtures cover every supported legacy format.
- No known flaky test is silently retried indefinitely.
- Critical accounting, migration, backup, and security paths have meaningful coverage.

### 22.3 Data migration definition of done

- Source file is never modified during preflight.
- Verified backup is created before migration.
- Migration is written to a distinct destination unless the user explicitly completes a later replacement step.
- Post-migration account/transaction/balance/reference checks pass.
- Restore procedure is tested.
- Failure and cancellation preserve the source and backup.
- User receives a non-secret migration report.

### 22.4 Security definition of done

- No runtime executable dependency downloads.
- No password/token in URLs, logs, global properties, reports, or normal preferences.
- Legacy unauthenticated encryption is not used for new data/messages.
- Remote mode has a reviewed threat model and secure disposition.
- Deserialization inputs are allowlisted and bounded.
- Dependency/static security checks run in CI.
- `SECURITY.md` is published.

### 22.5 Packaging/release definition of done

- Native application image works offline on every supported target.
- Runtime and JavaFX are included.
- Command-line arguments and file paths are preserved exactly.
- Install/uninstall behavior is tested.
- Artifacts are signed/notarized as applicable.
- Checksums, SBOM, and provenance are published.
- Release and rollback runbooks have been executed in a rehearsal.

## 23. Pull-request verification template

Copy this checklist into modernization pull requests:

```markdown
## Scope

- Work item ID:
- Primary change category:
- Why this change is isolated:

## Compatibility

- [ ] No data-format change
- [ ] Data-format change described below
- [ ] No plugin API change
- [ ] Plugin API change described below
- [ ] Supported OS/JDK impact described

## Data safety

- [ ] Not applicable
- [ ] Legacy fixtures tested
- [ ] Backup behavior tested
- [ ] Migration rollback tested
- [ ] Source file remains unchanged on failure

## Security

- [ ] No new network/file/deserialization surface
- [ ] Threat/security impact reviewed
- [ ] Logs checked for secrets and financial data
- [ ] Dependency/security reports reviewed

## Verification

- [ ] `./gradlew test`
- [ ] `./gradlew integrationTest`
- [ ] `./gradlew migrationTest`
- [ ] `./gradlew uiTest`
- [ ] `./gradlew check --warning-mode fail`
- [ ] Platform/package checks listed below

## Rollback

- Revert procedure:
- Data recovery procedure, if applicable:

## Documentation

- [ ] README/support policy updated
- [ ] ADR added/updated
- [ ] User manual/release note updated
```

## 24. Migration implementation checklist

Use this checklist for every file/schema migration:

- [ ] Format/version detection occurs read-only.
- [ ] Password validation does not modify the source.
- [ ] All companion files are identified.
- [ ] Required free disk space is checked.
- [ ] Backup destination is writable.
- [ ] Source engine is closed/flushed before backup.
- [ ] Backup is stored under a new unique name.
- [ ] Source and backup SHA-256 checksums match.
- [ ] Backup manifest is written.
- [ ] Backup can be reopened or structurally validated.
- [ ] Migration writes to a new destination.
- [ ] Schema migrations are explicit and logged by version.
- [ ] Account hierarchy matches expected summary.
- [ ] Transaction and entry counts match.
- [ ] Currency totals and accounting invariants match.
- [ ] Investment history/events match.
- [ ] Budgets/reminders/tags match.
- [ ] Attachments and hashes match.
- [ ] Export/reopen round trip succeeds.
- [ ] Failure/cancellation leaves source and backup intact.
- [ ] Recovery instructions are displayed and tested.

## 25. Dependency upgrade checklist

- [ ] Read official release and migration notes.
- [ ] Select a stable maintained release, not simply the highest version.
- [ ] Record source/runtime Java requirements.
- [ ] Record transitive dependency changes.
- [ ] Compare dependency trees before/after.
- [ ] Update locks and verification metadata intentionally.
- [ ] Run relevant focused tests.
- [ ] Run full CI matrix.
- [ ] Run migration fixtures for serialization/database changes.
- [ ] Run rendering fixtures for POI/PDFBox/JavaFX changes.
- [ ] Run protocol tests for Netty/security changes.
- [ ] Review vulnerability scan changes.
- [ ] Document rollback version.
- [ ] Update SBOM/license notices if required.

## 26. Suggested local validation commands

These are target commands. Add them as the corresponding suites are implemented.

```bash
./gradlew clean check --warning-mode fail
./gradlew test
./gradlew integrationTest
./gradlew migrationTest
./gradlew uiTest
./gradlew dependencyUpdates
./gradlew dependencies
./gradlew dependencyInsight --dependency <name> --configuration runtimeClasspath
./gradlew jpackage
./gradlew cyclonedxBom
cargo test --manifest-path rust-launcher/Cargo.toml
cargo clippy --manifest-path rust-launcher/Cargo.toml --all-targets -- -D warnings
```

Until the Rust launcher is retired, also run `cargo fmt --check` and a Windows launcher integration test.

## 27. Risk register

| Risk | Likelihood | Impact | Mitigation |
|---|---:|---:|---|
| Legacy H2 files cannot be opened by modern H2 | High | Critical | Isolated legacy reader, canonical export/import, fixture suite |
| ORM upgrade changes schema or query semantics | High | Critical | Explicit migrations, schema diff, staged Hibernate upgrade |
| XStream upgrade breaks old XML/binary data | Medium | Critical | Type allowlist plus golden round-trip fixtures |
| Custom encryption migration locks out users | Medium | Critical | Versioned envelope, legacy read path, independent review |
| Remote mode exposes credentials/data | High | Critical | Loopback/disable until TLS and authenticated protocol ship |
| JavaFX upgrade causes platform-native regressions | Medium | High | Native runner matrix, packaged smoke tests, UI checklist |
| Build upgrade hides lost tests | Medium | High | Track test discovery counts and reports before/after |
| Broad formatting obscures behavior changes | High | Medium | Isolated mechanical commit or changed-file ratchet |
| Mutable GitHub Actions dependency is compromised | Medium | High | Full SHA pinning, Dependabot/Renovate, minimal permissions |
| Reproducibility is broken by timestamps/signing | High | Medium | Reproducible unsigned artifacts, signing as final envelope |
| Plugin compatibility breaks silently | Medium | High | Versioned API, compatibility test plugins, clear rejection |
| Maintainer capacity is exhausted by parallel migrations | High | High | Sequential milestones, small PRs, explicit non-goals |
| Documentation diverges from behavior | Medium | Medium | One source of truth, release checklist validation |

## 28. Progress metrics

Track metrics per milestone, not as individual performance targets:

- clean-build success rate by platform;
- median CI duration and flaky-test rate;
- number of Gradle/compiler deprecation warnings;
- test count by suite and module;
- changed-line coverage in critical packages;
- number of dependencies on unsupported/pre-release lines;
- number and age of vulnerability exceptions;
- percentage of legacy migration fixtures passing;
- reproducible artifact hash success;
- packaged startup success by OS/architecture;
- number of direct UI references to global engine/message/preferences state;
- number of stored/logged credential paths remaining;
- unresolved critical/high modernization risks.

## 29. Open decisions requiring maintainer approval

- Is Java 21 the desired first baseline, or must Java 17 remain supported?
- Should Java 25 be a tested compatibility lane immediately or only after the first stable modernization release?
- Which existing data formats must remain read/write versus migration-only?
- Is compressed XML acceptable as the canonical migration interchange format?
- Should remote multi-user mode be temporarily disabled, loopback-only, or fully redesigned now?
- Which platforms and architectures will receive signed installers?
- Is the current external plugin ecosystem active enough to require binary compatibility?
- Is JPMS valuable enough to justify the reflection/plugin complexity?
- Should the manual PDF remain version-controlled or become a generated release artifact?
- What is the support window for the legacy-reader/migration utility?
- Which organization/account will own signing keys, vulnerability reports, and release approvals?

No issue that depends on one of these decisions should silently choose an answer. Record the decision in an ADR.

## 30. Reference guidance

Use primary documentation during implementation:

- Gradle Java compatibility matrix: <https://docs.gradle.org/current/userguide/compatibility.html>
- Gradle Java toolchains: <https://docs.gradle.org/current/userguide/toolchains.html>
- Gradle dependency verification: <https://docs.gradle.org/current/userguide/dependency_verification.html>
- Gradle dependency locking: <https://docs.gradle.org/current/userguide/dependency_locking.html>
- Hibernate migration guides: <https://hibernate.org/orm/documentation/migrate/>
- Hibernate ORM 6 migration guide and Jakarta transition: <https://docs.hibernate.org/orm/6.0/migration-guide/>
- OpenJFX documentation: <https://openjfx.io/>
- JDK `jpackage` guide: <https://docs.oracle.com/en/java/javase/21/jpackage/>
- GitHub Actions secure use: <https://docs.github.com/en/actions/reference/security/secure-use>
- OWASP Password Storage Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html>
- OWASP Cryptographic Storage Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html>

Recheck current maintained versions and support status at the time each work item begins. Do not treat version numbers in this plan as permanent.
