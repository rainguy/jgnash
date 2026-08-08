# Architecture decision records

Architecture decision records (ADRs) capture decisions that materially affect compatibility, security, data formats, architecture, build tooling, or maintenance. They explain why a decision was made so later changes do not have to reconstruct context from commits.

## Status values

- **Proposed**: under review and not yet authoritative.
- **Accepted**: current project direction.
- **Superseded**: replaced by a newer ADR; retain the original record and link both directions.
- **Deprecated**: retained for historical context but no longer recommended.
- **Rejected**: considered and intentionally not selected.

## Process

1. Copy [`template.md`](template.md) to the next zero-padded number and a short kebab-case title.
2. Fill in context, decision, alternatives, consequences, and revisit conditions.
3. Use **Proposed** while discussion or implementation prerequisites remain unresolved.
4. Merge the ADR with or before the change that depends on it.
5. Never rewrite a historical decision to conceal changed assumptions; supersede it with a new ADR.
6. Link the relevant work item from [`MODERNIZATION_PLAN.md`](../../MODERNIZATION_PLAN.md).

## Index

| ADR | Status | Decision |
|---|---|---|
| [0001](0001-java-21-baseline.md) | Accepted | Use Java 21 as the first modernization baseline |
| [0002](0002-gradle-upgrade-path.md) | Accepted | Upgrade Gradle through 7.6 to 8.14 before evaluating Gradle 9 |
| [0003](0003-platform-packaging.md) | Accepted | Replace runtime dependency downloads with native `jpackage` images |
| [0004](0004-persistence-migration.md) | Accepted | Protect legacy formats with read-only preflight, verified backup, and explicit migration |
| [0005](0005-remote-security.md) | Accepted | Treat legacy remote mode as unsupported on untrusted networks until every channel has a reviewed disposition |
| [0006](0006-defer-jpms.md) | Accepted | Defer full JPMS modularization until packaging and dependency migration stabilize |
| [0007](0007-plugin-compatibility.md) | Accepted | Preserve exact-match legacy plugin compatibility while designing a versioned contract |
