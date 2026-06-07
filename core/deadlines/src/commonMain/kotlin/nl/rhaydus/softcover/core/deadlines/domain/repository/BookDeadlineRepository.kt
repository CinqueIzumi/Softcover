package nl.rhaydus.softcover.core.deadlines.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

interface BookDeadlineRepository {
    fun observe(bookId: Int): Flow<BookDeadline?>

    fun observeAll(): Flow<List<BookDeadline>>

    suspend fun setDeadline(
        bookId: Int,
        deadlineDate: LocalDate,
        current: Int,
        total: Int,
        unit: DeadlineUnit,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    )

    suspend fun clearDeadline(bookId: Int)
}
