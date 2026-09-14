package nl.rhaydus.softcover.core.component.cover

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Per-[CoverVariant] treatment: shadow elevation, corner radius, shadow tint, and an optional decode
 * cap in pixels. This is a straight port of `EditionImage`'s 29 call sites' individual parameter
 * values, audited one-to-one against today's rendering — **this table changes no pixel**, it only
 * gives each call site's existing choice a name.
 *
 * Several variants land on the same tuple (seven share `0.dp` / `4.dp` / `Color.Unspecified` / `null`)
 * and are kept as separate entries on purpose, not collapsed to one shared constant: the whole point
 * of a per-variant table (`component-contract.md` § 7.2 R2) is that tuning one surface's cover
 * treatment must never silently move another's.
 *
 * The audit also surfaced drift that is **today's behaviour, preserved deliberately** rather than
 * fixed here: [HiddenSeriesStack] sits at 3dp of corner radius while every other unelevated row-style
 * cover sits at 4dp, and Reading's three thumbnail surfaces ([ReadingRowThumb], [ReadingTrendingTile],
 * [ReadingPickUpNextTile]) span 6/8/10dp for what reads as the same kind of tile. Whether to collapse
 * either of those is a design decision for `docs/reference/design-system/`, not something this
 * component-library port makes unilaterally.
 */
internal data class CoverDimensions(
    val elevation: Dp,
    val cornerRadius: Dp,
    val shadowColor: Color,
    val maxDecodePx: Int?,
) {
    companion object {
        private val BlackAlpha60 = Color.Black.copy(alpha = 0.6f)
        private val BlackAlpha50 = Color.Black.copy(alpha = 0.5f)
        private val BlackAlpha35 = Color.Black.copy(alpha = 0.35f)

        fun forVariant(variant: CoverVariant): CoverDimensions = when (variant) {
            CoverVariant.LibraryShelfItem -> CoverDimensions(
                elevation = 6.dp,
                cornerRadius = 10.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = 600,
            )

            CoverVariant.ExploreRail -> CoverDimensions(
                elevation = 6.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ExploreSeriesCard -> CoverDimensions(
                elevation = 4.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ExploreSearchRow -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.HiddenBookRow -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.HiddenSeriesStack -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 3.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ReadingFeaturedHero -> CoverDimensions(
                elevation = 12.dp,
                cornerRadius = 4.dp,
                shadowColor = BlackAlpha60,
                maxDecodePx = null,
            )

            CoverVariant.ReadingRowThumb -> CoverDimensions(
                elevation = 4.dp,
                cornerRadius = 6.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ReadingPickUpNextTile -> CoverDimensions(
                elevation = 4.dp,
                cornerRadius = 10.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ReadingTrendingTile -> CoverDimensions(
                elevation = 4.dp,
                cornerRadius = 8.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ReadingHeroBackdrop -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.VerdictSheetJacket -> CoverDimensions(
                elevation = 6.dp,
                cornerRadius = 16.dp,
                shadowColor = BlackAlpha35,
                maxDecodePx = null,
            )

            // 16dp, the same radius `VerdictSheetJacket` uses — and deliberately its own entry rather
            // than a shared constant. The two happen to agree today; tuning the sheet's jacket must
            // not drag the book page's hero along with it. (Carried over from the
            // `HERO_COVER_CORNER_RADIUS` this replaced, whose comment said exactly that.)
            CoverVariant.BookDetailHero -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 16.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.BookDetailBackdrop -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.SheetHeaderJacket -> CoverDimensions(
                elevation = 4.dp,
                cornerRadius = 4.dp,
                shadowColor = BlackAlpha50,
                maxDecodePx = null,
            )

            CoverVariant.EditionListRow -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.ChooseListsSingleJacket -> CoverDimensions(
                elevation = 4.dp,
                cornerRadius = 4.dp,
                shadowColor = BlackAlpha50,
                maxDecodePx = null,
            )

            CoverVariant.ChooseListsStackJacket -> CoverDimensions(
                elevation = 3.dp,
                cornerRadius = 4.dp,
                shadowColor = BlackAlpha35,
                maxDecodePx = null,
            )

            CoverVariant.SessionPeekBar -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )

            CoverVariant.SessionFocus -> CoverDimensions(
                elevation = 20.dp,
                cornerRadius = 8.dp,
                shadowColor = BlackAlpha50,
                maxDecodePx = null,
            )

            CoverVariant.FullScreenViewer -> CoverDimensions(
                elevation = 0.dp,
                cornerRadius = 4.dp,
                shadowColor = Color.Unspecified,
                maxDecodePx = null,
            )
        }
    }
}
