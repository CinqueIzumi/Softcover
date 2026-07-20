---
name: project_skeleton_crossfade_contract
description: Foundation SkeletonCrossfade and rememberStaggeredEntryCoordinator implementation contracts, verified against source — use when reviewing any loading-skeleton or staggered-entry work
metadata:
  type: project
---

Verified 2026-07-21 against `../rhaydus-foundation/designsystem-core/src/commonMain/kotlin/nl/rhaydus/designsystem/`.

**`util/SkeletonCrossfade.kt`** — `SkeletonCrossfade(isLoading, modifier, label, content: @Composable (Boolean) -> Unit)`.
It is a thin wrapper around `androidx.compose.animation.Crossfade` (tween 150ms), gated by
`playDecorativeMotion()` (falls back to an unanimated `Box` under reduced motion). **It does no
shared/union sizing** — no `SubcomposeLayout`, no reserving the max of both branches. The container
just follows `Crossfade`'s stock behavior. This means **the caller is fully responsible for making
the skeleton branch and the loaded branch the same height** — if they differ, the card visibly
resizes at the end of the 150ms fade. See [[project_skeleton_dimension_math]] for the specific bug
class this produces and how to estimate it without running the app.

**`component/StaggeredEntry.kt`** — `rememberStaggeredEntryCoordinator(key: Any, stepMillis=60, windowMillis=350)`
+ `Modifier.staggeredEntry(coordinator, index, translateFrom=8.dp)`. The "first composition
timestamp" lives in a **process-wide global `mutableMapOf<Any, Long>`** keyed by the `key` argument
(reference/equals), populated via `getOrPut` — the first caller anywhere in the process with that
key wins the timestamp; it never resets except on process death. Consequence for review: **hoisting
the `rememberStaggeredEntryCoordinator(...)` call to above a loading/loaded branch (e.g. above a
`SkeletonCrossfade`) does NOT change timestamp correctness by itself** — what it changes is *whether
the call runs at all* while still loading. If it's called only inside the loaded branch, the very
first time that branch composes (i.e. the moment data arrives) is also the first time the timestamp
gets stamped — so `now - startMillis` is ~0 and the stagger animation fires at exactly the same
moment the crossfade is also fading in, i.e. double-animation. Hoisting it above the branch makes it
run at the section's own first paint (typically while still loading), so by the time data arrives
the 350ms window has usually already elapsed and the stagger silently no-ops, leaving only the
crossfade. This is the correct fix, verified against source — not just a plausible-sounding
comment.
