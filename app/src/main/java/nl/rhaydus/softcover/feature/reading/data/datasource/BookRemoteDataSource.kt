package nl.rhaydus.softcover.feature.reading.data.datasource

import nl.rhaydus.softcover.core.domain.model.Book

interface BookRemoteDataSource {
    suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    )

    suspend fun markBookAsRead(book: Book)

    suspend fun updateBookEdition(
        userBookId: Int,
        newEditionId: Int,
    )
}