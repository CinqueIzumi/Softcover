---
name: room-upsert-conflict-host
description: Room @Upsert on an existing key throws android.database.SQLException in :core:database host tests; seed state with a first insert path that avoids re-upserting
metadata:
  type: feedback
---

In `:core:database` androidHostTest (in-memory Room, BundledSQLiteDriver), a `@Upsert` whose row already exists throws a bare `android.database.SQLException` (the conflict-to-update path breaks on host). First insert works.

**Why:** hit with `replaceTrendingBooks` (upserts a singleton meta row); only the second call failed.

**How to apply:** never call an upsert-based DAO method twice on the same key in one test. Seed the "previous state" with a different DAO method (e.g. `insertTrendingBooks`) and leave the singleton row absent. Do not test "overwrites previous value" on host. Not a production bug (device is fine).
