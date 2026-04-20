package nl.rhaydus.softcover.feature.deadlines.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import java.time.LocalDate

interface BookDeadlineRepository {
    fun observe(bookId: Int): Flow<BookDeadline?>

    fun observeAll(): Flow<List<BookDeadline>>

    suspend fun setDeadline(
        bookId: Int,
        deadlineDate: LocalDate,
        currentPage: Int,
        totalPages: Int,
        today: LocalDate = LocalDate.now(),
    )

    suspend fun clearDeadline(bookId: Int)
}
