---
name: architecture-r2-lookup-table-applies-to-small-enums
description: component-contract.md R2's "lookup, never a branch inside the layout" rule also governs a small sizing enum, not just a big sealed content variant
metadata:
  type: feedback
---

R2 (`docs/reference/design-system/component-contract.md`) reads as being about sealed *content* variants
(`BookCardVariant`), but its "a variant that needs its own metrics gets a lookup keyed off the variant,
never a branch inside the layout" clause applies just as much to a small size/scale enum used only for
typography and spacing (e.g. `PageMastheadSize.Regular` / `Compact`).

**Why:** `PageMasthead.kt` resolved `titleStyle`, `subtitleStyle` and `titleToSubtitleGap` via three
separate `when (model.size) { … }` blocks inline in the composable body, instead of one
`PageMastheadDimensions.forSize(size)`-style table (the `ShareCardDimensions.forContent` /
`CoverDimensions.forVariant` pattern R2 names). Three parallel branches on the same enum is exactly the
drift risk R2 exists to prevent — a size added later only needs one branch updated to compile, silently
leaving the other two on stale values.

**How to apply:** any UI model field the render `when`s on more than once — even a plain enum, not a
sealed variant — should resolve through one table/lookup, not one branch per property.
