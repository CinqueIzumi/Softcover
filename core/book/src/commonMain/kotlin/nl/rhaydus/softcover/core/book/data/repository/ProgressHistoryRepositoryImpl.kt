package nl.rhaydus.softcover.core.book.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.book.domain.repository.ProgressHistoryRepository
import kotlin.time.Instant

internal class ProgressHistoryRepositoryImpl(
    private val progressHistoryLocalDataSource: ProgressHistoryLocalDataSource,
) : ProgressHistoryRepository {
    override fun observeAllUpdates(): Flow<List<ProgressUpdate>> =
        progressHistoryLocalDataSource.observeAllUpdates().map { rows ->
            rows.map {
                ProgressUpdate(
                    bookId = it.bookId,
                    at = Instant.fromEpochMilliseconds(it.actionAtEpochMillis),
                    pages = it.pages,
                    pagesRead = it.pagesRead,
                )
            }
        }
}
