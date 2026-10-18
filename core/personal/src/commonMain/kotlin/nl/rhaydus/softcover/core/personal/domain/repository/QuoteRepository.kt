package nl.rhaydus.softcover.core.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import kotlin.time.Instant

interface QuoteRepository {
    fun observeForBook(bookId: Int): Flow<List<Quote>>

    suspend fun refreshForBook(bookId: Int)

    fun observeAll(): Flow<List<Quote>>

    suspend fun refreshAll()

    /** Saves [actionAt] as given, or the current time when `null`, so a queued quote keeps its save time. */
    suspend fun add(
        bookId: Int,
        editionId: Int?,
        text: String,
        privacy: PrivacySetting,
        actionAt: Instant?,
        position: QuotePosition?,
        bookTitle: String?,
        editionTitle: String?,
        editionCoverUrl: String?,
    ): Long

    suspend fun delete(localId: Long)
}
