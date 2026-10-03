package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle

/** The state slices the "Also reading" rows and "Pick up next" tiles are derived from. */
internal data class BookCardModelsSnapshot(
    val alsoReadingBooks: List<Book>,
    val pickUpNextBooks: List<Book>?,
    val deadlines: Map<Int, BookDeadline>,
    val deadlineCoverOverlays: Map<Int, CoverOverlayUiModel>,
    val dateStyle: DateStyle,
    val failedMutationBookIds: Set<Int>,
)
