package nl.rhaydus.softcover.core.book.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * A hydrated similar-book candidate.
 *
 * [seriesIds] holds every series the book belongs to, because [Book.bookSeries] keeps only the first.
 */
data class SimilarBookCandidate(
    val book: Book,
    val seriesIds: Set<Int>,
)
