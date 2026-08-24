---
name: sessionvaluecache-and-placeholder-settling
description: How to test a SessionValueCache-gated use case, and how to detect a settled (non-placeholder) Turbine emission when the flow emits a transient loading placeholder first
metadata:
  type: project
---

`nl.rhaydus.softcover.core.domain.util.SessionValueCache<K, V>` is a real, concrete, mutex-guarded class — never mock it. Instantiate a real one in `setUp()` (`SessionValueCache()`) and pass it to the use case's constructor. It caches success only (a thrown `load()` propagates without storing), so "does not cache a failure" tests just need two `coEvery` stubs in sequence (throw, then return) around two `useCase()` calls, with `coVerify(exactly = 2)` after.

For an unkeyed cache (`SessionValueCache<Unit, V>`, via the `getOrPut(load)` extension), one call to the use case gates the whole session. For a keyed cache (`SessionValueCache<K, V>`, e.g. `booksByGenreCache: SessionValueCache<String, List<Book>>` in `GetBecauseYouReadBooksUseCase`), caching is per-key — a switch away and back to the same key must still hit the cache, so the critical test is "leave key A, visit B, return to A, assert `fetchX(key = A, ...)` still called exactly once total."

When the use case under test emits a transient `loading = true` placeholder before the settled value (e.g. `BecauseYouReadRecommendation.loading`, emitted on a genre *switch* before the network/cache result arrives), a Turbine `while (recommendation?.genre != target) { recommendation = awaitItem() }` loop can stop AT the placeholder — which already carries the new genre — before the cache/network call has actually completed. This makes a `coVerify` placed right after `useCase().test { ... }` unreliable (it may read 0 instead of 1, because the flow was cancelled before the call ran). Fix: loop on `recommendation == null || recommendation.genre != target || recommendation.loading` so the loop only exits once the SETTLED (non-loading) emission for that genre has been observed.

**Why:** Discovered while adding cache tests to `GetFeaturedUpcomingReleaseUseCase`, `GetMoodTagsUseCase`, and `GetBecauseYouReadBooksUseCase` (2026-08-23 hotfix reducing Explore-tab request volume, issue tracked separately as #276 for a related starvation bug in `EnrichDismissedContinueSeriesMetadataUseCase`'s row cap — not to be "fixed" or asserted as correct in tests).

**How to apply:** Any test involving a `SessionValueCache`-backed use case or a use case with a "switch placeholder" emission pattern (see the Network Layer section of `docs/reference/architecture.md` for the rate-limit rationale) should follow this pattern.
