package nl.rhaydus.softcover.core.personal.domain.repository

import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry

interface ReadingJournalHistoryRepository {
    suspend fun getReadingJournalHistory(bookId: Int): List<ReadingJournalEntry>
}
