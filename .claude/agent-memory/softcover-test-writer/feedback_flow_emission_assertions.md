---
name: feedback_flow_emission_assertions
description: Turbine traps — StateFlow conflation hides a reset-then-recompute transient unless all three values differ; a loop that exits on a loading placeholder makes the following coVerify read 0.
metadata:
  type: feedback
---

## Reset-then-recompute needs three distinct values

When production code does two sequential `MutableStateFlow.update { }` calls in one suspend function
(e.g. `SearchRemoteDataSourceImpl.searchByMood`/`searchForName` reset `_queriedBooks` to `emptyList()`
and `_queriedBooksHasMore` to `true`, then set the computed value), `StateFlow` conflates equal
consecutive values — a `true -> true` or `[] -> []` transition never emits, so `awaitItem()` hangs or
the test under-asserts.

**How to apply:** pick a baseline and a final value that differ from the reset value and from each other
(baseline `false` → reset `true` → final `false`, or non-empty → empty → a different non-empty list),
then `flow.test { awaitItem(); <call>; awaitItem(); awaitItem() }`.

## Loop past loading placeholders

When a flow emits a transient `loading = true` placeholder before the settled value (e.g.
`BecauseYouReadRecommendation.loading` on a genre switch), a Turbine loop like
`while (recommendation?.genre != target) { recommendation = awaitItem() }` can stop AT the placeholder,
which already carries the new genre. The flow is then cancelled before the cache/network call runs, and a
`coVerify` after `.test { }` reads 0 instead of 1.

**How to apply:** loop on `recommendation == null || recommendation.genre != target || recommendation.loading`
so it only exits on the settled emission.
