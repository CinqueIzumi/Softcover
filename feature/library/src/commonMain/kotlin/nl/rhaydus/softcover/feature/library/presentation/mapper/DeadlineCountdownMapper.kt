package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus

/**
 * A deadline's progress as Library's grid-cover countdown overlay: "N day(s)" while tracking,
 * "Expired" once past due. [DeadlineStatus.Behind] keeps [BadgeTone.ErrorContainer] rather than the
 * countdown's own [BadgeTone.Primary] — the app reserves error for "needs attention" everywhere
 * else. [CoverOverlayUiModel.grayscale] follows the same [DeadlineStatus.Expired] check.
 */
internal fun DeadlineProgress.toCountdownCoverOverlayUiModel(): CoverOverlayUiModel {
    val tone = when (status) {
        DeadlineStatus.OnTrack -> BadgeTone.Primary
        DeadlineStatus.Behind -> BadgeTone.ErrorContainer
        DeadlineStatus.Expired -> BadgeTone.Muted
    }

    val label = if (status == DeadlineStatus.Expired) {
        "Expired"
    } else {
        val days = daysRemaining
        "$days ${if (days == 1L) "day" else "days"}"
    }

    return CoverOverlayUiModel(
        badge = BadgeUiModel(
            label = label,
            tone = tone,
            variant = BadgeVariant.GridCountdown,
            icon = SoftcoverIcon.DateRange,
        ),
        variant = CoverOverlayVariant.LibraryCountdown,
        grayscale = status == DeadlineStatus.Expired,
    )
}
