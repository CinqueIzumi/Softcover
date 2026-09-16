package nl.rhaydus.softcover.core.component.badge

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Per-[BadgeVariant] padding: a straight port of today's two `Surface` + `Text` call sites' padding
 * values, audited one-to-one against today's rendering — **this table changes no pixel**, it only
 * gives each call site's existing choice a name. See [BadgeVariant] for why the two are kept apart
 * rather than collapsed to one shared constant.
 */
internal data class BadgeDimensions(
    val horizontal: Dp,
    val vertical: Dp,
) {
    companion object {
        fun forVariant(variant: BadgeVariant): BadgeDimensions = when (variant) {
            BadgeVariant.Standard -> BadgeDimensions(
                horizontal = 6.dp,
                vertical = 2.dp,
            )

            BadgeVariant.FeaturedRelease -> BadgeDimensions(
                horizontal = 8.dp,
                vertical = 4.dp,
            )
        }
    }
}
