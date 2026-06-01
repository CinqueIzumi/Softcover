package nl.rhaydus.softcover.core.deadlines.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import java.time.LocalDate

interface BookDeadlineRepository {
    fun observe(bookId: Int): Flow<BookDeadline?>

    fun observeAll(): Flow<List<BookDeadline>>

    suspend fun setDeadline(
        bookId: Int,
        deadlineDate: LocalDate,
        current: Int,
        total: Int,
        unit: DeadlineUnit,
        today: LocalDate = LocalDate.now(),
    )

    suspend fun clearDeadline(bookId: Int)
}
