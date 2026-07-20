---
name: project_skeleton_dimension_math
description: How to check whether a skeleton placeholder's dp height actually matches its loaded counterpart's text height — a recurring near-miss in Explore's skeleton work
metadata:
  type: project
---

`SkeletonCrossfade` does no shared sizing (see [[project_skeleton_crossfade_contract]]), so a
skeleton bar's claimed height must be checked against the real text style's `lineHeight`, not
against a flat "12dp = small text, 16dp = title text" gut feel — that convention systematically
undershoots larger roles.

Softcover's `MaterialTheme.editorialTypography` (`core/designsystem/.../theme/EditorialTypography.kt`)
inherits Material3's stock type-scale sizes for every role it doesn't explicitly `.copy()` a
fontSize/lineHeight onto (it only overrides `fontFamily` by default): `titleMedium` = 16sp/24sp,
`titleSmall` = 14sp/20sp, `bodySmall`/`eyebrowSmall` (built off `labelSmall`) = 12sp/16sp (11sp/16sp
for eyebrowSmall), `headlineSmall` = 24sp/32sp. A 2-line `Text(minLines=2, maxLines=2)` reserves
roughly `2 × lineHeight` regardless of glyph size.

Found in the 2026-07-21 Explore skeleton-crossfade review: `RailCardSkeleton` and
`SeriesCardSkeleton` (both in `feature/explore/.../screen/ExploreShelf.kt`) use flat 14–16dp bars for
a 2-line `titleMedium`/`titleSmall` title (real: ~48dp/~40dp for 2 lines) and a flat 12dp bar for a
`bodySmall` subline row (real: 16dp explicit row height) — each card undershoots its loaded
counterpart's total height by ~12dp. This DOES matter for a `Column`-stacked card (cover, title,
subline all stacked → heights sum), unlike `FeaturedCardSkeleton`, where the cover-and-text pair is a
`Row` and the fixed-aspect-ratio cover (144dp) dominates over both the real and skeleton text
columns (68dp vs 84dp) — so a mismatch inside a `Row` next to a tall image is often invisible (`Row`
height = max of children), while the same style of mismatch inside a `Column` is NOT (heights sum).
**When auditing a new skeleton, check whether the mismatched element sits in a `Row` (masked by a
taller sibling) or a `Column` (adds directly to total height) before deciding severity.**
