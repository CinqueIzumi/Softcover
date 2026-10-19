package nl.rhaydus.softcover.core.uibinding.release

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.uibinding.date.formatLongDate

fun LocalDate.toCompactUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Out ${formatCompactDate()}",
    size = BadgeSize.Compact,
)

fun LocalDate.toProminentUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Releases ${formatLongDate()}",
    size = BadgeSize.Compact,
)

fun LocalDate.toEditionUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Edition out ${formatCompactDate()}",
    size = BadgeSize.Compact,
)

fun LocalDate.toFeaturedUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Arriving ${formatCompactDate()}",
    size = BadgeSize.Regular,
)

private fun unreleasedBadge(
    label: String,
    size: BadgeSize,
): BadgeUiModel = BadgeUiModel(
    label = label,
    tone = BadgeTone.Primary,
    size = size,
)

private fun LocalDate.formatCompactDate(): String {
    val formatter = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day(Padding.NONE)
    }

    val base = formatter.format(this)
    val now = currentLocalDate()

    return if (year == now.year) base else "$base, $year"
}
