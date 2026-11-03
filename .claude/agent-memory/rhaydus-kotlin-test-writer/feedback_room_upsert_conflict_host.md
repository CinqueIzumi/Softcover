---
name: room-upsert-conflict-host
description: A second Room @Upsert on an existing key throws a message-less android.database.SQLException in :core:database host tests; seed and test around it
metadata:
  type: feedback
---

In `:core:database` androidHostTest (in-memory Room, BundledSQLiteDriver), an `@Upsert` whose row already exists throws a bare `android.database.SQLException`: the conflict-to-UPDATE fallback cannot run under the sqlite-bundled-jvm host binary because the uniqueness error has a null message. The first insert works; device builds are fine.

**Why:** hit with `replaceTrendingBooks` (a singleton meta row) and a second `BookDao.cacheBook` of the same book. Coroutine debug strips the stack trace, so it looks like a mystery (see the comment in `UserTagVocabularyDaoTest`).

**How to apply:** never call an upsert-based DAO method twice on the same key in one test, and don't test "overwrites previous value" on host. Seed the previous state with a different DAO method (e.g. `insertTrendingBooks`), test the first-insert path, and drive delete-then-insert steps (e.g. `deleteUserBookRead` + `insertUserBookReads`) directly instead of re-calling the whole `@Transaction`. Upgrade-open tests need no upsert: the `ExportedSchema` helper in the `core/database` migration tests builds a real old DB from `schemas/*.json`.
