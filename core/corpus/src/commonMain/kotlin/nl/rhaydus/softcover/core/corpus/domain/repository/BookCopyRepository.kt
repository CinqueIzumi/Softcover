package nl.rhaydus.softcover.core.corpus.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookCopy

interface BookCopyRepository {
    /** Emits an empty [BookCopy] while nothing is stored for [bookId]. */
    fun observe(bookId: Int): Flow<BookCopy>

    suspend fun get(bookId: Int): BookCopy

    /** Saving an empty [copy], or one whose custom source is blank, removes the stored row. */
    suspend fun save(copy: BookCopy)
}
