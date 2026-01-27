package nl.rhaydus.softcover.feature.book.data.repository

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSource
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import javax.inject.Inject

class BookDetailRepositoryImpl @Inject constructor(
    private val bookDetailRemoteDataSource: BookDetailRemoteDataSource,
) : BookDetailRepository {
    override suspend fun fetchBookById(
        id: Int,
        userId: Int,
    ): Book {
        return bookDetailRemoteDataSource.fetchBookById(
            id = id,
            userId = userId,
        )
    }
}