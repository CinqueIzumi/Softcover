package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle

/** The state fields the Library screen's deadline progress and UI models are derived from. */
internal data class DeadlineModelsSnapshot(
    val booksByTab: Map<String, List<Book>>,
    val deadlines: Map<Int, BookDeadline>,
    val dateStyle: DateStyle,
)
