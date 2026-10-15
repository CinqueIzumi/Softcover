---
name: room-upsert-conflict-untestable-on-host
description: Re-running a DAO @Upsert on an existing row (e.g. second BookDao.cacheBook) throws a message-less SQLException on host tests
metadata:
  type: feedback
---

Room @Upsert's conflict-to-UPDATE fallback cannot run under the sqlite-bundled-jvm host binary: the uniqueness error has a null message, so the second cacheBook of the same book fails with a bare `android.database.SQLException`.

**Why:** known limitation (see comment in UserTagVocabularyDaoTest). Stack traces are stripped by coroutine debug, so it looks like a mystery.

**How to apply:** test the first-insert path, and drive delete-then-insert steps (e.g. deleteUserBookRead + insertUserBookReads) directly instead of re-calling the whole @Transaction. Upgrade-open tests need no upsert: the `ExportedSchema` helper in core/database migration tests builds a real old DB from schemas/*.json.
