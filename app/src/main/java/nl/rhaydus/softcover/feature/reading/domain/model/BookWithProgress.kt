package nl.rhaydus.softcover.feature.reading.domain.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

data class BookWithProgress(
    val book: Book,
    val currentPage: Int,
    val progress: Float,
    val editionId: Int,
    val userBookId: Int,
    val userBookReadId: Int,
    val startedAt: String?,
    val finishedAt: String?,
) {
    val currentEdition: BookEdition
        get() {
            return book.editions.first { it.id == editionId }
        }
}