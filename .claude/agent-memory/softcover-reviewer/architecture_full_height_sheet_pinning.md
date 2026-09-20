---
name: architecture_full_height_sheet_pinning
description: Verified (via M3 source) that AdaptiveModalSheet safely supports a pinned-header + Modifier.weight(1f)+verticalScroll scrollable body on both the SHEET and desktop PANEL forms — no need to re-litigate this pattern's correctness in future reviews.
metadata:
  type: project
---

**Question that recurs:** a sheet body wants pinned rows (e.g. a top bar, header, input block) with
only one inner region scrolling, via `Column { PinnedStuff(); Scrollable(Modifier.weight(1f)
.verticalScroll(...)) }` nested inside `AdaptiveModalSheet`'s `content` slot. Does this actually work,
or does it risk an unbounded-height crash / fail to fill available height, on both the compact
bottom-sheet form and the desktop centered-panel form?

**Verified 2026-07-16** (tag-editor redesign review) by reading the M3 `material3-1.11.0-alpha07`
sources (`ModalBottomSheet.kt` / `SheetDefaults.kt`) and `AdaptiveModalSheet.kt`
(`rhaydus-foundation/designsystem-core`):

- **Bottom-sheet form**: M3's outer `Box(Modifier.fillMaxSize().imePadding())` gives the sheet's
  `Surface` a *bounded* max height (the screen height), even though the `Surface` itself has no
  explicit `heightIn`/`fillMaxHeight`. M3's own inner `Column` (drag handle + `content()`) has no
  weighted children of its own, so it wraps to content — but our nested `Column` (the one actually
  declared inside `content()`, carrying the `weight(1f)` child) receives that *bounded* max height
  and, per Compose's Row/Column measure policy, a weighted child forces its immediate parent Column to
  consume the *entire* incoming max constraint rather than wrap-content. That "fill" cascades back up
  through M3's non-weighted outer Column and Surface (which just wrap their child's reported size), so
  the whole sheet ends up full-height with the `Expanded` anchor computed at `offset = 0`. No crash,
  correctly fills available height.
- **Desktop panel form**: `DesktopSheetPanel`'s `Surface` has an explicit `.heightIn(max =
  windowSize.heightDp * 0.9f)` — directly bounded — so the same weight-forces-full-expansion mechanism
  applies one level shallower, with the same result.

**How to apply:** this pattern (pinned rows + one `weight(1f)` scrollable region, filling available
height) is safe and idiomatic in this codebase's `AdaptiveModalSheet`. Don't flag it as a
correctness risk by default — but do still check that `imePadding()` is applied at the right level
(the outer pinned+scrollable `Column`, not just the scrollable child) so the pinned rows stay above
the keyboard rather than the whole sheet shifting.
