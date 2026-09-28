package nl.rhaydus.softcover.core.component.selection

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Per-[SelectionMarkVariant] size and check-icon size: a straight port of
 * `SelectionCircleIndicator`'s and `SelectedCheckBadge`'s call sites, audited one-to-one against
 * today's rendering — this table changes no pixel. [SelectionMarkVariant.OnCover]'s 18dp and
 * [SelectionMarkVariant.RowLeading]'s 16dp restate their call sites' "fill the circle, then pad
 * 4dp" as a plain icon size, which is pixel-identical and keeps this table in one shape with
 * [SelectionMarkVariant.EditionCard]'s literal 15dp, which has no clean 4dp inset to state instead.
 */
internal data class SelectionMarkDimensions(
    val size: Dp,
    val iconSize: Dp,
) {
    companion object {
        fun forVariant(variant: SelectionMarkVariant): SelectionMarkDimensions = when (variant) {
            SelectionMarkVariant.OnCover -> SelectionMarkDimensions(
                size = 26.dp,
                iconSize = 18.dp,
            )

            SelectionMarkVariant.RowLeading -> SelectionMarkDimensions(
                size = 24.dp,
                iconSize = 16.dp,
            )

            SelectionMarkVariant.EditionCard -> SelectionMarkDimensions(
                size = 24.dp,
                iconSize = 15.dp,
            )
        }
    }
}
