package nl.rhaydus.softcover.core.deadlines.data.repository

import kotlin.math.max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import nl.rhaydus.softcover.core.deadlines.data.datasource.BookDeadlineLocalDataSource
import nl.rhaydus.softcover.core.deadlines.data.mapper.toDomain
import nl.rhaydus.softcover.core.deadlines.data.mapper.toEntity
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

internal class BookDeadlineRepositoryImpl(
    private val localDataSource: BookDeadlineLocalDataSource,
) : BookDeadlineRepository {
    override fun observe(bookId: Int): Flow<BookDeadline?> =
        localDataSource.observe(bookId = bookId).map { it?.toDomain() }

    override fun observeAll(): Flow<List<BookDeadline>> =
        localDataSource.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun setDeadline(
        bookId: Int,
        deadlineDate: LocalDate,
        current: Int,
        total: Int,
        unit: DeadlineUnit,
        today: LocalDate,
    ) {
        val remaining = max(
            0,
            total - current,
        )
        val daysUntilDeadline = today.daysUntil(deadlineDate)

        val initialPerDay = when {
            remaining == 0 -> 0f
            daysUntilDeadline <= 0 -> remaining.toFloat()
            else -> remaining.toFloat() / daysUntilDeadline.toFloat()
        }

        val deadline = BookDeadline(
            bookId = bookId,
            deadlineDate = deadlineDate,
            setAt = today,
            initialPerDay = initialPerDay,
            unit = unit,
        )

        localDataSource.upsert(entity = deadline.toEntity())
    }

    override suspend fun clearDeadline(bookId: Int) {
        localDataSource.delete(bookId = bookId)
    }
}
