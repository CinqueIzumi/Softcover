package nl.rhaydus.softcover.feature.book.domain.repository

import nl.rhaydus.softcover.core.domain.model.Book

interface BookDetailRepository {
    suspend fun fetchBookById(id: Int): Book

    suspend fun markBookAsWantToRead(bookId: Int): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)
}