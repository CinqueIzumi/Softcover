---
name: project_bottombarscaffold_f2_f3_adoption
description: Softcover adopted foundation F2/F3 BottomBarScaffold (2026-07-10), reversing a 2026-07-06 "deliberate skip"; introduced a LocalBottomBarPadding scoping widening worth re-checking on future touches
metadata:
  type: project
---

On 2026-07-10 (branch `hotfix/3.0.3`), Softcover adopted the foundation's `BottomBarScaffold` +
`BottomBarPlacement` (`DOCKED`/`OVERLAY`) generalization, reversing a 2026-07-06 "deliberate skip"
recorded in `docs/working/foundation-upstream-candidates.md` (F2 + F3's BottomBarScaffold half).
Deleted the app-local read-side fork `core/designsystem/.../presentation/util/BottomBarPadding.kt`
(which branched `rememberBottomBarPadding()` on `BottomBarStyle`, returning a hardcoded `16.dp` for
DOCKED) in favor of the foundation's unconditional `rememberBottomBarPadding() = LocalBottomBarPadding.current`.
`orchestration/.../BottomBarScreen.kt`'s `CompactNavShell` is now a single `BottomBarScaffold` whose
`placement` maps from the persisted `BottomBarStyle` preference (`toPlacement()`), with `CompactBottomChrome`
holding the docked/floating bar render. `WideNavShell` hosts the flush `SessionPeekBar` in the same
scaffold (`OVERLAY`, `barSpacing = 0.dp`).

**Verified equivalent (padding arithmetic):** the padding math checks out exactly against the *actual*
pre-change behavior (which was mediated by the now-deleted fork, not the shell's raw computed value) —
DOCKED reserves `0.dp footprint + 16.dp barSpacing = 16.dp` (matches the fork's old hardcoded value);
OVERLAY/FLOATING reserves `measured footprint (nav-inset included, since `onSizeChanged` sits *before*
`windowInsetsPadding` in the foundation host) + 16.dp barSpacing`, matching the old shell's
`bottomBarHeight (inset-excluded) + 16dp + navBarInset` formula once you account for where each version
puts the inset.

**Open architectural leak found in review (latent, not yet a visible bug):** `BottomBarScaffold` now
provides `LocalBottomBarPadding` around its *entire* `content()` parameter. Pre-change, the ambient was
scoped narrowly — only inside the `tabBody` `movableContentOf` block (i.e. only the list-pane tab body).
Post-change, on EXPANDED width `content()` is the whole `body` lambda that renders
`TwoPaneScaffold(list = { tabBody() }, detail = { BookDetailPaneHost(...) })`, so the **detail pane**
(`BookDetailScreen` via `BookDetailPaneHost`) now also sits inside the ambient's scope and would see a
non-zero `LocalBottomBarPadding` (the flush `SessionPeekBar`'s footprint) instead of the ambient default
`0.dp`. Currently inert because nothing in `feature/book_detail` calls `rememberBottomBarPadding()` — but
if a future change adds scrollable content there that reaches for it (the "standard" way screens get
bottom padding in this codebase), it will silently pick up padding meant for the tab body. Worth
re-checking whenever `BookDetailPaneHost`/`BookDetailScreen` or the `BottomBarScaffold` content-scoping
changes again.

See also [[feedback_style_guide_review]] for the general review process this was checked against.
