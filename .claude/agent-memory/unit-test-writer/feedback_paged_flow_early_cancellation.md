---
name: paged-flow-early-cancellation-test-pattern
description: How to unit-test that a transformWhile/takeWhile-driven single-pass paging use case stops requesting pages early, without a real paged data source
metadata:
  type: feedback
---

To prove a use case's `Flow<T>.transformWhile { ... }.collect()` walk over a repository-mocked
stream genuinely stops early (doesn't drain a large upstream), build a `flow { }` in the test that
chunks a synthetic dataset into "pages," incrementing a `pagesRequested: MutableList<Int>` (or
counter) once per chunk *before* emitting that chunk's items — mirroring how the real remote data
source fetches a whole page via one network call and only then emits its rows one by one. Because
`transformWhile`'s predicate runs synchronously inside the upstream's `emit()` call, an
`AbortFlowException` thrown when the predicate returns false unwinds immediately from *inside*
that `emit()` — so any remaining items in the same already-fetched page are simply never emitted,
and the *next* page's counter increment never happens. Assert on the exact list of page numbers
requested (e.g. `pagesRequested shouldBe listOf(1, 2, 3, 4, 5)`) with a dataset deliberately sized
much larger than what should be paged (e.g. 200 items available, only 5 pages' worth ever pulled)
so the assertion is a real proof of early cancellation, not a coincidence of a short dataset.

**Why:** Verified on `RefreshReadingActivityUseCaseTest.SinglePassEarlyCancellation` — a combined
`hasUndeterminedStreakGap(...) || date >= windowStart` predicate over
`streamReadingDaysDescending()`, testing that the streak-determination and 21-day-window checks
jointly stop paging early rather than draining the whole account history. Working out exactly
which item triggers the predicate to go false required hand-tracing the algorithm (oldest date
seen vs. first-missing-day-walking-back) against a concrete crafted dataset — worth doing on paper
before writing the flow, since off-by-one errors in the crafted dataset are easy to make.

**How to apply:** Reuse this pattern whenever a use case does `repository.someDescendingStream()
.transformWhile { ... }.collect()` (or `.takeWhile`) and the whole point of a change is that it
avoids over-paging. Don't test this by asserting item counts collected — that doesn't distinguish
"stopped early due to logic" from "stream just happened to be short." Test the request/fetch
count on the upstream instead.
