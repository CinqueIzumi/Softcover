package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Per-[BadgeVariant] pad, shape, label weight/tracking and leading-icon size: a straight port of
 * today's `Surface` + `Text`/`Icon` call sites, audited one-to-one against today's rendering — this
 * table changes no pixel, it only gives each call site's existing choice a name. [fontWeight] `null`
 * keeps `labelSmall`'s own weight; [letterSpacing] [TextUnit.Unspecified] keeps its own tracking.
 * [Standard] and [FeaturedRelease] never carry an icon today, so their [iconSize]/[iconGap] are a
 * fallback rather than an audited value.
 */
internal data class BadgeDimensions(
    val horizontal: Dp,
    val vertical: Dp,
    val shape: Shape,
    val fontWeight: FontWeight?,
    val letterSpacing: TextUnit,
    val iconSize: Dp,
    val iconGap: Dp,
) {
    companion object {
        private val PillShape = RoundedCornerShape(6.dp)
        private val FallbackIconSize = 11.dp
        private val FallbackIconGap = 4.dp

        fun forVariant(variant: BadgeVariant): BadgeDimensions = when (variant) {
            BadgeVariant.Standard -> BadgeDimensions(
                horizontal = 6.dp,
                vertical = 2.dp,
                shape = PillShape,
                fontWeight = null,
                letterSpacing = TextUnit.Unspecified,
                iconSize = FallbackIconSize,
                iconGap = FallbackIconGap,
            )

            BadgeVariant.FeaturedRelease -> BadgeDimensions(
                horizontal = 8.dp,
                vertical = 4.dp,
                shape = PillShape,
                fontWeight = null,
                letterSpacing = TextUnit.Unspecified,
                iconSize = FallbackIconSize,
                iconGap = FallbackIconGap,
            )

            BadgeVariant.GridCountdown -> BadgeDimensions(
                horizontal = 8.dp,
                vertical = 4.dp,
                shape = PillShape,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = TextUnit.Unspecified,
                iconSize = 11.dp,
                iconGap = 4.dp,
            )

            BadgeVariant.OwnedCover -> BadgeDimensions(
                horizontal = 8.dp,
                vertical = 4.dp,
                shape = RoundedCornerShape(8.dp),
                fontWeight = FontWeight.Bold,
                letterSpacing = TextUnit.Unspecified,
                iconSize = 14.dp,
                iconGap = 4.dp,
            )

            BadgeVariant.TrackingTag -> BadgeDimensions(
                horizontal = 9.dp,
                vertical = 4.dp,
                shape = RoundedCornerShape(percent = 50),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                iconSize = FallbackIconSize,
                iconGap = FallbackIconGap,
            )
        }
    }
}
