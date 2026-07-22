package nl.rhaydus.softcover.feature.book_detail.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagVocabularyLocalDataSource
import nl.rhaydus.softcover.feature.book_detail.data.datasource.UserTagsRemoteDataSource
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toUserTag
import nl.rhaydus.softcover.feature.book_detail.data.mapper.toVocabularyEntity
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagVocabularyRepository

internal class UserTagVocabularyRepositoryImpl(
    private val remoteDataSource: UserTagsRemoteDataSource,
    private val localDataSource: UserTagVocabularyLocalDataSource,
) : UserTagVocabularyRepository {
    override fun observe(userId: Int): Flow<List<UserTag>> =
        localDataSource.observe(userId = userId).map { entities -> entities.map { it.toUserTag() } }

    override suspend fun syncFromRemote(userId: Int) {
        val entities = remoteDataSource.fetchUserVocabulary(userId = userId)
            .map { it.toVocabularyEntity(userId = userId) }

        localDataSource.replaceAll(
            userId = userId,
            entities = entities,
        )
    }

    override suspend fun record(
        userId: Int,
        tags: List<UserTag>,
    ) {
        // [tags] is the SaveTags mutation echo, whose count is the tag's global site-wide
        // popularity, not this user's usage. Seeding new rows at 1 and never touching existing
        // ones keeps personal usage counts intact; the next syncFromRemote replaces them with the
        // accurate aggregate.
        val entities = tags.map { it.toVocabularyEntity(userId = userId).copy(usageCount = 1) }

        localDataSource.insertIfAbsent(entities = entities)
    }
}
