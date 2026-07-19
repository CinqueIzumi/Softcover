package nl.rhaydus.softcover.feature.book_detail.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.UserTagVocabularyDao
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity

internal interface UserTagVocabularyLocalDataSource {
    fun observe(userId: Int): Flow<List<UserTagVocabularyEntity>>

    suspend fun upsertAll(entities: List<UserTagVocabularyEntity>)

    suspend fun insertIfAbsent(entities: List<UserTagVocabularyEntity>)

    suspend fun replaceAll(
        userId: Int,
        entities: List<UserTagVocabularyEntity>,
    )
}

internal class UserTagVocabularyLocalDataSourceImpl(
    private val dao: UserTagVocabularyDao,
) : UserTagVocabularyLocalDataSource {
    override fun observe(userId: Int): Flow<List<UserTagVocabularyEntity>> =
        dao.observe(userId = userId)

    override suspend fun upsertAll(entities: List<UserTagVocabularyEntity>) =
        dao.upsertAll(entities = entities)

    override suspend fun insertIfAbsent(entities: List<UserTagVocabularyEntity>) =
        dao.insertIfAbsent(entities = entities)

    override suspend fun replaceAll(
        userId: Int,
        entities: List<UserTagVocabularyEntity>,
    ) = dao.replaceForUser(
        userId = userId,
        entities = entities,
    )
}
