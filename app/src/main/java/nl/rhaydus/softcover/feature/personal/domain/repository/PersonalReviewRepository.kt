package nl.rhaydus.softcover.feature.personal.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.PersonalReview

interface PersonalReviewRepository {
    fun observe(bookId: Int): Flow<PersonalReview?>

    fun observeAll(): Flow<List<PersonalReview>>

    suspend fun saveDraft(
        bookId: Int,
        body: String,
        hasSpoilers: Boolean,
    )

    suspend fun publish(
        bookId: Int,
        body: String,
        hasSpoilers: Boolean,
    )

    suspend fun delete(bookId: Int)
}
