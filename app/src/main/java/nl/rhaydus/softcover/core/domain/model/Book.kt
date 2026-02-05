package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

data class Book(
    val id: Int,
    val title: String,
    val editions: List<BookEdition>,
    val defaultEdition: BookEdition?,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    val authors: List<Author>,

    val userBook: UserBook?,
    val userBookRead: UserBookRead?,
) {
    val status: BookStatus
        get() = userBook?.status ?: BookStatus.None

    val currentEdition: BookEdition
        get() {
            val userEditionId = userBook?.editionId

            val matchingEdition = editions.firstOrNull { it.id == userEditionId }

            return matchingEdition ?: defaultEdition ?: editions.first()
        }
}