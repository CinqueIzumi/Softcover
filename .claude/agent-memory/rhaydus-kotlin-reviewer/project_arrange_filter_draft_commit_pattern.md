---
name: project_arrange_filter_draft_commit_pattern
description: Library Arrange/Filter sheets' draft-on-UiState pattern — recurring traps for its live "nudge" side action and its async collector-derived chip rows
metadata:
  type: project
---

`LibraryUiState.arrangeDraft` / `filterDraft` (seeded on sheet open, cleared on close, committed by a
no-arg `OnApplyArrangeAction`/`OnApplyFiltersAction`) replaced local `remember` state so `ArrangeDraftChipsCollector`
/ `FilterDraftChipsCollector` can derive `ChipUiModel` rows off it (R10 — never build a UI model in
composition). Two traps to re-check whenever this pattern is extended elsewhere:

1. **A live side-action that nudges the open draft must roll the nudge back on its own failure path.**
   Check every "nudge the open draft from outside its own draft-edit actions" call site for a matching
   rollback in its `onFailure` branch — a draft mutation that isn't reverted alongside the committed-state
   rollback leaves the open sheet showing a selection the commit no longer reflects, self-healing only if
   the user closes and reopens the sheet.
2. **Moving chip derivation from synchronous `remember` to an async collector trades zero-latency-correct
   render for a `distinctUntilChanged` → `collectLatest` → `withContext(Default)` round trip.** Both sheets
   render `chips.isEmpty` / `selection != null` gates that show neither content nor an empty-state message
   until the collector's first emission lands — a real (if likely sheet-transition-masked) blank-or-stale
   frame on every open. Worth a skeleton or optimistic first-frame fallback if this is ever reported as a
   visible flicker; not flagged as a blocker since it mirrors the established `FilterChipModelsCollector`
   R9 shape used throughout this feature.
