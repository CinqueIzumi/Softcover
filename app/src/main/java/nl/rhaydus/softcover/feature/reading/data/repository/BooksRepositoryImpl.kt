package nl.rhaydus.softcover.feature.reading.data.repository

import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import javax.inject.Inject

class BooksRepositoryImpl @Inject constructor(
    private val bookRemoteDataSource: BookRemoteDataSource,
) : BooksRepository {
    override suspend fun getCurrentlyReadingBooks(userId: Int): List<BookWithProgress> {
        return bookRemoteDataSource.getCurrentlyReadingBooks(userId = userId)
    }

    override suspend fun updateBookProgress(
        book: BookWithProgress,
        newPage: Int,
    ): BookWithProgress {
        return bookRemoteDataSource.updateBookProgress(
            book = book,
            newPage = newPage,
        )
    }
}