package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.profile.domain.model.DidNotFinishStats
import nl.rhaydus.softcover.core.profile.domain.model.StopQuarter

/**
 * Aggregates the books of [this] stopped in [range]. Under [StatsRange.Bounded] a book without a
 * stop date is left out; under [StatsRange.Unbounded] it counts like any other.
 */
internal fun List<Book>.toDidNotFinishStats(
    range: StatsRange,
    userTagsByBookId: Map<Int, List<UserTag>>,
): DidNotFinishStats {
    val inRange = filter { it.isStoppedIn(range) }
    val quarters = inRange.map { it.toStopPoint().quarter }
    val counts = quarters.filterNotNull().groupingBy { it }.eachCount()

    return DidNotFinishStats(
        bookCount = inRange.size,
        quarterCounts = StopQuarter.entries.associateWith { counts[it] ?: 0 },
        unknownStopCount = quarters.count { it == null },
        genres = inRange.toGenreBreakdown(userTagsByBookId),
    )
}

private fun Book.isStoppedIn(range: StatsRange): Boolean = when (range) {
    StatsRange.Unbounded -> true
    is StatsRange.Bounded -> userBook?.stopDate?.let { it in range } == true
}
