# 0004: Migrate persistence through verified, reversible conversion

- Status: Accepted
- Date: 2026-08-07
- Work items: `BASE-02`, `BASE-04`, `DATA-01` through `DATA-10`
- Supersedes: none
- Superseded by: none

## Context

jGnash stores financial records in XML/binary XStream, legacy H2 page-store, H2 MVStore, and HSQLDB formats. It uses Hibernate 5.4, Java Persistence 2.0/`javax.persistence`, H2 1.4.200, and automatic schema update. Modern H2 and Hibernate releases introduce file, namespace, schema, query, and type-system incompatibilities. Silent corruption or inability to recover a user's records is unacceptable.

## Decision

- Treat all currently recognized storage families as protected migration inputs.
- Build synthetic/anonymized golden fixtures and expected accounting summaries before a database or ORM major upgrade.
- Inspect sources read-only before migration.
- Close/flush the legacy engine and create a unique, checksummed, validated backup before conversion.
- Write migrated data to a distinct destination and leave the source untouched.
- Use a versioned canonical interchange representation between incompatible legacy readers and modern writers.
- Isolate incompatible legacy database engines in a non-networked subprocess when they cannot share a classpath.
- Replace `hibernate.hbm2ddl.auto=update` with schema validation plus explicit, versioned migrations.
- Provide CLI inspect, backup, convert, verify, and restore operations that the UI can call.
- Retain legacy readers for a published support window.

## Alternatives considered

### Open old databases directly with modern engines

Some library versions cannot read old files safely or at all. Relying on direct open makes library behavior the migration strategy and risks modifying the only copy.

### Let Hibernate update schemas automatically

Automatic update is difficult to review, version, rehearse, and roll back. ORM mapping changes can produce unexpected schema changes.

### Support only export from the legacy GUI

This strands users when the old GUI no longer starts on their platform and makes bulk/headless recovery difficult.

## Consequences

### Positive

- Migration becomes observable, testable, and recoverable.
- Database/ORM upgrades are decoupled from legacy file reading.
- Users receive a durable backup and verification report.

### Negative

- The project must temporarily maintain legacy-reader code and fixtures.
- Migration may require extra disk space and an additional process.
- A canonical format requires deliberate versioning and compatibility rules.

### Risks and mitigations

- Risk: fixtures miss real-world structures.
  - Mitigation: cover every domain feature with synthetic fixtures and add privacy-reviewed cases from field reports.
- Risk: backup exists but is unusable.
  - Mitigation: compare checksums and reopen/structurally validate the backup before conversion.
- Risk: semantic loss despite matching row counts.
  - Mitigation: verify accounting totals, references, metadata, and attachment hashes.

## Verification

- Golden migration suite passes every protected format and password variant.
- Failure/cancellation tests leave source and backup unchanged.
- Restore drills are executed from the generated manifest.
- Ordinary open validates rather than mutates schema.

## Revisit conditions

- Migration telemetry and published support windows show a legacy reader can be retired.
- The canonical interchange format cannot represent a required domain feature without loss.
