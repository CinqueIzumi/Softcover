# S5 — Skeletons

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

9 declarations collapse onto `Skeleton` + `SkeletonUiModel`.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-8 | All nine skeleton declarations | [ ] |
| S5-C-skeletons | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S5-8 — inventory

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `EditorialSectionHeaderSkeleton` | `feature/explore/presentation/screen/section/EditorialSectionHeaderSkeleton.kt:25` | |
| `FeaturedCardSkeleton` | `feature/explore/presentation/screen/section/FeaturedCard.kt:179` | |
| `RailCardSkeleton` | `feature/explore/presentation/screen/section/DiscoveryRailCard.kt:104` | |
| `TrendingCardSkeleton` | `feature/explore/presentation/screen/section/DiscoveryRailCard.kt:187` | |
| `BecauseYouReadCardSkeleton` | `feature/explore/presentation/screen/section/DiscoveryRailCard.kt:216` | |
| `SeriesCardSkeleton` | `feature/explore/presentation/screen/section/SeriesCard.kt:111` | |
| `MoodTileSkeleton` | `feature/explore/presentation/screen/section/MoodGrid.kt:233` | |
| `RoadmapSkeleton` | `feature/settings/presentation/screen/RoadmapContent.kt:458` | |
| `RoadmapSkeletonLine` | `feature/settings/presentation/screen/RoadmapContent.kt:496` | |

**Phase 1 questions:** seven of the nine are explore rail/card skeletons that mirror the shape of the
`BookCard` variant they stand in for (`Rail`, `Featured`, `Tile`) — decide whether `SkeletonUiModel`
carries a shape variant matching `BookCardVariant`, so a skeleton can be requested "in the shape of"
a card without `:core:component` depending on the not-yet-built `BookCard` (S7).
