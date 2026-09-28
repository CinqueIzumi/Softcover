package nl.rhaydus.softcover.core.component.badge

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/** [CoverOverlay]'s corner pad, matching the badge's own [BadgeDimensions] horizontal pad for [size]. */
internal object CoverOverlayDimensions {
    fun insetFor(size: BadgeSize): Dp =
        BadgeDimensions.paddingFor(size).calculateLeftPadding(LayoutDirection.Ltr)
}
