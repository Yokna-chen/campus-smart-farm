# SDD ledger — plan: docs/superpowers/plans/2026-09-14-smart-farm-backend-implementation.md
Plan scan: Task 1 produces persistence/application baseline consumed by Tasks 2-5; Task 2 produces auth context consumed by all controllers; Task 3 produces integration snapshots consumed by overview and irrigation; Task 4 produces irrigation operations consumed by overview/audit; Task 5 consumes all prior interfaces.
Task 1: fix round 1/5 started — Ruling: use documented DB_URL, DB_USERNAME, and DB_PASSWORD variables for the MySQL profile because repository deployment documentation is the existing public contract; cost if wrong: deployment configuration must be renamed.
Task 1: fix round 1/5 (2 addressed, 1 open — seed fixture assertions; uncommitted working tree)
Task 1: fix round 2/5 (1 addressed, 0 open; uncommitted working tree)
Task 1: complete (uncommitted working tree, review clean)
Task 2: minor (deferred): add explicit integration assertions for `Authorization: Basic` and empty bearer forms.
Task 2: complete (uncommitted working tree, review clean)
Task 3: fix round 1/5 started — retain the newest source-time snapshot; record ordinary adapter failures; inject integration ports rather than concrete demo adapters. Cost if wrong: out-of-order telemetry could mislead users and production adapter substitution would require scheduler changes.
Task 3: fix round 1/5 (2 addressed, 1 open — concurrent stale-write race; uncommitted working tree)
Task 3: fix round 2/5 (1 addressed, 0 open; uncommitted working tree)
Task 3: minor (deferred): directly test concurrent missing-row unique-key insertion collision path.
Task 3: complete (uncommitted working tree, review clean)
