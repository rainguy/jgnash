# 0003: Ship platform-specific application images

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `PKG-01`, `SEC-01`, `PKG-02`
- Supersedes: none
- Superseded by: none

## Context

The current distribution assembles cross-platform JavaFX jars manually, includes a prebuilt Rust Windows launcher, discovers a system Java runtime, and may download platform JavaFX artifacts on first launch. Downloaded files are checked using an MD5 file from the same repository location, and users must restart afterward. This creates reliability, reproducibility, offline-startup, and supply-chain concerns.

## Decision

- Build separate Windows, macOS, and Linux application images using the JDK `jpackage` tool.
- Bundle the selected Java runtime, JavaFX native modules, application jars, shipped plugins, licenses, and required documentation.
- Resolve and verify all executable dependencies at build time.
- Do not download JavaFX or other executable libraries on application startup.
- Preserve a transitional platform-specific zip only if needed; do not publish a single archive containing all native classifiers.
- Retire the Rust launcher after packaged startup and argument handling are proven equivalent.
- Treat signing and notarization as a release envelope after reproducible unsigned artifacts are built.

## Alternatives considered

### Keep runtime downloads but replace MD5 with SHA-256

This improves corruption detection but retains first-run failure modes, installation-directory writes, and an unnecessary runtime supply-chain path.

### Keep requiring a system JDK

This reduces artifact size but transfers runtime compatibility and JavaFX installation complexity to users.

### Create one cross-platform archive

JavaFX includes platform-native components. A universal archive is larger and encourages the current classifier filtering and runtime detection complexity.

## Consequences

### Positive

- Offline first startup becomes testable.
- Runtime and JavaFX compatibility are controlled by the release.
- Native launchers, file associations, icons, and install paths use supported JDK tooling.

### Negative

- CI must build on native platform runners.
- Release artifacts become larger and platform-specific.
- Signing/notarization requires protected identities and release environments.

### Risks and mitigations

- Risk: automatic module and reflection behavior complicates a trimmed runtime.
  - Mitigation: permit a complete bundled runtime first; optimize with `jlink` later.
- Risk: launcher behavior changes.
  - Mitigation: test non-ASCII paths, spaces, arguments, exit codes, and file associations before removing Rust.

## Verification

- Clean supported machine starts the application offline without a system JDK.
- Package contents match dependency verification, SBOM, and checksum records.
- Core create/open/save/backup flows pass on every supported platform.
- No runtime code path downloads executable jars.

## Revisit conditions

- A maintained cross-platform Java packaging standard supersedes `jpackage`.
- Artifact size becomes a measured user problem after stable release.
