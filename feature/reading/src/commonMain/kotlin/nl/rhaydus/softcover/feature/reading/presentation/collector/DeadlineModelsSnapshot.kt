package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle

/** The state slices every Reading deadline UI model is derived from. */
internal data class DeadlineModelsSnapshot(
    val featuredBook: Book?,
    val books: List<Book>,
    val deadlines: Map<Int, BookDeadline>,
    val dateStyle: DateStyle,
)
