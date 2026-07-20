---
name: project_toad_action_race_untracked_job
description: TOAD ActionScope.setState/setLocalVariables are synchronous StateFlow.update — dispatch() launches one coroutine per action on screenModelScope, so a same-action re-entrancy guard (read-state → set-flag before any suspension point) is sound with zero extra locking, but a suspend fetch not stored in a cancellable Job can race with a *different* fresh-start action and corrupt shared state.
metadata:
  type: project
---

Confirmed by reading the vendored `nl.rhaydus:toad-jvm` sources jar (ToadScreenModel.kt / ActionScope.kt):
`dispatch(action)` = `screenModelScope.launch { action.execute(dependencies, scope) }` (one new
coroutine per dispatched action); `ActionScope.setState`/`setLocalVariables` are plain
`MutableStateFlow.update { }` — synchronous, no suspension. So a same-action self-reentrancy guard
of the shape `if (state.flagAlreadySet) return; setState { flagSet = true }; <suspend fetch>` is
provably race-free without a mutex, *as long as* the check and the flag-set happen before the first
suspension point in that action's own coroutine.

**What that guard does NOT protect against**: a second, *different* action (e.g. a fresh
query/filter/sort change) that doesn't share the first action's Job. Found this exact bug in
Explore's search pagination (2026-07, feature/explore `OnLoadMoreSearchResultsAction` +
`executeSearch`/`ExploreSearchExecution.kt`): the four "start a fresh search" actions
(`OnQueryChangeAction`, `OnMoodChipClickAction`, `OnRetrySearchAction`, `OnSortModeChangeAction`)
all cancel `localVariables.queryJob` before starting, but `OnLoadMoreSearchResultsAction` calls its
suspend `executeSearch(page = nextPage)` directly in its own dispatched coroutine without ever
storing that coroutine as `queryJob`. Result: a stale in-flight "load more" (mood A, page 2) can
resolve *after* a fresh search (mood B, page 1) has already replaced the shared
`MutableStateFlow<List<Book>>` in the data source, and its `if (page > 1) previous + books else books`
append logic silently corrupts the new search's results with the old search's tail page (plus can
flip the shared `hasMore`/`isLoading` flags backwards mid-flight).

**How to apply**: when reviewing any TOAD "load more" / pagination action, check that its suspend
fetch is tied into the *same* cancellable-Job field that the feature's "fresh search/filter" actions
already cancel — not just that the load-more action's own re-entrancy guard is internally sound. The
two are separate questions; a reviewer (and the doc comment in the code) can correctly prove the first
and still miss the second. [[project_architecture]]

**Status (2026-07-20):** confirmed fixed in `OnLoadMoreSearchResultsAction.kt` — it now launches the
fetch via `dependencies.launch { executeSearch(...) }`, captures the returned `Job`, and stores it as
`ExploreLocalVariables.queryJob` via `setLocalVariables`, matching every fresh-search action. A
follow-up UI-only pass (mobile/desktop pagination `LaunchedEffect`+`snapshotFlow` wiring) was
reviewed against this fixed action and found sound — no re-flagging needed unless the action file
changes again.
