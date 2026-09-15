# Task 4 Report: Irrigation Operations and Audit

## Delivered

- Added an irrigation domain with persisted zones, run records, and audit records.
- Added `GET /api/zones` and `POST /api/irrigation/{zoneId}/{action}`. Supported actions are `start`, `stop`, and `pause`; the special target `all` is accepted only for `pause` and means pause every zone.
- Added safe HTTP DTOs. Responses include command result, confirmation state, and non-sensitive detail only; no vendor credential or command implementation detail is returned.
- Added `IrrigationCommandPort` as the replaceable controller boundary. `HydrawiseDemoIrrigationCommandAdapter` is an in-memory demo adapter with no credentials and no remote calls.
- Start operations require a positive duration and reject durations above the zone-specific maximum or outside the configured operation window. A start is also rejected if the target or another zone is already running.
- Control authorization is enforced for `ADMIN` and `OPERATOR`; other authenticated roles receive `403`. Parameter errors are `400`; schedule, duration-limit, and running conflicts are `409`.
- Every command outcome writes both an irrigation run and audit record with actor, target, action, parameter/reason summary, command result, confirmation, and outcome detail. Rejected and malformed actions are retained as well.
- The command flow reads state before dispatch, dispatches through the port, then reads state again to determine `CONFIRMED` or `PENDING`. Dispatch acceptance is therefore not treated as successful execution.

## Test-first Evidence

### Initial RED

Added `IrrigationOperationServiceTest` and `IrrigationControllerIntegrationTest` before production irrigation/audit types existed. Ran:

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn -q -Dtest=IrrigationOperationServiceTest,IrrigationControllerIntegrationTest test
```

The expected RED was test-compilation failure: the irrigation zone/run repositories, command port/types, service/policy/exceptions, and audit package did not yet exist.

### Green Coverage

The service tests cover positive-duration validation, per-zone maximum duration, operation-window rejection, active-zone conflict rejection, role enforcement, audit/run outcomes, and pre-dispatch plus post-dispatch state reads. The controller tests cover `400`, `403`, rejected-command persistence, and persistence for unsupported actions.

Focused GREEN command:

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn -q -Dtest=IrrigationOperationServiceTest,IrrigationControllerIntegrationTest test
```

Result: exit code 0. The focused irrigation suite contains 10 tests with no failures.

## Regression Found During Verification

The first HTTP rejected-command persistence test was deliberately added after the core path passed. It failed because `IrrigationService.command` was transactional and the intentional `IrrigationBadRequestException` rolled back the run/audit writes. The service has no enclosing command transaction now, so each repository write commits for outcomes while rejected commands can still surface as their intended HTTP status.

The unsupported-action persistence test then produced a second expected RED: action parsing happened before the guarded outcome-recording path, so a `400` lacked an audit/run row. Parsing and validation now occur inside the guarded path, with a bounded raw action label used only for the rejected record. The regression test passed after that change.

## Full Verification

```text
/Users/yokna/.trae/tools/maven/latest/bin/mvn test package
```

Result: `Tests run: 26, Failures: 0, Errors: 0, Skipped: 0`, followed by `BUILD SUCCESS`.

Created artifact:

```text
backend/target/smart-farm-backend-0.0.1-SNAPSHOT.jar
```
