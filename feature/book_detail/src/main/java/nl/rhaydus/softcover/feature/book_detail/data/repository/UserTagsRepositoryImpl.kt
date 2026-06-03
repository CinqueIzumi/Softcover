package nl.rhaydus.softcover.feature.book_detail.data.repository

import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagsRepository

internal class UserTagsRepositoryImpl(
    private val userTagsRemoteDataSource: UserTagsRemoteDataSource,
) : UserTagsRepository {
    override suspend fun getUserTags(
        userId: Int,
        bookId: Int,
    ): List<UserTag> = userTagsRemoteDataSource.getUserTags(
        userId = userId,
        bookId = bookId,
    )

    override suspend fun saveTags(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag> = userTagsRemoteDataSource.saveTags(
        bookId = bookId,
        tags = tags,
    )
}
