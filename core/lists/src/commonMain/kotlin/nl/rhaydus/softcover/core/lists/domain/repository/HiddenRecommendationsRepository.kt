package nl.rhaydus.softcover.core.lists.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation

interface HiddenRecommendationsRepository {
    val hiddenRecommendations: Flow<List<HiddenRecommendation>>

    suspend fun hide(book: HiddenRecommendation)

    suspend fun restore(bookId: Int)
}
