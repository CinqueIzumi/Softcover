package nl.rhaydus.softcover.feature.book.data.datasource

import nl.rhaydus.softcover.core.domain.model.Book

interface BookDetailRemoteDataSource {
    suspend fun fetchBookById(
        id: Int,
        userId: Int,
    ): Book
}