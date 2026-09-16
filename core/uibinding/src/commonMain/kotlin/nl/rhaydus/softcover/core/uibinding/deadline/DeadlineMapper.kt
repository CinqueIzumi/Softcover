package nl.rhaydus.softcover.core.uibinding.deadline

import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

/**
 * A deadline's progress as the badge that should represent it — the status label as the badge's
 * text, and the tone `DeadlineBadge` used to pick per-status colours before this became a mapper
 * output instead of a `when` inline in the component.
 */
fun DeadlineProgress.toBadgeUiModel(): BadgeUiModel {
    val tone = when (status) {
        DeadlineStatus.OnTrack -> BadgeTone.OnTrack
        DeadlineStatus.Behind -> BadgeTone.Behind
        DeadlineStatus.Expired -> BadgeTone.Expired
    }

    return BadgeUiModel(
        label = status.label,
        tone = tone,
        variant = BadgeVariant.Standard,
    )
}

/**
 * A deadline's progress as a cover overlay — greyscaled exactly on [DeadlineStatus.Expired], the
 * same condition `DeadlineCoverOverlay` checked directly.
 */
fun DeadlineProgress.toCoverOverlayUiModel(): CoverOverlayUiModel =
    CoverOverlayUiModel(
        badge = toBadgeUiModel(),
        grayscale = status == DeadlineStatus.Expired,
    )

/**
 * A deadline's progress as the summary line's date and pace strings, in [dateStyle]'s date format
 * and rendered for [tone]. The pace text is lifted verbatim from `DeadlineSummaryLine`: the status
 * label once the deadline is expired, otherwise the required pace rounded up to a whole unit.
 */
fun DeadlineProgress.toDeadlineSummaryUiModel(
    dateStyle: DateStyle,
    tone: DeadlineSummaryTone,
): DeadlineSummaryUiModel {
    val dateText = dateStyle.formatter.format(deadline)

    val paceText = if (status == DeadlineStatus.Expired) {
        status.label
    } else {
        val pace = ceilToInt(requiredPerDay)
        when (unit) {
            DeadlineUnit.PAGES -> {
                val pageLabel = if (pace == 1) "page" else "pages"
                "$pace $pageLabel/day"
            }

            DeadlineUnit.SECONDS -> "${secondsToHm(pace)}/day"
        }
    }

    return DeadlineSummaryUiModel(
        dateText = dateText,
        paceText = paceText,
        tone = tone,
    )
}

private fun ceilToInt(value: Float): Int {
    val rounded = value.toInt()

    return if (value > rounded) rounded + 1 else rounded
}
