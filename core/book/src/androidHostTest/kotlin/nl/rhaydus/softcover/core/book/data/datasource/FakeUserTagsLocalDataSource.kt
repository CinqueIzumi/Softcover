package nl.rhaydus.softcover.core.book.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.UserTag

internal class FakeUserTagsLocalDataSource : UserTagsLocalDataSource {
    private val rowsByBookId = MutableStateFlow<Map<Int, List<UserTag>>>(emptyMap())

    var replaceForBookFailure: Throwable? = null

    val snapshot: Map<Int, List<UserTag>> get() = rowsByBookId.value

    override fun observeAll(): Flow<Map<Int, List<UserTag>>> = rowsByBookId

    override fun observeForBook(bookId: Int): Flow<List<UserTag>> = rowsByBookId.map { rows ->
        rows[bookId].orEmpty()
    }

    override suspend fun getForBook(bookId: Int): List<UserTag> = rowsByBookId.value[bookId].orEmpty()

    override suspend fun replaceForBook(
        bookId: Int,
        tags: List<UserTag>,
    ) {
        replaceForBookFailure?.let { failure -> throw failure }

        rowsByBookId.value = rowsByBookId.value - bookId + (bookId to tags.withoutCounts())
    }

    override suspend fun replaceForBooks(
        bookIds: Collection<Int>,
        tagsByBookId: Map<Int, List<UserTag>>,
    ) {
        rowsByBookId.value = rowsByBookId.value - bookIds.toSet() +
            bookIds.associateWith {
                tagsByBookId.getOrDefault(
                    it,
                    emptyList(),
                ).withoutCounts()
            }
    }

    // Stored rows carry no global count, as in the Room-backed source.
    private fun List<UserTag>.withoutCounts(): List<UserTag> = map { tag -> tag.copy(count = 0) }
}
