package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover

/**
 * The inputs every cover on the book page resolves from. [displayedEdition] is read off the state's
 * computed getter rather than recomputed here, which is safe because a collector runs off the
 * composition.
 */
internal data class CoverModelsSnapshot(
    val book: Book?,
    val displayedEdition: BookEdition?,
    val editions: List<BookEdition>,
    val initialCover: BookInitialCover?,
    val loadingBookDetails: Boolean,
    val bookId: Int?,
    val transitionSurface: String?,
)
