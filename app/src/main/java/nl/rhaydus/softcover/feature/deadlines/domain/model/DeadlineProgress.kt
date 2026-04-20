package nl.rhaydus.softcover.feature.deadlines.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class DeadlineProgress(
    val deadline: LocalDate,
    val daysRemaining: Long,
    val pagesRemaining: Int,
    val requiredPagesPerDay: Float,
    val initialPagesPerDay: Float,
    val isExpired: Boolean,
    val isOnTrack: Boolean,
    val pagesBehindSchedule: Int,
) {
    val status: DeadlineStatus
        get() = when {
            isExpired -> DeadlineStatus.Expired
            isOnTrack -> DeadlineStatus.OnTrack
            else -> DeadlineStatus.Behind
        }

    companion object {
        fun compute(
            deadline: BookDeadline,
            currentPage: Int,
            totalPages: Int,
            today: LocalDate = LocalDate.now(),
        ): DeadlineProgress {
            val daysRemaining = ChronoUnit.DAYS.between(today, deadline.deadlineDate)
            val pagesRemaining = max(0, totalPages - currentPage)
            val isExpired = daysRemaining < 0 || (daysRemaining == 0L && pagesRemaining > 0)

            val requiredPagesPerDay = when {
                pagesRemaining == 0 -> 0f
                daysRemaining <= 0 -> pagesRemaining.toFloat()
                else -> pagesRemaining.toFloat() / daysRemaining.toFloat()
            }

            val isOnTrack = !isExpired && requiredPagesPerDay <= deadline.initialPagesPerDay

            val expectedPagesRemaining = when {
                daysRemaining <= 0 -> 0f
                else -> deadline.initialPagesPerDay * daysRemaining.toFloat()
            }

            val pagesBehindSchedule = max(
                0,
                (pagesRemaining.toFloat() - expectedPagesRemaining).toInt(),
            )

            return DeadlineProgress(
                deadline = deadline.deadlineDate,
                daysRemaining = daysRemaining,
                pagesRemaining = pagesRemaining,
                requiredPagesPerDay = requiredPagesPerDay,
                initialPagesPerDay = deadline.initialPagesPerDay,
                isExpired = isExpired,
                isOnTrack = isOnTrack,
                pagesBehindSchedule = pagesBehindSchedule,
            )
        }
    }
}
