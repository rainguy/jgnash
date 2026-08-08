# Synthetic data-format fixture catalog

This directory is the migration safety net for every persistence format exposed
by `DataStoreType`. Every person, institution, account number, transaction,
security, tag, and attachment is fictional and generated specifically for this
repository. Do not replace or extend this catalog with production financial
data.

## Inventory

`catalog.properties` is the authoritative machine-readable inventory. The
valid-fixture matrix is:

| Fixture family | Store and on-disk form | Scenario | Password |
|---|---|---|---|
| `xml-rich` | XML XStream, `.xml` | Feature-rich | Unsupported by this store |
| `binary-xstream-rich` | Binary XStream, `.bxds` | Feature-rich | Unsupported by this store |
| `h2-page-rich` | H2 1.3 page store, `.h2.db` | Feature-rich | Empty |
| `h2-mvstore-rich` | H2 1.4 MVStore, `.mv.db` | Feature-rich | Empty |
| `hsqldb-rich` | HSQLDB, `.script` and `.properties` | Feature-rich | Empty |
| `h2-page-password` | H2 1.3 page store | Minimal | `fixture-password` |
| `h2-mvstore-password` | H2 1.4 MVStore | Minimal | `fixture-password` |
| `hsqldb-password` | HSQLDB | Minimal | `fixture-password` |
| `xml-empty` | XML XStream | No user accounts or transactions | Unsupported by this store |
| `binary-xstream-minimal` | Binary XStream | USD plus one cash account | Unsupported by this store |

The password is intentionally public test data, not a secret. XML and binary
XStream ignore the password argument and therefore must never be described as
password-protected. HSQLDB rejects the parameterized password statement used by
the legacy `SqlUtils.changePassword` helper; the generator uses an HSQLDB JDBC
statement solely to create the protected fixture. Production remediation is a
separate work item.

Each rich fixture contains the same scenario:

- a 12-account hierarchy with USD and EUR accounts;
- one synthetic security linked to an investment account;
- salary, multi-currency, split, investment, and attachment transactions;
- one reconciled transaction and two tags;
- one monthly reminder and one budget;
- a 44-byte plain-text synthetic receipt in the sibling `attachments`
  directory.

Five `*-truncated` directories contain only the first 64 bytes of a valid
primary file, one for each parser. They are expected-invalid inputs and have no
semantic summary. Their detected file-magic classifications are pinned in the
catalog so future parser-dispatch changes are explicit and reviewed.

## Expected summaries and review

The three files under `expected/` are Java-properties summaries shared by
semantically identical fixtures. They record account count and hierarchy,
currency and security counts, transaction and entry counts, debit and credit
totals by currency, date range, reminder/budget/tag counts, attachment name,
size and SHA-256, and file-format version. Extra feature counters prove that
investment, multi-currency, split, reconciled, and attachment transactions did
not disappear.

Summary review is intentionally independent from fixture construction:

1. `DataFormatFixtureGenerator` writes objects through the normal engine APIs
   but does not create expected summaries.
2. `DataFormatFixtureSummaryPrinter` copies and opens each fixture, then derives
   a summary through public read APIs.
3. A reviewer compares that output with the documented scenario and edits the
   checked-in expected properties deliberately.
4. `DataFormatFixtureCatalogTest` repeats the read on fresh copies and requires
   exact map equality.
5. `payload-sha256.properties` detects any byte-level payload or attachment
   change that was not explicitly reviewed.

Run the independent report and regression test with:

```shell
./gradlew :jgnash-tests:printDataFormatFixtureSummaries --no-daemon
./gradlew :jgnash-tests:test \
  --tests jgnash.engine.fixture.DataFormatFixtureCatalogTest \
  --warning-mode fail --no-daemon
```

Regeneration is intentionally a separate command because database UUIDs and
store internals are not promised to be byte-reproducible:

```shell
./gradlew :jgnash-tests:generateDataFormatFixtures --warning-mode fail --no-daemon
```

After regeneration, independently review summaries and update every changed
payload checksum. Never update expected summaries or hashes merely to make the
test pass.

## Provenance, license, and storage decision

- Generator and payload provenance: created from the fixed fictional scenario
  in `DataFormatFixtureGenerator` on 2026-08-07.
- Legacy compatibility target: source commit
  `bc5aa91fa27c7f668e1cfb5c67abaea4408fb8e3`, the Java 11 baseline recorded in
  `docs/baselines/legacy-java11`.
- License: generated fixtures, summaries, and supporting source are distributed
  under the repository's GNU GPL v3-or-later license. The files contain no
  third-party or user-contributed data.
- Storage: the complete payload is approximately 1.4 MiB, contains no sensitive
  material, and is useful in every clone. It belongs in normal Git, not Git LFS
  or a protected test-data store. Revisit this decision before adding any
  individual file above 10 MiB, pushing the catalog above 25 MiB, or accepting
  any data that is not provably synthetic.

## Recorded legacy validation

On 2026-08-07, the catalog sources and resources were copied into an isolated
detached worktree at
`bc5aa91fa27c7f668e1cfb5c67abaea4408fb8e3`. The targeted catalog suite ran
with Eclipse Temurin `11.0.32+9` and the baseline Gradle 6.8.2 wrapper:

```shell
env GRADLE_USER_HOME=/tmp/jgnash-gradle-base04 \
    JAVA_HOME=/tmp/jgnash-jdk11 \
    ./gradlew :jgnash-tests:test \
      --tests jgnash.engine.fixture.DataFormatFixtureCatalogTest \
      --no-daemon
```

Result: `BUILD SUCCESSFUL in 1m 1s`; 20 tests, 0 skipped, 0 failures, and
0 errors. The test opens copies rather than source fixtures, so engine shutdown
cannot rewrite the catalog payloads. The temporary worktree and generated test
reports were removed after recording this result.
