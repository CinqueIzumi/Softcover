package nl.rhaydus.softcover.core.book.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate
import nl.rhaydus.softcover.core.book.domain.repository.ProgressHistoryRepository

internal class ProgressHistoryRepositoryImpl(
    private val progressHistoryLocalDataSource: ProgressHistoryLocalDataSource,
) : ProgressHistoryRepository {
    override fun observeAllUpdates(): Flow<List<ProgressUpdate>> = progressHistoryLocalDataSource.observeAllUpdates()
}
