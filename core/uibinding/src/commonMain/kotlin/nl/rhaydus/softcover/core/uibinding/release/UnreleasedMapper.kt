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

/** A short "Out <date>" badge in the compact size, for a cover overlay. */
fun LocalDate.toCompactUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Out ${formatCompactDate()}",
    size = BadgeSize.Compact,
)

/** A "Releases <long date>" badge in the compact size, for a book's detail header. */
fun LocalDate.toProminentUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Releases ${formatLongDate()}",
    size = BadgeSize.Compact,
)

/** An "Arriving <date>" badge in the regular size, for a featured release. */
fun LocalDate.toFeaturedUnreleasedBadgeUiModel(): BadgeUiModel = unreleasedBadge(
    label = "Arriving ${formatCompactDate()}",
    size = BadgeSize.Regular,
)

/**
 * Shared by the three mappers above; none checks that the date is in the future — `Book.isUnreleased`
 * decides that, and callers gate on it before mapping.
 */
private fun unreleasedBadge(
    label: String,
    size: BadgeSize,
): BadgeUiModel = BadgeUiModel(
    label = label,
    tone = BadgeTone.Primary,
    size = size,
)

/**
 * "Sep 2", with ", <year>" appended only when [LocalDate.year] is not the current year.
 */
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
