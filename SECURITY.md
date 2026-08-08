# Security policy

## Supported versions

Security fixes are developed on the current `master` branch. Older releases receive migration assistance rather than an open-ended promise of patches. See [`docs/support-policy.md`](docs/support-policy.md) for the complete lifecycle and the current limitations of remote multi-user mode.

## Reporting a vulnerability

Do not open a public issue for a suspected vulnerability involving financial data, credentials, remote access, deserialization, plugins, attachments, or code execution.

Use one of these private routes:

1. Use GitHub's **Report a vulnerability** option on the repository Security tab when private vulnerability reporting is enabled.
2. If that option is unavailable, email `jgnash.devel@gmail.com` with the subject `jGnash security report`.

Include only the information needed to reproduce and assess the problem:

- affected jGnash version or commit;
- operating system, architecture, and Java version;
- affected feature and whether remote access is involved;
- minimal reproduction steps or a proof of concept;
- expected and actual security boundary;
- potential impact;
- whether the issue is already public or under active exploitation.

Do not send real financial files, passwords, API tokens, private keys, or unredacted logs. Create a synthetic reproducer. If a real file is essential, first agree on a secure transfer method and data-retention plan with the maintainer.

## Response targets

The project aims to:

- acknowledge a complete report within 7 calendar days;
- provide an initial severity and scope assessment within 14 calendar days;
- coordinate a remediation and disclosure timeline based on exploitability, user impact, and migration risk;
- credit reporters who want public credit, unless legal or safety concerns prevent it.

These are best-effort targets for a volunteer-maintained project, not a service-level agreement. If no acknowledgement arrives, send one follow-up using the alternate private route.

## Disclosure process

- Keep the report private until a fix, mitigation, and user guidance are ready.
- The maintainer will validate supported release lines and whether data migration is required.
- Security fixes must include regression tests that do not publish a readily weaponized exploit unnecessarily.
- Releases should include checksums and clear upgrade/recovery instructions.
- A coordinated advisory should describe impact, affected versions, fixed versions, mitigations, and acknowledgements.

## Current remote-mode warning

The legacy remote multi-user implementation must not be exposed directly to the public internet. Its database, message, lock, and attachment channels do not yet share a completed, reviewed authenticated-transport design. Use network-level protection and strict firewalling, or avoid remote mode, until the security modernization work is complete.

## Security-sensitive development rules

- Never log or commit passwords, tokens, private keys, financial records, or unredacted database URLs.
- Never use production financial files as test fixtures.
- Treat imported documents, serialized data, network messages, attachments, and plugins as untrusted input.
- Use standard authenticated cryptography and transport protocols; do not introduce new custom cryptographic formats without review and versioning.
- Create and verify backups before any security fix that migrates encrypted or persisted data.
- Record time-bounded vulnerability suppressions with evidence and an owner.
