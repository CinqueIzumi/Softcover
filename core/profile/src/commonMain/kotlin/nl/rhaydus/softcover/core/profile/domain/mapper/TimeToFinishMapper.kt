package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.profile.domain.model.LengthBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadDuration
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish

/** Reads with an unknown length count in the overall median only. */
internal fun List<ReadDuration>.toTimeToFinish(excludedReadCount: Int): TimeToFinish = TimeToFinish(
    readCount = size,
    medianDays = map { it.days }.medianOrNull(),
    bandMedianDays = mapNotNull { duration ->
        LengthBand.fromPages(duration.pages)?.let { band -> band to duration.days }
    }
        .groupBy(
            { it.first },
            { it.second },
        )
        .mapValues { (_, days) -> days.median() },
    excludedReadCount = excludedReadCount,
)

private fun List<Int>.medianOrNull(): Int? = if (isEmpty()) null else median()

/** An even count averages the middle two, rounded half up. */
private fun List<Int>.median(): Int {
    val sorted = sorted()
    val middle = sorted.size / 2

    return if (sorted.size % 2 == 1) {
        sorted[middle]
    } else {
        (sorted[middle - 1] + sorted[middle] + 1) / 2
    }
}
