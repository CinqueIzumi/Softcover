---
name: book-detail-progress-action-id-stub
description: book_detail progress-action tests (OnUpdate*ProgressClickAction) need Book.id stubbed on every mock book, not just a relaxed use-case return
type: feedback
---

When these actions gained per-book job-cancellation tracking (`scope.currentLocalVariables.bookMutationJobs[bookToUpdate.id]?.cancel()`), the real red-suite root cause was `io.mockk.MockKException: no answer found for Book.getId()` from plain non-relaxed `mockk<Book>()` book-stub helpers — NOT only the `ClassCastException` from a relaxed `RecordBookProgressUseCase` mock's inline `Result.onSuccess`. Both defects coexisted.

**Why:** A prior task brief assumed the whole red suite was one cause (relaxed-mock cast). Running the suite and reading the actual stack trace (not just skimming the action's diff) revealed the id-getter exception fires first, on the very first line of `execute()`, before the use case is even invoked.

**How to apply:** Whenever a book_detail action's book-stub helper is `mockk<Book>()`/`mockk { ... }` with no `id` stub, and the action under test reads `book.id` for job tracking, add `every { id } returns <int>` (default 42) to that helper. Also add a baseline `coEvery { recordBookProgressUseCase(any(), any(), any()) } returns Result.success(null)` in `setUp()` for `RecordBookProgressUseCase`'s `Result<ShelfMutationOutcome?>` return type — specific per-test `coEvery` overrides still win over this baseline. See [[project_canonical_test_templates]] for the action-layer template this pattern should be folded into.
