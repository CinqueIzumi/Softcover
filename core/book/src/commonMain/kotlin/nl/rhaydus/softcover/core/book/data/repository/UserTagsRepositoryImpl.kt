package nl.rhaydus.softcover.core.book.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.book.data.datasource.UserTagsLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.UserTagsRemoteDataSource
import nl.rhaydus.softcover.core.book.data.sync.UserBookCacheGate
import nl.rhaydus.softcover.core.book.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.core.domain.model.UserTag

internal class UserTagsRepositoryImpl(
    private val userTagsRemoteDataSource: UserTagsRemoteDataSource,
    private val userTagsLocalDataSource: UserTagsLocalDataSource,
    private val userBookCacheGate: UserBookCacheGate,
) : UserTagsRepository {
    override fun observeAll(): Flow<Map<Int, List<UserTag>>> = userTagsLocalDataSource.observeAll()

    override fun observeForBook(bookId: Int): Flow<List<UserTag>> =
        userTagsLocalDataSource.observeForBook(bookId = bookId)

    override suspend fun refreshForBook(
        userId: Int,
        bookId: Int,
    ) = userBookCacheGate.refreshUserTags(
        bookId = bookId,
        fetch = {
            userTagsRemoteDataSource.getUserTags(
                userId = userId,
                bookId = bookId,
            )
        },
    )

    override suspend fun save(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag> {
        val previous: List<UserTag> = userTagsLocalDataSource.getForBook(bookId = bookId)

        userBookCacheGate.writeUserTags(
            bookId = bookId,
            rows = tags,
        )

        val saved: List<UserTag> = runCatchingCancellable {
            userTagsRemoteDataSource.saveTags(
                bookId = bookId,
                tags = tags,
            )
        }.getOrElse { failure ->
            userBookCacheGate.restoreUserTags(
                bookId = bookId,
                written = tags,
                previous = previous,
            )

            throw failure
        }

        runCatchingCancellable {
            userBookCacheGate.writeUserTags(
                bookId = bookId,
                rows = saved,
            )
        }.onFailure { failure ->
            AppLog.w("Tags for book $bookId saved remotely but the local write failed: ${failure.message}")
        }

        return saved
    }
}
