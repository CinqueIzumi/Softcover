package nl.rhaydus.softcover.core.component.badge

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Per-[CoverOverlayVariant] badge placement: a straight port of today's two call sites' alignment
 * and padding — this table changes no pixel, it only gives each call site's existing choice a name.
 */
internal data class CoverOverlayDimensions(
    val alignment: Alignment,
    val padding: Dp,
) {
    companion object {
        fun forVariant(variant: CoverOverlayVariant): CoverOverlayDimensions = when (variant) {
            CoverOverlayVariant.ReadingStatus -> CoverOverlayDimensions(
                alignment = Alignment.TopEnd,
                padding = 6.dp,
            )

            CoverOverlayVariant.LibraryCountdown -> CoverOverlayDimensions(
                alignment = Alignment.TopStart,
                padding = 8.dp,
            )
        }
    }
}
