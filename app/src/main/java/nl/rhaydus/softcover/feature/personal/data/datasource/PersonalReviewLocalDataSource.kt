package nl.rhaydus.softcover.feature.personal.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.data.dao.PersonalReviewDao
import nl.rhaydus.softcover.feature.personal.data.model.PersonalReviewEntity

interface PersonalReviewLocalDataSource {
    fun observe(bookId: Int): Flow<PersonalReviewEntity?>

    fun observeAll(): Flow<List<PersonalReviewEntity>>

    suspend fun upsert(entity: PersonalReviewEntity)

    suspend fun delete(bookId: Int)
}

class PersonalReviewLocalDataSourceImpl(
    private val dao: PersonalReviewDao,
) : PersonalReviewLocalDataSource {
    override fun observe(bookId: Int): Flow<PersonalReviewEntity?> =
        dao.observeByBookId(bookId = bookId)

    override fun observeAll(): Flow<List<PersonalReviewEntity>> = dao.observeAll()

    override suspend fun upsert(entity: PersonalReviewEntity) = dao.upsert(entity = entity)

    override suspend fun delete(bookId: Int) = dao.deleteByBookId(bookId = bookId)
}
