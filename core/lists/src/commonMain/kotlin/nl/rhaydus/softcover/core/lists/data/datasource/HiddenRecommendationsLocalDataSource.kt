package nl.rhaydus.softcover.core.lists.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.HiddenRecommendationDao
import nl.rhaydus.softcover.core.database.model.HiddenRecommendationEntity

internal interface HiddenRecommendationsLocalDataSource {
    val hiddenRecommendations: Flow<List<HiddenRecommendationEntity>>

    suspend fun hide(entity: HiddenRecommendationEntity)

    suspend fun restore(bookId: Int)
}

internal class HiddenRecommendationsLocalDataSourceImpl(
    private val dao: HiddenRecommendationDao,
) : HiddenRecommendationsLocalDataSource {
    override val hiddenRecommendations: Flow<List<HiddenRecommendationEntity>> =
        dao.observeHiddenRecommendations()

    override suspend fun hide(entity: HiddenRecommendationEntity) {
        dao.hide(entity = entity)
    }

    override suspend fun restore(bookId: Int) {
        dao.restore(bookId = bookId)
    }
}
