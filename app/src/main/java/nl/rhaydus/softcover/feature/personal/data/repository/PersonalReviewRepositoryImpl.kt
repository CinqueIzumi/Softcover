package nl.rhaydus.softcover.feature.personal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.personal.data.datasource.PersonalReviewLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.mapper.toDomain
import nl.rhaydus.softcover.feature.personal.data.model.PersonalReviewEntity
import nl.rhaydus.softcover.feature.personal.domain.model.PersonalReview
import nl.rhaydus.softcover.feature.personal.domain.repository.PersonalReviewRepository
import java.time.Instant

class PersonalReviewRepositoryImpl(
    private val localDataSource: PersonalReviewLocalDataSource,
) : PersonalReviewRepository {
    override fun observe(bookId: Int): Flow<PersonalReview?> =
        localDataSource.observe(bookId = bookId).map { it?.toDomain() }

    override fun observeAll(): Flow<List<PersonalReview>> =
        localDataSource.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveDraft(
        bookId: Int,
        body: String,
        hasSpoilers: Boolean,
    ) {
        localDataSource.upsert(
            entity = PersonalReviewEntity(
                bookId = bookId,
                body = body,
                hasSpoilers = hasSpoilers,
                isDraft = true,
                updatedAt = Instant.now().toString(),
            ),
        )
    }

    override suspend fun publish(
        bookId: Int,
        body: String,
        hasSpoilers: Boolean,
    ) {
        localDataSource.upsert(
            entity = PersonalReviewEntity(
                bookId = bookId,
                body = body,
                hasSpoilers = hasSpoilers,
                isDraft = false,
                updatedAt = Instant.now().toString(),
            ),
        )
    }

    override suspend fun delete(bookId: Int) {
        localDataSource.delete(bookId = bookId)
    }
}
