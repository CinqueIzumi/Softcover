package nl.rhaydus.softcover.core.book.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.UserTag

interface UserTagsRepository {
    fun observeAll(): Flow<Map<Int, List<UserTag>>>

    fun observeForBook(bookId: Int): Flow<List<UserTag>>

    suspend fun refreshForBook(
        userId: Int,
        bookId: Int,
    )

    /**
     * Writes [tags] locally at once, then to the server, and returns the server's set. A failure puts
     * the previous tags back unless a later write has replaced them, then rethrows.
     */
    suspend fun save(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag>
}
