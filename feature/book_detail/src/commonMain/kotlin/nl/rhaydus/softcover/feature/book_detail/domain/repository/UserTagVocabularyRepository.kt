package nl.rhaydus.softcover.feature.book_detail.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.UserTag

interface UserTagVocabularyRepository {
    fun observe(userId: Int): Flow<List<UserTag>>

    suspend fun syncFromRemote(userId: Int)

    suspend fun record(
        userId: Int,
        tags: List<UserTag>,
    )
}
