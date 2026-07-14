package nl.rhaydus.softcover.feature.book_detail.data.repository

import nl.rhaydus.softcover.feature.book_detail.data.datasource.ReadingJournalHistoryRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.feature.book_detail.domain.repository.ReadingJournalHistoryRepository

internal class ReadingJournalHistoryRepositoryImpl(
    private val readingJournalHistoryRemoteDataSource: ReadingJournalHistoryRemoteDataSource,
) : ReadingJournalHistoryRepository {
    override suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry> =
        readingJournalHistoryRemoteDataSource.getReadingJournalHistory(bookId = bookId)
}
