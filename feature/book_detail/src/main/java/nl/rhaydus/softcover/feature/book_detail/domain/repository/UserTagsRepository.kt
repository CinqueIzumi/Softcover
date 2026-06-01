package nl.rhaydus.softcover.feature.book_detail.domain.repository

import nl.rhaydus.softcover.core.domain.model.UserTag

interface UserTagsRepository {
    suspend fun getUserTags(
        userId: Int,
        bookId: Int,
    ): List<UserTag>

    suspend fun saveTags(
        bookId: Int,
        tags: List<UserTag>,
    ): List<UserTag>
}
