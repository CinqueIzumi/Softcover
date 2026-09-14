package nl.rhaydus.softcover.core.component.cover

/**
 * Every surface that renders a book cover, one `data object` per surface. [Cover] does not branch on
 * this directly — it reads treatment (elevation, corner radius, shadow tint, decode cap) from
 * [CoverDimensions.forVariant], keyed off the variant, following the `ShareCardDimensions.forContent`
 * pattern (`component-contract.md` § 7.2 R2).
 *
 * Two variants — [ReadingHeroBackdrop] and [FullScreenViewer] — are never passed to [Cover] itself;
 * both surfaces render through [rememberCoverImageRequest] directly because their anatomy (a
 * `Crop` + `blur` backdrop, and a zoom/pan full-bleed viewer) doesn't fit [Cover]'s shimmer /
 * coverless / fit-image body. They still carry an entry here because a decode cap is still a
 * per-surface decision, and keeping every surface in one enum is what lets [CoverDimensions] stay a
 * single exhaustive table instead of two.
 */
sealed interface CoverVariant {
    data object LibraryShelfItem : CoverVariant

    data object ExploreRail : CoverVariant

    data object ExploreSeriesCard : CoverVariant

    data object ExploreSearchRow : CoverVariant

    data object HiddenBookRow : CoverVariant

    data object HiddenSeriesStack : CoverVariant

    data object ReadingFeaturedHero : CoverVariant

    data object ReadingRowThumb : CoverVariant

    data object ReadingPickUpNextTile : CoverVariant

    data object ReadingTrendingTile : CoverVariant

    data object ReadingHeroBackdrop : CoverVariant

    data object VerdictSheetJacket : CoverVariant

    data object BookDetailHero : CoverVariant

    data object BookDetailBackdrop : CoverVariant

    data object SheetHeaderJacket : CoverVariant

    data object EditionListRow : CoverVariant

    data object ChooseListsSingleJacket : CoverVariant

    data object ChooseListsStackJacket : CoverVariant

    data object SessionPeekBar : CoverVariant

    data object SessionFocus : CoverVariant

    data object FullScreenViewer : CoverVariant
}
