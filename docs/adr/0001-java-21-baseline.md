# 0001: Use Java 21 as the first modernization baseline

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `BUILD-02`
- Supersedes: none
- Superseded by: none

## Context

The current project documents Java 11, uses JavaFX 15, and pins Gradle 6.8.2 without a Java toolchain or compiler `--release` setting. A local verification run showed that Gradle 6.8.2 fails during Java compilation when run on Java 21 because its compiler integration attempts module-internal access rejected by that JDK.

The project needs a maintained baseline that supports modern build tooling, packaging, dependency releases, and language/runtime improvements without adding the churn of the newest short-lived feature release.

## Decision

- Java 21 is the first modernized source, bytecode, test, and packaged-runtime baseline.
- Gradle will use a Java 21 toolchain and `JavaCompile.options.release = 21`.
- The application will not depend on preview features.
- The legacy Java 11 environment is retained only long enough to capture the pre-modernization baseline and operate isolated migration tooling if required.
- A later Java baseline change requires a new ADR and the full platform, migration, packaging, and plugin compatibility gates.

## Alternatives considered

### Remain on Java 11

This minimizes immediate language change but retains an aging baseline, constrains maintained dependency choices, and does not address modern packaging/tooling goals.

### Use Java 17

Java 17 is a viable intermediate runtime, but Java 21 provides a longer modernization runway and is already sufficient for the intended build and JavaFX bridge targets. Supporting both would double compatibility work during the riskiest migration period.

### Move directly to Java 25

This may become desirable after the first stable modernization release, but it unnecessarily couples the initial recovery to newer tool and native-library requirements.

## Consequences

### Positive

- Build and runtime behavior become explicit and reproducible.
- Maintained Gradle, JavaFX, ORM, and testing releases become available.
- The project can use stable Java 21 language features selectively.

### Negative

- Users who run directly from jars need Java 21 unless they use the planned bundled runtime.
- Plugins must be tested against Java 21 and the matching jGnash release.
- Some legacy libraries may require staged upgrades before compilation succeeds.

### Risks and mitigations

- Risk: a dependency or plugin is incompatible with Java 21.
  - Mitigation: upgrade in isolated work items and preserve an explicit legacy baseline.
- Risk: accidental use of APIs newer than the target.
  - Mitigation: enforce toolchains and `--release 21` in convention build logic.

## Verification

- Clean build and all test suites run with the Java 21 toolchain.
- Produced class files target Java 21.
- Native packaged applications run without a system JDK.
- Migration fixtures pass under the packaged runtime.

## Revisit conditions

- The first modernized stable release has shipped and its migration window is complete.
- A required maintained dependency raises its Java minimum.
- Java 21 leaves the project's chosen security-support window.
