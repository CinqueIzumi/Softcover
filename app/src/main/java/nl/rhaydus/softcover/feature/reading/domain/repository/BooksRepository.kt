package nl.rhaydus.softcover.feature.reading.domain.repository

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress

interface BooksRepository {
    suspend fun getCurrentlyReadingBooks(userId: Int): List<BookWithProgress>
}