# 0006: Defer full JPMS modularization

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `ARCH-04`, `ARCH-05`, `PKG-01`
- Supersedes: none
- Superseded by: none

## Context

The project is split into multiple Gradle subprojects and several jars declare `Automatic-Module-Name`, but there are no `module-info.java` descriptors. JavaFX FXML, Hibernate, XStream, plugin classloading, and other reflection-heavy behavior require deliberate `opens` and module boundaries. Packaging does not require completing JPMS if a complete or appropriately assembled runtime is bundled.

## Decision

- Do not make full JPMS modularization a prerequisite for Java 21, JavaFX 21, persistence migration, or the first `jpackage` release.
- Preserve clear Gradle project boundaries and useful automatic module names.
- Package a classpath application with a complete bundled runtime first if necessary.
- Re-evaluate JPMS after dependency upgrades, module API cleanup, plugin contract work, and platform packaging are stable.
- If adopted later, add descriptors one leaf module at a time and minimize reflective `opens`.

## Alternatives considered

### Modularize before any other upgrade

This could improve encapsulation but would combine reflection, plugin, JavaFX, Hibernate, and dependency problems with build recovery.

### Reject JPMS permanently

The project may later benefit from stronger boundaries and smaller runtime images; there is insufficient evidence to make a permanent rejection.

## Consequences

### Positive

- Packaging and security work are not blocked on broad module descriptors.
- Reflection behavior can be characterized before access is restricted.
- Module APIs can be cleaned based on actual dependencies.

### Negative

- The first bundled runtime may be larger.
- Compile-time encapsulation remains limited.
- Automatic module names may provide only partial value.

### Risks and mitigations

- Risk: deferral becomes accidental permanent ambiguity.
  - Mitigation: retain `ARCH-05` and explicit revisit gates in the plan.
- Risk: a dependency becomes modular-only.
  - Mitigation: evaluate its impact in the dependency upgrade work item.

## Verification

- Platform packages start and pass smoke tests without module-path assumptions.
- `jdeps` output is captured before the later JPMS decision.
- Project dependency direction is documented and architecture-tested independently of JPMS.

## Revisit conditions

- `PKG-01`, `ARCH-04`, and `ARCH-07` are complete.
- Runtime size or encapsulation becomes a measured requirement.
