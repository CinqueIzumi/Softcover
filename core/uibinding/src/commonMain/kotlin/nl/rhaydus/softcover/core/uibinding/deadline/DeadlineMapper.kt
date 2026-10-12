package nl.rhaydus.softcover.core.uibinding.deadline

import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineRowUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import kotlin.math.ceil

/** A deadline's progress as a badge: the status label as its text, toned by status. */
fun DeadlineProgress.toBadgeUiModel(): BadgeUiModel {
    val tone = when (status) {
        DeadlineStatus.OnTrack -> BadgeTone.PrimaryContainer
        DeadlineStatus.Behind -> BadgeTone.ErrorContainer
        DeadlineStatus.Expired -> BadgeTone.Neutral
    }

    return BadgeUiModel(
        label = status.label,
        tone = tone,
    )
}

/** A deadline's progress as a cover overlay, greyscaled exactly on [DeadlineStatus.Expired]. */
fun DeadlineProgress.toCoverOverlayUiModel(): CoverOverlayUiModel =
    CoverOverlayUiModel(
        badge = toBadgeUiModel(),
        corner = CoverOverlayCorner.TopEnd,
        grayscale = status == DeadlineStatus.Expired,
    )

/**
 * A deadline's progress as the summary line's text, in [dateStyle]'s date format. The pace is the
 * status label once the deadline is expired, otherwise the required pace rounded up to a whole unit.
 */
fun DeadlineProgress.toDeadlineSummaryUiModel(
    dateStyle: DateStyle,
): DeadlineSummaryUiModel {
    val dateText = dateStyle.formatter.format(deadline)

    val paceText = if (status == DeadlineStatus.Expired) {
        status.label
    } else {
        formatPacePerDay(
            amount = ceil(requiredPerDay).toInt(),
            unit = unit,
        )
    }

    return DeadlineSummaryUiModel(text = "$dateText • $paceText")
}

/**
 * A deadline's progress as the deadline card: the "Finish by" date in [dateStyle], the status badge
 * and the goal sentence. An expired deadline says how long ago it passed; otherwise the goal states
 * the pace needed for the remaining days, prefixed by how far behind the reader is when they are.
 */
fun DeadlineProgress.toDeadlineRowUiModel(dateStyle: DateStyle): DeadlineRowUiModel =
    DeadlineRowUiModel(
        title = "Finish by ${dateStyle.formatter.format(deadline)}",
        badge = toBadgeUiModel(),
        goal = goalText(),
        isExpired = isExpired,
    )

private fun DeadlineProgress.goalText(): String {
    if (isExpired) {
        val daysPast = -daysRemaining

        return "Deadline passed $daysPast ${if (daysPast == 1L) "day" else "days"} ago."
    }

    val verb = if (unit == DeadlineUnit.SECONDS) "Listen to" else "Read"
    val dayLabel = if (daysRemaining == 1L) "day" else "days"
    val pace = formatPacePerDay(
        amount = ceil(requiredPerDay).toInt(),
        unit = unit,
    )
    val base = "$verb $pace for the next $daysRemaining $dayLabel to finish on time."

    if (isOnTrack || unitsBehindSchedule <= 0) return base

    val behind = formatUnitAmount(
        amount = unitsBehindSchedule,
        unit = unit,
    )

    return "You're $behind behind schedule. $base"
}
