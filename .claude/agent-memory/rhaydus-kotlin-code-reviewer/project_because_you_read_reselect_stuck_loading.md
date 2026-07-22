---
name: project_because_you_read_reselect_stuck_loading
description: Re-selecting the already-active "because you read" genre (or the already-effective Auto option) permanently strands loadingBecauseYouReadBooks at true because the preference flow is distinctUntilChanged and the action has no same-value guard.
metadata:
  type: project
---

Found 2026-07-20 (Explore 3a final review). `OnBecauseYouReadGenreSelectedAction` (feature/explore)
optimistically sets `loadingBecauseYouReadBooks = true` and clears `becauseYouReadBooks` unconditionally,
then calls `setBecauseYouReadGenreUseCase(genre)`, relying on the reactive collector
(`BecauseYouReadCollector`, fed by `GetBecauseYouReadBooksUseCase`) to eventually clear the loading flag
once the genre-derived flow re-emits. But `SettingsLocalDataSourceImpl.becauseYouReadGenre` is
`appSettingsDataStore.store.data.map { it.becauseYouReadGenre }.distinctUntilChanged()`, and
`GetBecauseYouReadBooksUseCase`'s own `genreFlow` is *also* `.distinctUntilChanged()`. So persisting the
**same** genre value that's already in effect (or "Auto" when the auto-derived genre is already showing)
never produces a new emission anywhere downstream — the collector never re-fires, and the action has no
`onSuccess` branch or early-return guard to clear the flag itself (unlike the equivalent `onFailure`
branch, which does clear it). Net effect: the rail is stuck on an empty skeleton for the rest of the
session.

**Why it's reachable, not just theoretical:** `BecauseYouReadGenreSheet` (`ExploreShelf.kt`) renders every
option including the currently-active one (checked with a primary tint + check glyph) and dispatches the
action unconditionally on ANY row tap, active or not — there is no `if (!active) runAction(...)` guard,
unlike the natural "tapping the selected item is a no-op" affordance users expect from this kind of sheet.

**How to apply:** whenever reviewing a "picker sheet dispatches a selection action" pattern in this
codebase, check two things together: (1) does the *sheet* skip dispatching when the tapped row is already
active, and (2) if not, does the *action* itself guard on "genre/value unchanged" the way
`OnSortModeChangeAction` does (`if (scope.currentState.sortMode == mode) return`)? A reactive
collector-clears-the-flag design (rather than the action clearing it on success) is a footgun specifically
when the upstream preference/settings flow is `distinctUntilChanged` — an unchanged persisted value is
silently swallowed with no downstream signal at all, which is very different from a network use case
where even an identical *result* still flows through a stateless one-shot call+setState.
