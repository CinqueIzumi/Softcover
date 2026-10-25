package nl.rhaydus.softcover.core.book.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.book.data.mapper.toEntity
import nl.rhaydus.softcover.core.book.data.mapper.toUserTag
import nl.rhaydus.softcover.core.database.dao.BookUserTagDao
import nl.rhaydus.softcover.core.database.model.BookUserTagEntity
import nl.rhaydus.softcover.core.domain.model.UserTag

internal interface UserTagsLocalDataSource {
    fun observeAll(): Flow<Map<Int, List<UserTag>>>

    fun observeForBook(bookId: Int): Flow<List<UserTag>>

    suspend fun getForBook(bookId: Int): List<UserTag>

    suspend fun replaceForBook(
        bookId: Int,
        tags: List<UserTag>,
    )

    /** Replaces the tags of every id in [bookIds]; an id without an entry in [tagsByBookId] ends up empty. */
    suspend fun replaceForBooks(
        bookIds: Collection<Int>,
        tagsByBookId: Map<Int, List<UserTag>>,
    )
}

internal class UserTagsLocalDataSourceImpl(
    private val dao: BookUserTagDao,
) : UserTagsLocalDataSource {
    override fun observeAll(): Flow<Map<Int, List<UserTag>>> = dao
        .observeAll()
        .map { rows ->
            rows
                .groupBy { it.bookId }
                .mapValues { (_, bookRows) -> bookRows.toSortedUserTags() }
        }

    override fun observeForBook(bookId: Int): Flow<List<UserTag>> = dao
        .observeForBook(bookId = bookId)
        .map { it.toSortedUserTags() }

    override suspend fun getForBook(bookId: Int): List<UserTag> = dao
        .getForBook(bookId = bookId)
        .toSortedUserTags()

    override suspend fun replaceForBook(
        bookId: Int,
        tags: List<UserTag>,
    ) = dao.replaceForBook(
        bookId = bookId,
        rows = tags.map { it.toEntity(bookId = bookId) },
    )

    override suspend fun replaceForBooks(
        bookIds: Collection<Int>,
        tagsByBookId: Map<Int, List<UserTag>>,
    ) = dao.replaceForBooks(
        bookIds = bookIds,
        rows = bookIds.flatMap { bookId ->
            tagsByBookId[bookId].orEmpty().map { it.toEntity(bookId = bookId) }
        },
    )

    private fun List<BookUserTagEntity>.toSortedUserTags(): List<UserTag> = map { it.toUserTag() }
        .sortedWith(
            compareBy<UserTag> { it.category.ordinal }
                .thenBy { it.name.lowercase() },
        )
}
