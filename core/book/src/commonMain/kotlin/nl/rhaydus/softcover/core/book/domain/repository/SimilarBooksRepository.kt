package nl.rhaydus.softcover.core.book.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookCandidate
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookSeed

interface SimilarBooksRepository {
    /** The seed for [bookId], fetched at most once per session. */
    suspend fun getSeed(bookId: Int): SimilarBookSeed

    /** Hydrates [ids] in a single request; the result is in no particular order. */
    suspend fun fetchCandidates(ids: List<Int>): List<SimilarBookCandidate>

    /** The ids among [ids] that sit on any shelf. */
    suspend fun getShelvedBookIds(ids: List<Int>): Set<Int>

    /** Re-emits the ids among [ids] that sit on any shelf as shelves change. */
    fun observeShelvedBookIds(ids: List<Int>): Flow<Set<Int>>
}
