---
name: offline-sync-mocking-pattern
description: When OfflineUserBookSync is mocked in BooksRepositoryImplTest, every refreshUserBooks test needs a pass-through drainAndReconcile stub or initializeBooks is never called
metadata:
  type: feedback
---

After extracting `OfflineUserBookSyncImpl`, `BooksRepositoryImpl.refreshUserBooksInternal` delegates all drain+fetch+reconcile work to `offlineSync.drainAndReconcile { booksRemoteDataSource.initializeBooks(...) }`. Because `offlineSync` is `mockk(relaxed = true)`, the lambda (and therefore `initializeBooks`) is never invoked unless explicitly stubbed.

Every `refreshUserBooks` test that asserts on `initializeBooks` or on what gets cached must add this stub in its Arrange section:

```kotlin
coEvery { offlineSync.drainAndReconcile(any()) } coAnswers {
    firstArg<suspend () -> List<Book>>().invoke()
}
```

Tests that want to assert reconciliation behavior instead stub `drainAndReconcile` to return a pre-computed list:

```kotlin
coEvery { offlineSync.drainAndReconcile(any()) } returns listOf(reconciledBook)
```

**Why:** The relaxed mock silently returns an empty list and the lambda is never called, causing `initializeBooks` coVerify assertions to fail with "no calls recorded".

**How to apply:** Add the pass-through stub to every `refreshUserBooks` test in `BooksRepositoryImplTest` that touches `initializeBooks` stubs or assertions.
