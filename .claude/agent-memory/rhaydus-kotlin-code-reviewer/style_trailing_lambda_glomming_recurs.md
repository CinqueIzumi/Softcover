---
name: style_trailing_lambda_glomming_recurs
description: The Softcover-delta "trailing lambda multi-line body must not glom `) }`" rule (docs/reference/code-style.md) is NOT ktlint-enforced — keep checking new files by eye.
metadata:
  type: feedback
---

`docs/reference/code-style.md` documents a Softcover-specific tightening of the foundation's trailing-
lambda exemption: when a trailing lambda's body is a multi-line construct, the lambda's `{`/`}` must
each be on their own line — never `) }` glommed. The doc says explicitly this is **not yet tool-
enforced** (ktlint's multi-arg wrapping rule exempts trailing-lambda calls and even produces the
glommed form itself), so it is review-only.

**Why this matters:** found a fresh instance in the Profile 1a-redesign Phase 0+1 pass
(`core/profile/.../ProfileRemoteDataSource.kt`, `val monthWindows = (1..MONTHS_IN_YEAR).map { month ->
monthWindow(\n    currentYear,\n    month,\n) }`) — `ktlintCheck` passed clean on it (confirmed by
running the task), so this is exactly the kind of violation that only a manual read catches. A known
pre-existing instance also lives in `feature/profile/.../UserInformationCollector.kt`
(`scope.setState { it.copy(\n    userProfileData = profileData,\n    isLoading = false,\n) }`) — out of
scope unless that file is itself touched (on-touch policy), but a useful worked example to recognize
the pattern.

**How to apply:** in every reviewed file, explicitly scan trailing-lambda calls (`.map {`, `.let {`,
`scope.setState {`, `.also {`, etc.) whose body spans multiple lines — grep for `) }` glommed patterns
literally, since `ktlintCheck`/`ktlintFormat` will not surface them.

**Scale check (2026-07-20, Explore 3a final review):** grepping `^\s*\)\s*\}[,)]?\s*$` across every
file touched by a branch found ~30 instances in a single PR, spread across production code
(`ExploreShelf.kt`, both `ExploreScreenLayout.{mobile,jvm}.kt` — mostly `onClick = { onBookClick(\n book,\n
SURFACE_X,\n) },` card-click callbacks) AND test code (`BecauseYouReadCollectorTest.kt`,
`FeaturedUpcomingReleaseCollectorTest.kt`, `BooksRepositoryImplTest.kt` — mostly `launch {
collector.onLaunch(\n scope = scope,\n dependencies = dependencies,\n) }`). This confirms the violation
is not a one-off slip but a systemic blind spot for both humans and agents writing this codebase's Compose
callbacks and coroutine-test bodies — budget time for a dedicated grep-and-fix pass rather than expecting
to catch every instance by eye during a large-diff review. A quick repo-scoped shell one-liner (grep for
lines matching `^\s*\)\s*\}` in touched files) is the fastest way to enumerate them.

**Regression pattern (2026-07-21, Reading 1a redesign review):** `feature/reading/.../ReadingShelf.kt`
had TWO fresh instances in `LinearWavyProgressIndicator(progress = { progressFraction.coerceIn(\n 0f,\n
1f,\n) }, ...)` (FeaturedProgressStat + ProgressBlock) — and the interesting part is these were
**regressions**: the pre-redesign code being replaced had the same `coerceIn` call correctly formatted
(`{` alone, body indented, `}` alone) on the old `LinearProgressIndicator`. The rewrite to
`LinearWavyProgressIndicator` reintroduced the glommed form. Lesson: when a diff swaps one component for
a near-identical one, diff the *old* formatting against the *new* one for these lambda bodies specifically
— a clean rewrite can silently un-fix a previously-compliant spot.
