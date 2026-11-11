package nl.rhaydus.softcover.core.profile.domain.model

/**
 * The pages read in a range, as credited per day, with the books behind them.
 *
 * [finishedBookCount] and [stoppedBookCount] count the Read and Did not finish books that ended in the
 * range; [hasInProgressPages] is true when some credited pages belong to neither. [pagesPerDay] is null
 * when nothing was read or the average rounds below one page.
 */
data class PagesRead(
    val total: Int,
    val finishedBookCount: Int,
    val stoppedBookCount: Int,
    val hasInProgressPages: Boolean,
    val pagesPerDay: Int?,
)
