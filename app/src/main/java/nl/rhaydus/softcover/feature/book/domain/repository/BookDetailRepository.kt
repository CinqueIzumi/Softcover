package nl.rhaydus.softcover.feature.book.domain.repository

import nl.rhaydus.softcover.core.domain.model.Book

interface BookDetailRepository {
    suspend fun fetchBookById(
        id: Int,
        userId: Int,
    ): Book
}