package nl.rhaydus.softcover.feature.reading.presentation.screen

import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import kotlin.math.ceil

internal fun planTodayNudgeFor(progress: DeadlineProgress?): String? {
    progress ?: return null
    if (progress.isExpired) return null
    if (progress.unitsRemaining == 0) return null
    if (progress.daysRemaining <= 0L) return null

    val needed = ceil(progress.requiredPerDay.toDouble()).toInt()
    if (needed <= 0) return null

    return when (progress.unit) {
        DeadlineUnit.PAGES -> "Read $needed pages today to stay on pace."
        DeadlineUnit.SECONDS -> {
            val minutes = (needed + 59) / 60
            "Listen $minutes min today to stay on pace."
        }
    }
}
