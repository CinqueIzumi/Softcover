package nl.rhaydus.softcover.feature.deadlines.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.deadlines.data.datasource.BookDeadlineLocalDataSource
import nl.rhaydus.softcover.feature.deadlines.data.mapper.toDomain
import nl.rhaydus.softcover.feature.deadlines.data.mapper.toEntity
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.repository.BookDeadlineRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

class BookDeadlineRepositoryImpl(
    private val localDataSource: BookDeadlineLocalDataSource,
) : BookDeadlineRepository {
    override fun observe(bookId: Int): Flow<BookDeadline?> =
        localDataSource.observe(bookId = bookId).map { it?.toDomain() }

    override fun observeAll(): Flow<List<BookDeadline>> =
        localDataSource.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun setDeadline(
        bookId: Int,
        deadlineDate: LocalDate,
        currentPage: Int,
        totalPages: Int,
        today: LocalDate,
    ) {
        val pagesRemaining = max(0, totalPages - currentPage)
        val daysUntilDeadline = ChronoUnit.DAYS.between(today, deadlineDate)

        val initialPagesPerDay = when {
            pagesRemaining == 0 -> 0f
            daysUntilDeadline <= 0 -> pagesRemaining.toFloat()
            else -> pagesRemaining.toFloat() / daysUntilDeadline.toFloat()
        }

        val deadline = BookDeadline(
            bookId = bookId,
            deadlineDate = deadlineDate,
            setAt = today,
            initialPagesPerDay = initialPagesPerDay,
        )

        localDataSource.upsert(entity = deadline.toEntity())
    }

    override suspend fun clearDeadline(bookId: Int) {
        localDataSource.delete(bookId = bookId)
    }
}
