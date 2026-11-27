package nl.rhaydus.softcover.core.profile.domain.model

/**
 * How the books of a stats range waited on the reading list. [waitDays] is the median wait over the
 * [waitedCount] books that were added and later started, and is null when there are none.
 */
data class ReadingListStats(
    val addedCount: Int = 0,
    val waitDays: Int? = null,
    val waitedCount: Int = 0,
)
