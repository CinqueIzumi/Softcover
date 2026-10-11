package nl.rhaydus.softcover.core.uibinding.progress

import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit

/**
 * The reader's in-progress hours/minutes/seconds entry in the progress sheet.
 *
 * [forSeconds] is the position the entry was seeded from. A draft whose seed no longer matches the
 * book's position (a percentage update moved it) is stale and [resolve] discards it, so a feature
 * never shows a time the book has moved past.
 */
data class ProgressTimeDraft(
    val forSeconds: Int,
    val hours: String,
    val minutes: String,
    val seconds: String,
) {
    /** Empty text is kept so the field can be cleared; anything else is clamped into the unit's range. */
    fun withText(
        unit: ProgressTimeUnit,
        text: String,
    ): ProgressTimeDraft {
        val clamped = if (text.isEmpty()) {
            text
        } else {
            val parsed = text.toIntOrNull() ?: return this

            parsed.coerceIn(
                minimumValue = 0,
                maximumValue = unit.maximum,
            ).toString()
        }

        return when (unit) {
            ProgressTimeUnit.HOURS -> copy(hours = clamped)
            ProgressTimeUnit.MINUTES -> copy(minutes = clamped)
            ProgressTimeUnit.SECONDS -> copy(seconds = clamped)
        }
    }

    fun steppedMinutes(delta: Int): ProgressTimeDraft = withText(
        unit = ProgressTimeUnit.MINUTES,
        text = ((minutes.toIntOrNull() ?: 0) + delta).coerceIn(
            minimumValue = 0,
            maximumValue = ProgressTimeUnit.MINUTES.maximum,
        ).toString(),
    )

    companion object {
        fun resolve(
            stored: ProgressTimeDraft?,
            currentSeconds: Int,
        ): ProgressTimeDraft {
            if (stored != null && stored.forSeconds == currentSeconds) return stored

            val hms = currentSeconds.toHoursMinutesSeconds()

            return ProgressTimeDraft(
                forSeconds = currentSeconds,
                hours = hms.hours.toString(),
                minutes = hms.minutes.toString(),
                seconds = hms.seconds.toString(),
            )
        }
    }
}

internal val ProgressTimeUnit.maximum: Int
    get() = when (this) {
        ProgressTimeUnit.HOURS -> Int.MAX_VALUE
        ProgressTimeUnit.MINUTES,
        ProgressTimeUnit.SECONDS -> 59
    }
