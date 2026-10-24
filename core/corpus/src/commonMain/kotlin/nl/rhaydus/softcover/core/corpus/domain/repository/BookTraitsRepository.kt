package nl.rhaydus.softcover.core.corpus.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookTraits

interface BookTraitsRepository {
    /** Emits an empty [BookTraits] while nothing is stored for [bookId]. */
    fun observe(bookId: Int): Flow<BookTraits>

    suspend fun get(bookId: Int): BookTraits

    /** Saving an empty [traits] removes the stored row instead of keeping a blank one. */
    suspend fun save(traits: BookTraits)
}
