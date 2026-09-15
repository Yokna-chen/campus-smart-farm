# Task 3 Report: Integration Ports and Synchronization

## Delivered

- Added the vendor-neutral, read-only `SnapshotPort` boundary and `ExternalSnapshot` transport model.
- Added `FusionSolarDemoAdapter` and `HydrawiseDemoAdapter`. These are explicitly demo-only: they create sample snapshots locally, retain no credentials, and make no external network requests.
- Added `IntegrationSyncService` with a fixed maximum of three retrieval attempts. Retrieval failures are recorded as `FAILED` `SyncExecution` rows with attempt count and an error message; failures are not rethrown to cause unbounded scheduler retries.
- Added persistence for the latest snapshot (`source`, device, metric, value, source timestamp, received timestamp) and separate sync execution history (`integration`, status, attempts, start/end timestamps, error message).
- Added `SnapshotFreshness`, which uses the external source timestamp rather than arrival time. Data older than the configured maximum is `DELAYED`, so a newly received stale payload cannot be represented as current.
- Enabled scheduling and added `IntegrationScheduler`, configurable with `farm.integration.sync-enabled` (default `true`) and `farm.integration.sync-delay-ms` (default five minutes).

## Test-first Evidence

`IntegrationSyncServiceTest` was added before the integration production types. The initial Maven test run failed during test compilation because `IntegrationSyncService`, the port/model types, repositories, status model, and freshness type did not yet exist. After implementation, the focused suite passed.

The suite covers:

- Mapping a port snapshot into the persisted latest-snapshot record while preserving both source and received timestamps, plus a successful sync record.
- Freshness classification using source timestamp and marking data older than 60 seconds as `DELAYED`.
- Retry cap of three attempts and persisted failed sync status/error message.

## Deliberate Boundaries

- Controllers are unchanged; no vendor protocol is exposed through HTTP APIs.
- No FusionSolar or Hydrawise SDK, endpoint, token, or credential assumption appears in the integration domain/service layer.
- Adapter replacement is isolated to `SnapshotPort` implementations when production credentials and vendor protocol contracts are later introduced.

## Review Fixes: Red/Green Evidence

### RED

Before changing production integration code, added these regression tests to `IntegrationSyncServiceTest`:

- `syncDoesNotReplaceNewerSnapshotWithAnOlderSourceTimestamp`
- `syncRecordsNormalRuntimeFailuresWithoutEscaping`
- `portRegistryExposesNamedSnapshotPortAbstractions`

Ran:

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn -q -Dtest=IntegrationSyncServiceTest test
```

Observed expected RED compilation failure: `NamedSnapshotPort` and `IntegrationPortRegistry` were missing from production code (`cannot find symbol` at the new registry test declarations/constructor). The stale-write and ordinary-runtime-failure assertions were also added before their production behavior changes.

### GREEN

Added `NamedSnapshotPort` plus `IntegrationPortRegistry`; converted both demo adapters to named port implementations; changed the scheduler to iterate `Map<String, SnapshotPort>` from that registry; changed retries/failure recording to catch `RuntimeException`; and rejected an incoming latest-snapshot update when its source timestamp is before the persisted timestamp.

Re-ran the focused command above. It completed with exit code 0, including all six integration tests. This verifies fresh-then-stale preserves `12.50` at the newer source timestamp, normal `IllegalStateException` retries three times and persists `FAILED`, and registry values are exposed as `SnapshotPort` abstractions.

`RuntimeException` is intentionally narrower than `Throwable`, so JVM fatal `Error` instances are not swallowed by scheduling synchronization.

### Full-suite Verification

The first full-suite run revealed a test isolation issue, not a synchronization regression: the default application test context schedules demo synchronization immediately and shares the named in-memory H2 database with the integration context. Its later demo row (`8.40`) was newer than the test fixture source time and therefore correctly won the timestamp ordering rule. Added `@BeforeEach` cleanup for only the two integration repositories inside the already-transactional integration test class.

Final command:

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn test package
```

Final result: `Tests run: 15, Failures: 0, Errors: 0, Skipped: 0`, followed by `BUILD SUCCESS`; the Spring Boot jar was created at `backend/target/smart-farm-backend-0.0.1-SNAPSHOT.jar`.

## Concurrent Latest-Snapshot Guard

### RED

Added `concurrentTimestampUpdatesCannotLetAnOlderSourceTimestampWin` before the persistence implementation. It starts two executor threads together behind a `CountDownLatch`; each invokes the sync service for the same unique snapshot key, one with source time `NOW`/value `12.50` and one with `NOW - 60 seconds`/value `2.00`. The test asserts the final stored timestamp/value are the newer pair.

Ran:

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn -q -Dtest=IntegrationSyncServiceTest test
```

Observed expected RED compilation failure because `LatestSnapshotRepository.updateWhenSourceTimestampNotAfter(...)` did not exist (`cannot find symbol` at `IntegrationSyncServiceTest.java:156`). That missing operation was the database-level compare-and-set boundary required to close the read-then-write race.

### GREEN

Added the repository `@Modifying` JPQL update whose predicate includes `s.sourceTimestamp <= :sourceTimestamp`. `LatestSnapshotWriter` executes this compare-and-set in `REQUIRES_NEW`; for a missing row, a unique-key insertion collision is surfaced to the caller and retried against the newly committed row, where the timestamp predicate is re-evaluated. `IntegrationSyncService` now uses this writer for every snapshot.

Focused GREEN command completed with exit code 0 and `IntegrationSyncServiceTest` reported `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`.

Final full verification:

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn test package
```

Result: `Tests run: 16, Failures: 0, Errors: 0, Skipped: 0`, followed by `BUILD SUCCESS`.
