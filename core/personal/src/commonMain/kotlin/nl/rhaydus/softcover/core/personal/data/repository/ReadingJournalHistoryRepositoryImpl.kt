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

    override suspend fun refreshReadingJournalHistory(bookId: Int) {
        val remote = readingJournalHistoryRemoteDataSource.getReadingJournalHistory(bookId = bookId)

        readingJournalHistoryLocalDataSource.replaceSyncedForBook(
            bookId = bookId,
            entities = remote,
        )
    }

    override suspend fun refreshAllReadingJournalHistory() {
        val remote = readingJournalHistoryRemoteDataSource.getAllReadingJournalHistory()

        readingJournalHistoryLocalDataSource.replaceAllSynced(entities = remote)
    }
}
