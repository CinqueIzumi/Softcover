---
name: feedback_mockk_default_param_stub_gotcha
description: A setUp() coEvery stub that omits a trailing default-valued parameter only matches calls where that parameter equals its default — not "any value".
metadata:
  type: feedback
---

When a mocked suspend function has a trailing parameter with a default (e.g. `RecordBookProgressUseCase.invoke(book, newPage = null, newSeconds = null, actionAt = null)`), a `coEvery { mock(any(), any(), any()) }` stub that only lists 3 matchers for a 4-param function does NOT mean "don't care about the 4th param" — Kotlin fills the omitted trailing arg with its default value, so the stub becomes an exact match on `actionAt == null`.

**Why:** discovered while adding `actionAt: String?` round-trip tests to `OnUpdatePageProgressClickActionTest` / `OnUpdateTimeProgressClickActionTest` / `OnUpdatePercentageProgressClickActionTest` in `:feature:book_detail`. The shared `setUp()` stub (`coEvery { updateBookProgress(any(), any(), any()) } returns Result.success(null)`) implicitly pinned `actionAt = null`. New tests constructing the action with a non-null `actionAt` fell through to MockK's relaxed-mock auto-generated return value (an untyped `Object`), which then blew up with a `ClassCastException` at the `outcome == ShelfMutationOutcome.Applied` comparison inside the action — a confusing failure that looks like a production bug but is a stub-matching gap.

**How to apply:** whenever a test exercises a non-default value for a trailing default parameter that an existing/shared `coEvery` stub doesn't explicitly cover, add a local `coEvery` in that test's Arrange section with an explicit matcher for every parameter that differs from the shared stub's implied defaults (e.g. `actionAt = any()` alongside `book = any()`, `newPage = any()`), returning the same success value. Mirror the pattern already used elsewhere in these files for `Applied`/failure-outcome overrides — this is the same "local override beats setUp() default" shape, just triggered by a param the shared stub silently pinned to its default rather than by the return value.
