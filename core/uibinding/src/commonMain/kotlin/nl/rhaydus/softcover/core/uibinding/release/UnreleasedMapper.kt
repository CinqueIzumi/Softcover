package nl.rhaydus.softcover.core.uibinding.release

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import nl.rhaydus.softcover.core.uibinding.date.formatCompactDate
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate

/**
 * A release date as the badge a caller wants to show for it. `Compact` and `Prominent` reproduce
 * today's `UnreleasedBadge` exactly. `Featured` reproduces the hand-rolled badge at
 * `feature/explore/.../ExploreShelf.kt:243` (`"Arriving ${releaseDate.formatCompactRelease()}"`,
 * 8dp/4dp padding), which this stage folds into the component behind [BadgeVariant.FeaturedRelease].
 */
fun LocalDate.toUnreleasedBadgeUiModel(style: UnreleasedBadgeStyle): BadgeUiModel {
    val (label, variant) = when (style) {
        UnreleasedBadgeStyle.Compact -> "Out ${formatCompactDate()}" to BadgeVariant.Standard
        UnreleasedBadgeStyle.Prominent -> "Releases ${formatLongDate()}" to BadgeVariant.Standard
        UnreleasedBadgeStyle.Featured -> "Arriving ${formatCompactDate()}" to BadgeVariant.FeaturedRelease
    }

    return BadgeUiModel(
        label = label,
        tone = BadgeTone.Release,
        variant = variant,
    )
}
