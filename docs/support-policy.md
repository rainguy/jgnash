# jGnash support policy

Status: active during modernization

Last reviewed: 2026-08-08

This policy defines what the jGnash project intends to support while the modernization roadmap in [`MODERNIZATION_PLAN.md`](../MODERNIZATION_PLAN.md) is being executed. It distinguishes current repository behavior from targets that are not yet delivered.

## Release support

| Release line | Status | Scope |
|---|---|---|
| Current `master` / 3.7 development line | Maintenance and migration source | Correctness, data recovery, and modernization work are accepted. The branch is not yet the modernized stable release. |
| Last published legacy release | Migration support only | Existing user data must remain recoverable through a documented migration path. General dependency and platform updates are not promised. |
| Future Java 21 modernized release | Planned supported line | Becomes supported only after the release gates in `MODERNIZATION_PLAN.md` pass. |
| Older releases | Unsupported | Users should migrate through a supported intermediate release when direct migration is unavailable. |

Security and data-loss fixes take priority over feature work. A release is supported only when it is listed here; a version number in a build file does not by itself establish support.

## Java support

- Current `master` compiles and tests with an explicit Java 21 toolchain and emits Java 21 bytecode.
- Java 11 is retained solely for reproducing the archived legacy baseline and for migration-source verification.
- Java 21 is the modernization target, but it becomes a supported release runtime only after the remaining release gates pass.
- A newer JDK is not supported merely because the application happens to start on it.
- A JDK becomes supported only after clean build, unit, integration, migration, packaged-startup, and relevant UI tests pass.
- Preview Java features are not supported in production releases.

## Operating-system and architecture support

The modernization target matrix is:

- Windows 10 and 11 on x86-64;
- macOS 13 or newer on x86-64 and AArch64, subject to native-runner availability;
- Ubuntu 22.04 and 24.04 on x86-64.

Other Linux distributions and architectures may work but are community-supported until they have repeatable CI and packaging coverage. A platform is promoted to supported only when:

- the complete required CI lane passes;
- a platform-native application image is produced;
- offline startup is tested;
- file open, save, backup, migration, and restore smoke tests pass;
- installer/uninstaller behavior is documented;
- native JavaFX dependencies are included rather than downloaded at runtime.

The current legacy distribution predates this matrix. The matrix describes the release gate, not a claim that today's artifacts already satisfy it.

## Data-format support

The repository currently recognizes these jGnash storage families:

- XML XStream;
- binary XStream;
- H2 page-store files associated with the legacy H2 1.3 path;
- H2 MVStore files associated with the legacy H2 1.4 path;
- HSQLDB files.

Until a later policy revision explicitly reclassifies them, all five are protected migration inputs. This means:

- a known valid legacy file must not be silently modified merely by inspection;
- migration must create and verify a backup before changing representation or schema;
- migration must preserve accounting invariants, references, metadata, and attachments;
- a failed or cancelled migration must leave the source and backup intact;
- removal of a legacy writer does not permit removal of its reader/migration path;
- a format can become migration-only only after the fixture suite, recovery instructions, and support window are published.

New storage formats and schema versions require explicit migrations. Ordinary file opening must eventually validate the schema rather than relying on ORM-generated `update` behavior.

## Import and export support

The following user-facing formats are compatibility-sensitive:

- OFX and QFX;
- QIF;
- MT940;
- jGnash XML/backup output;
- PDF reports;
- spreadsheet reports.

Parser permissiveness, decimal precision, locale behavior, date handling, and exported semantic content are public behavior. Dependency upgrades affecting these formats require representative fixtures and before/after validation.

## Plugin support

- The current plugin loader requires an exact manifest `Plugin-Version` match to interface version `2.25`.
- Until a versioned plugin contract and compatibility suite are delivered, plugins should be built and tested against the exact jGnash release that loads them.
- Internal packages are not a supported plugin API.
- The interfaces in `jgnash-plugin` are the intended starting surface, but binary compatibility is not guaranteed during pre-stable modernization milestones.
- An incompatible plugin must be rejected before activation with an actionable message.
- Plugin compatibility changes require an ADR, test plugin, and release note.

## Remote multi-user mode

Remote multi-user mode is not security-supported across untrusted networks in the current legacy implementation. In particular, the current implementation includes custom password-derived encryption and database transport paths that are not uniformly protected by standard authenticated TLS.

Until the remote-security milestone is completed:

- do not expose jGnash database, message, lock, or attachment ports to the public internet;
- use remote mode only on a host/network whose confidentiality and integrity are independently protected;
- firewall access to explicitly trusted peers;
- do not assume that a database password provides modern transport security;
- treat remote mode as provisional and subject to temporary restriction or disablement.

The modernized release must either secure every remote channel with a reviewed design or mark remote mode unsupported/disabled. There is no partial-security exception for one channel while companion channels remain exposed.

## Security support

- Security reports follow [`SECURITY.md`](../SECURITY.md).
- The project prioritizes vulnerabilities that can expose financial data, credentials, attachments, or code execution.
- Dependency vulnerability exceptions must state reachability, owner, mitigation, and expiry.
- Secrets and raw financial records must not be included in public issues, fixtures, or diagnostic bundles.

## Support lifecycle for migration tooling

- A legacy reader or migration utility must remain available for at least the support window of the modernized release that first replaces its format.
- The exact retirement date must be published before that release becomes stable.
- Retirement requires evidence that all catalogued fixtures migrate successfully through the still-supported path.
- Migration tooling may run in an isolated legacy process when incompatible database libraries cannot safely share a classpath.

## Changing this policy

A change that removes platform, format, plugin, or remote capability requires:

1. an ADR describing the decision and alternatives;
2. migration or recovery instructions when user data is affected;
3. a release-note entry and advance notice;
4. updated automated tests and fixtures;
5. an update to this policy in the same pull request.
