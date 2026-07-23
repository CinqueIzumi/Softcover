---
name: project_adaptive_modal_sheet_scroll_contract
description: AdaptiveModalSheet's content slot does NOT scroll itself — the call site's Column must add .verticalScroll() if content can overflow
metadata:
  type: project
---

Confirmed from `docs/rhaydus/0.3.1/design-system-foundations.md` §5.9: `AdaptiveModalSheet`'s
`content` slot is "identical to `ModalBottomSheet`'s (a `ColumnScope` body owning its own padding
**and scroll**)". The sheet component itself only owns the width branch (bottom sheet vs. centered
panel), the `surfaceContainerLowest` fill, scrim/back dismissal, and the panel's top inset — it does
NOT wrap the content in a scroll container. A sheet body whose content can run long (a chip list,
option list, anything not a fixed few rows) must explicitly wrap its own `Column` in
`.verticalScroll(rememberScrollState())`, or content beyond the sheet's capped height (bottom sheet
viewport / ~90%-of-window expanded panel) is simply unreachable.

**Confirmed miss (2026-07-20, feature/explore `ExploreShelf.kt`):** the new
`BecauseYouReadGenreSheet` (genre picker, `AdaptiveModalSheet` wrapping an editorial header + a
`FlowRow` of `PillChip`s for every distinct genre tag across the reader's library) omits
`.verticalScroll()` on its `Column`, while the sibling `ContinueSeriesDismissSheet` in the exact
same file correctly adds it. `becauseYouReadGenreOptions` is explicitly documented (state comment)
as "the distinct Genre-category tags across the user's own books" — realistically double digits for
an avid reader — so this isn't a theoretical risk.

**How to apply:** whenever reviewing a new `AdaptiveModalSheet` call site, check its content
`Column` for `.verticalScroll(...)` (or another scrollable container) unless the content is
provably a fixed, small number of rows (e.g. a 2-3-option confirm sheet). Compare against sibling
sheets in the same file — if one has it and the new one doesn't, that's a strong signal the omission
is an oversight, not a deliberate choice. [[project_design_system_doc_drift_pattern]]
