package nl.rhaydus.softcover.feature.reading.presentation.mapper

import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.BannerVariant
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import kotlin.math.ceil

internal fun planTodayNudgeFor(
    progress: DeadlineProgress?,
    visible: Boolean,
): BannerUiModel? {
    progress ?: return null
    if (progress.isExpired) return null
    if (progress.unitsRemaining == 0) return null
    if (progress.daysRemaining <= 0L) return null

    val needed = ceil(progress.requiredPerDay.toDouble()).toInt()
    if (needed <= 0) return null

    val message = when (progress.unit) {
        DeadlineUnit.PAGES -> "Read $needed pages today to stay on pace."
        DeadlineUnit.SECONDS -> {
            val minutes = (needed + 59) / 60
            "Listen $minutes min today to stay on pace."
        }
    }

    return BannerUiModel(
        message = message,
        variant = BannerVariant.Editorial,
        visible = visible,
    )
}
