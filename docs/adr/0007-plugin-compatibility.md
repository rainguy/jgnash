# 0007: Preserve exact-match legacy plugins while designing a versioned contract

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `TEST-07`, `ARCH-07`, `SEC-06`
- Supersedes: none
- Superseded by: none

## Context

The current plugin loader reads `Plugin-Activator` and `Plugin-Version` from jar manifests and loads a plugin only when its decimal version exactly equals interface version `2.25`. Plugins share application classes through a custom classloader. Lifecycle, API compatibility, isolation, permissions, and failure behavior are only lightly specified.

## Decision

- Preserve exact version `2.25` compatibility for existing legacy plugins during the early build and packaging milestones.
- Require plugins to be tested against the exact jGnash release until a new contract ships.
- Do not expose additional internal packages as a shortcut for modernization.
- Build a plugin compatibility harness covering valid, incompatible, malformed, duplicate, and failing plugins.
- Design the replacement contract with:
  - a stable API identifier and semantic compatibility rules;
  - explicit lifecycle and threading requirements;
  - stable service interfaces rather than unrestricted internal engine access;
  - actionable rejection before activation;
  - controlled resource/classloader cleanup;
  - documented security boundaries.
- Any new contract or compatibility range requires a superseding ADR.

## Alternatives considered

### Accept all plugin versions during modernization

This hides incompatibility and risks loading code against changed internals.

### Remove plugin support immediately

This discards an existing extension mechanism before usage and migration needs are measured.

### Freeze all internal classes for binary compatibility

The current boundary is too broad and implicit; freezing it would prevent necessary architecture and security work.

## Consequences

### Positive

- Existing compatibility behavior remains predictable.
- Modernization can narrow and document the intended extension surface.
- Packaging tests can detect plugin loading regressions.

### Negative

- Plugins may need release-specific builds during the transition.
- Exact decimal matching remains until the replacement contract is ready.
- Classloader and security hardening are deferred to dedicated work.

### Risks and mitigations

- Risk: an active external ecosystem depends on undocumented internals.
  - Mitigation: solicit compatibility reports and provide a test harness before stable API removal.
- Risk: malicious plugins are assumed to be sandboxed.
  - Mitigation: document that locally installed plugins are trusted code unless a real isolation model is implemented.

## Verification

- Legacy test plugin `2.25` loads from packaged images.
- Incorrect/missing versions and activators are rejected without partial activation.
- Plugin exceptions do not prevent application shutdown or corrupt the plugin registry.
- Compatibility behavior is documented in the support policy.

## Revisit conditions

- The compatibility test harness and application composition root exist.
- Maintainers have evidence about external plugin usage and required API surface.
