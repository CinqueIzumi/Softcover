package nl.rhaydus.softcover.core.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSource
import nl.rhaydus.softcover.core.personal.data.mapper.toReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository

internal class ReadingJournalHistoryRepositoryImpl(
    private val readingJournalHistoryRemoteDataSource: ReadingJournalHistoryRemoteDataSource,
    private val readingJournalHistoryLocalDataSource: ReadingJournalHistoryLocalDataSource,
) : ReadingJournalHistoryRepository {
    override fun observeReadingJournalHistory(bookId: Int): Flow<List<ReadingJournalEntry>> =
        readingJournalHistoryLocalDataSource.observeByBookId(bookId = bookId).map { entities ->
            val timeZone = TimeZone.currentSystemDefault()

            entities.map { it.toReadingJournalEntry(timeZone = timeZone) }
        }

    // The synced ids are snapshotted before the fetch: a row that turns synced while the fetch is in flight
    // may be absent from the result, so deleting every synced row afterwards would lose it.
    override suspend fun refreshReadingJournalHistory(bookId: Int) {
        val syncedIdsBeforeFetch = readingJournalHistoryLocalDataSource.getSyncedIds(bookId = bookId)

        val remote = readingJournalHistoryRemoteDataSource.getReadingJournalHistory(bookId = bookId)

        readingJournalHistoryLocalDataSource.replaceSynced(
            syncedIdsBeforeFetch = syncedIdsBeforeFetch,
            entities = remote,
        )
    }
}
