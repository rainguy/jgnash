# 0005: Treat legacy remote mode as provisional until all channels are secured

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `SEC-02` through `SEC-05`, `DEP-07`
- Supersedes: none
- Superseded by: none

## Context

Remote mode spans database transport, a message bus, distributed locks, attachment transfer, authentication, and remote shutdown. The current implementation includes plaintext database TCP paths, password material in JDBC URLs/properties, and custom password-derived AES behavior. Securing only one companion service would not protect the complete session.

## Decision

- The current remote mode is not security-supported across untrusted networks.
- Until replacement work is complete, documentation must require independent network protection and strict firewalling or recommend avoiding remote mode.
- Remote functionality is feature-frozen except for tests, security remediation, migration, and observability needed for the redesign.
- `SEC-04` must produce a threat model and choose one release disposition:
  - every channel uses reviewed authenticated transport, explicit authorization, limits, and downgrade protection; or
  - remote mode is disabled/unsupported in the modernized stable release.
- No application-level password-derived cipher is accepted as a substitute for authenticated transport.
- Credential removal from URLs, logs, and global properties proceeds before the larger protocol decision.

## Alternatives considered

### Preserve the current behavior without a warning

This would imply a security guarantee the implementation has not established.

### Add TLS only to the database port

Message, lock, attachment, and shutdown channels would remain part of the attack surface, producing a misleading partial solution.

### Immediately remove all remote code

This might strand legitimate users before usage, migration, and alternatives are understood. Temporary restriction plus a time-bounded decision preserves evidence gathering.

## Consequences

### Positive

- Documentation matches the current security evidence.
- The redesign considers the whole protocol rather than individual sockets.
- Credential leakage can be fixed independently and early.

### Negative

- Existing remote users receive a support limitation.
- Full remediation is a large workstream requiring independent review.

### Risks and mitigations

- Risk: users mistake password protection for secure transport.
  - Mitigation: prominent support/security warnings and secure-by-default release behavior.
- Risk: the feature remains provisional indefinitely.
  - Mitigation: require a final disposition before the modernized stable release gate.

## Verification

- Threat model covers every remote service and privileged operation.
- Canary secrets never appear in logs, URLs, reports, or process-global properties.
- Integration tests cover wrong credentials, invalid certificates if applicable, replay, downgrade, truncation, oversized inputs, and remote shutdown authorization.
- Release configuration is secure by default or disables the feature.

## Revisit conditions

- The threat model and implementation spike provide enough evidence to choose secure remote support or removal.
- A maintained standard protocol can replace the custom service set.
