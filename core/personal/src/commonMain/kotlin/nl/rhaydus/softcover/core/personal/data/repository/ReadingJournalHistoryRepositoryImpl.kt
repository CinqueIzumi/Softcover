package nl.rhaydus.softcover.core.personal.data.repository

import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSource
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository

internal class ReadingJournalHistoryRepositoryImpl(
    private val readingJournalHistoryRemoteDataSource: ReadingJournalHistoryRemoteDataSource,
) : ReadingJournalHistoryRepository {
    override suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry> =
        readingJournalHistoryRemoteDataSource.getReadingJournalHistory(bookId = bookId)
}
