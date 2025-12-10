package nl.rhaydus.softcover.feature.reading.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
    private val bookRemoteDataSource: BookRemoteDataSource,
) : BooksRepository {
    override fun getCurrentlyReadingBooks(userId: Int): Flow<List<BookWithProgress>> {
        return bookRemoteDataSource.getCurrentlyReadingBooks(userId = userId)
    }

    override suspend fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    ) {
         bookRemoteDataSource.updateBookProgress(
            book = book,
            newPage = newPage,
        )
    }

    override suspend fun markBookAsRead(book: BookWithProgress) {
        bookRemoteDataSource.markBookAsRead(book = book)
    }

    override suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
        userId: Int,
    ) {
        bookRemoteDataSource.updateBookEdition(
            userBookId = userBookId,
            newEditionId = newEditionId,
            userId = userId,
        )
    }

    override suspend fun refreshCurrentlyReadingBooks(userId: Int) {
        bookRemoteDataSource.refreshCurrentlyReadingBooks(userId = userId)
    }
}
