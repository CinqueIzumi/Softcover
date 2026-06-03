package nl.rhaydus.softcover.core.domain.model

import kotlin.math.max
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

data class DeadlineProgress(
    val deadline: LocalDate,
    val daysRemaining: Long,
    val unitsRemaining: Int,
    val requiredPerDay: Float,
    val initialPerDay: Float,
    val isExpired: Boolean,
    val isOnTrack: Boolean,
    val unitsBehindSchedule: Int,
    val unit: DeadlineUnit,
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
            current: Int,
            total: Int,
            today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        ): DeadlineProgress {
            val daysRemaining = today.daysUntil(deadline.deadlineDate).toLong()
            val unitsRemaining = max(
                0,
                total - current,
            )
            val isExpired = daysRemaining < 0 || (daysRemaining == 0L && unitsRemaining > 0)

            val requiredPerDay = when {
                unitsRemaining == 0 -> 0f
                daysRemaining <= 0 -> unitsRemaining.toFloat()
                else -> unitsRemaining.toFloat() / daysRemaining.toFloat()
            }

            val isOnTrack = isExpired.not() && requiredPerDay <= deadline.initialPerDay

            val expectedRemaining = when {
                daysRemaining <= 0 -> 0f
                else -> deadline.initialPerDay * daysRemaining.toFloat()
            }

            val unitsBehindSchedule = max(
                0,
                (unitsRemaining.toFloat() - expectedRemaining).toInt(),
            )

            return DeadlineProgress(
                deadline = deadline.deadlineDate,
                daysRemaining = daysRemaining,
                unitsRemaining = unitsRemaining,
                requiredPerDay = requiredPerDay,
                initialPerDay = deadline.initialPerDay,
                isExpired = isExpired,
                isOnTrack = isOnTrack,
                unitsBehindSchedule = unitsBehindSchedule,
                unit = deadline.unit,
            )
        }
    }
}
