# 0002: Upgrade Gradle through controlled bridge releases

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `BUILD-01`
- Supersedes: none
- Superseded by: none

## Context

The repository pins Gradle 6.8.2. It fails before tests on Java 21 and reports deprecated behavior incompatible with Gradle 7. The build also uses old plugins, Gradle internal classes, and deprecated application/packaging APIs. A direct jump to the newest Gradle major would combine too many failure sources.

## Decision

- Upgrade from Gradle 6.8.2 to the latest 7.6 patch while running the bridge on Java 11.
- Resolve every actionable Gradle 7 deprecation before the next hop.
- Upgrade to the selected latest Gradle 8.14 patch.
- Make the build pass with `--warning-mode fail` on the Gradle 8 bridge.
- Evaluate the current Gradle 9 release only after obsolete packaging plugins and scripts are replaced.
- Regenerate the wrapper with Gradle's wrapper task and verify its checksum; never replace the wrapper jar from an unreviewed source.

## Alternatives considered

### Upgrade directly to Gradle 9

This reaches the newest tool faster but obscures which major removed each legacy API and increases plugin compatibility risk.

### Keep Gradle 6.8.2 and add JVM export flags

Module export flags would preserve an unsupported build and hide rather than remove the coupling to compiler internals.

### Replace Gradle with another build system

This would be a broad rewrite with little user benefit and would discard the project's existing multi-module knowledge.

## Consequences

### Positive

- Failures can be attributed to one compatibility boundary at a time.
- Build deprecations are removed before they become hard errors.
- Java 21 toolchains become available on the intended bridge.

### Negative

- The wrapper changes more than once.
- Temporary compatibility edits may be replaced again in the next hop.

### Risks and mitigations

- Risk: obsolete JavaFX/macOS plugins block a bridge release.
  - Mitigation: isolate or replace the plugin before proceeding; do not weaken wrapper verification.
- Risk: test discovery changes during build migration.
  - Mitigation: record test counts and reports before and after every hop.

## Verification

- `./gradlew help --warning-mode all` at each bridge.
- `./gradlew clean test` on the legacy baseline where possible.
- `./gradlew check --warning-mode fail` on the Gradle 8 target.
- Wrapper validation on every wrapper change.

## Revisit conditions

- Gradle 8 leaves support or a required plugin needs Gradle 9.
- Packaging plugins are removed and the full suite is green on Java 21.
