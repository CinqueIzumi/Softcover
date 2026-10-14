package nl.rhaydus.softcover.core.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry

interface ReadingJournalHistoryRepository {
    fun observeReadingJournalHistory(bookId: Int): Flow<List<ReadingJournalEntry>>

    suspend fun refreshReadingJournalHistory(bookId: Int)
}
