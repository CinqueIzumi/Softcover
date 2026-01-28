package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

data class Book(
    val id: Int,
    val status: BookStatus,
    val title: String,
    val editions: List<BookEdition>,
    val defaultEdition: BookEdition?,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    val authors: List<Author>,
    val currentPage: Int?,
    val progress: Float?,
    val editionId: Int?,
    val userBookId: Int?,
    val userBookReadId: Int?,
    val startedAt: String?,
    val finishedAt: String?,
) {
    val currentEdition: BookEdition
        get() {
            return editions.firstOrNull { it.id == editionId } ?: defaultEdition ?: editions.first()
        }
}