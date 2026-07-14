package nl.rhaydus.softcover.feature.book_detail.domain.repository

import nl.rhaydus.softcover.feature.book_detail.domain.model.ReadingJournalEntry

interface ReadingJournalHistoryRepository {
    suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry>
}
