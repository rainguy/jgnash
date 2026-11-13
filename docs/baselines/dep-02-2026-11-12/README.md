# DEP-02 test dependency acceptance evidence

Recorded on 2026-11-12 (America/New_York). Baseline: `79c8e5535`.
The after snapshot belongs to the implementation commit containing this file.

Environment: Linux x86_64, kernel `7.2.15-arch1-1`, OpenJDK
`21.0.13.0.3+1`, Gradle wrapper `8.14.3`.

## Dependency comparison

The JUnit BOM is now explicit in the shared Java convention and the included
build. Jupiter remains at 5.14.4 and Platform at 1.14.4. Awaitility 4.3.0,
TestFX 4.0.18, and the JavaFX 21-compatible Monocle 21.0.2 pin are retained.
This finishes BOM alignment and removal of the temporary-folder extension;
it does not introduce another library upgrade or change production dependencies.

The before/after `testRuntimeClasspath` trees cover `jgnash-tests`, `jgnash-fx`,
and `build-logic`. The other modules inherit the same test convention.
No resolved module version changes and no modules are added. Removing
`junit-extensions` also removes its unused transitive dependencies:

- `io.github.glytching:junit-extensions:2.6.0`
- `io.github.benas:random-beans:3.9.0`
- `org.objenesis:objenesis:3.0.1`
- `io.github.classgraph:classgraph:4.6.18`
- `com.fasterxml.jackson.core:jackson-databind:2.9.8`
- `com.fasterxml.jackson.core:jackson-annotations:2.9.0`
- `com.fasterxml.jackson.core:jackson-core:2.9.8`

Hamcrest remains a transitive dependency of Awaitility/TestFX; the three
application test assertions that used it now use JUnit's `assertInstanceOf`.

To regenerate a tree, run each task with its own configuration option:

```shell
./gradlew :build-logic:dependencies --configuration testRuntimeClasspath \
  :jgnash-tests:dependencies --configuration testRuntimeClasspath \
  :jgnash-fx:dependencies --configuration testRuntimeClasspath
```

## Test discovery and behavior

Both before and after clean gates passed:

```shell
./gradlew build-logic:test clean check --warning-mode=fail --no-daemon --stacktrace
```

| Module | Before | After | Skipped in each run |
|---|---:|---:|---:|
| build-logic | 1 | 1 | 0 |
| jgnash-fx | 2 | 2 | 0 |
| jgnash-tests | 359 | 359 | 2 |
| mt940 | 5 | 5 | 0 |
| Total | 367 | 367 | 2 |

`test-discovery.tsv` records counts for every suite; failures and errors are
zero. The two existing disabled Yahoo network tests account for the skips.
Every discovered test identity was compared. Only
`TransactionTest.testBackEnd(TemporaryFolder)` and
`TransactionTest.testEmptyAccount(TemporaryFolder)` change display names to
use `Path`, matching their new `@TempDir` parameter type. No tests are lost.

The expanded TestKit test resolves the actual BOM-aligned components, rejects
the removed extension in the resolved graph, and runs a nested fixture with
two parameterized invocations using isolated `@TempDir` directories. It
checks that the nested XML report contains exactly two tests and no failures
or errors. Those nested tests are in addition to the 367 outer test results.

Focused verification also passed before the clean gate:

```shell
./gradlew build-logic:test --warning-mode=fail --no-daemon --stacktrace
./gradlew :jgnash-tests:test --tests ApiTest \
  --tests jgnash.engine.TransactionTest \
  --tests jgnash.engine.InvestmentPerformanceTest \
  --tests jgnash.engine.InvestmentTransactionTest \
  --tests jgnash.convert.importat.ofx.OfxExportText \
  --tests jgnash.engine.net.security.IEXParserTest \
  --warning-mode=fail --no-daemon --stacktrace
```

Hosted matrix acceptance is recorded in `MODERNIZATION_PLAN.md` after the
implementation commit passes CI. Fixture payloads and reviewed financial
results are unchanged. Rollback is a revert of the implementation commit;
no data migration or release is required.
