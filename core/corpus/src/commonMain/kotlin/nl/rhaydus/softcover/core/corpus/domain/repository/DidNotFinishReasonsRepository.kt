package nl.rhaydus.softcover.core.corpus.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons

interface DidNotFinishReasonsRepository {
    fun observeAll(): Flow<Map<Int, DidNotFinishReasons>>

    /** Emits an empty [DidNotFinishReasons] while nothing is stored for [bookId]. */
    fun observe(bookId: Int): Flow<DidNotFinishReasons>

    suspend fun get(bookId: Int): DidNotFinishReasons

    suspend fun save(reasons: DidNotFinishReasons)
}
