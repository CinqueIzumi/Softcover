package nl.rhaydus.softcover.core.uibinding.release

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.uibinding.date.formatCompactDate
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate

/**
 * A release date as the badge a caller wants to show for it. This does not check whether the date
 * is actually in the future — `Book.isUnreleased` is the one place that decides that, and callers
 * gate on it before reaching for [style] rather than comparing dates themselves.
 */
fun LocalDate.toUnreleasedBadgeUiModel(style: UnreleasedBadgeStyle): BadgeUiModel {
    val (label, size) = when (style) {
        UnreleasedBadgeStyle.Compact -> "Out ${formatCompactDate()}" to BadgeSize.Compact
        UnreleasedBadgeStyle.Prominent -> "Releases ${formatLongDate()}" to BadgeSize.Compact
        UnreleasedBadgeStyle.Featured -> "Arriving ${formatCompactDate()}" to BadgeSize.Regular
    }

    return BadgeUiModel(
        label = label,
        tone = BadgeTone.Primary,
        size = size,
    )
}
