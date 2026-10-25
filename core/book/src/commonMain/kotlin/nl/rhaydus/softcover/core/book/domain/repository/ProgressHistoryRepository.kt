package nl.rhaydus.softcover.core.book.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.model.ProgressUpdate

interface ProgressHistoryRepository {
    /** Every recorded progress update across all books, pending ones included. */
    fun observeAllUpdates(): Flow<List<ProgressUpdate>>
}
