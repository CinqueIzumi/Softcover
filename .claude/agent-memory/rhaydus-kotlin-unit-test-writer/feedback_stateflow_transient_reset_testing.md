---
name: feedback_stateflow_transient_reset_testing
description: How to test a "reset-then-recompute" MutableStateFlow update inside a suspend function, given StateFlow conflates equal consecutive values
metadata:
  type: feedback
---

When production code does two sequential `MutableStateFlow.update { }` calls inside one suspend
function (e.g. `SearchRemoteDataSourceImpl.searchByMood`/`searchForName` reset `_queriedBooks` to
`emptyList()` and `_queriedBooksHasMore` to `true` before running a fresh page-1 search, then set
the final computed value afterward), a naive Turbine test that seeds a baseline state and then
expects to observe both the transient reset value AND the final value will silently hang or
under-assert if either transition happens to collapse to an equal value — `StateFlow` conflates
consecutive equal values, so `awaitItem()` will never fire for a `true -> true` or `[] -> []`
no-op transition.

**How to apply:** when testing this "reset before fetch" pattern, deliberately choose baseline and
final values that differ from BOTH the reset value and each other, so all three states are
distinct and observably emitted in sequence via `flow.test { awaitItem(); <call the suspend fn>;
awaitItem(); awaitItem() }`. Concretely: seed a baseline of `false` (or a non-empty list), have the
reset flip to `true` (or empty), and have the final recomputed value settle back to `false` (or a
different non-empty list) — three distinct values, three distinct emissions. Verified this in
`SearchRemoteDataSourceImplTest` (`feature/explore`) for both `searchByMood` and `searchForName`'s
fresh-search reset of `_queriedBooks`/`_queriedBooksHasMore`.

See also [[feedback_coroutine_safe_tests]] for the broader TestDispatcher/runTest wiring this
relies on.
