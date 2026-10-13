package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle

/**
 * The state slices every Reading deadline UI model is derived from.
 *
 * @property today Carried alongside the state slices, rather than read at collect time, so a day
 * rollover with no other state change still produces a new snapshot and re-evaluates
 * [dismissedPlanTodayByBook].
 */
internal data class DeadlineModelsSnapshot(
    val featuredBook: Book?,
    val books: List<Book>,
    val deadlines: Map<Int, BookDeadline>,
    val dateStyle: DateStyle,
    val dismissedPlanTodayByBook: Map<Int, String>,
    val today: LocalDate,
)
