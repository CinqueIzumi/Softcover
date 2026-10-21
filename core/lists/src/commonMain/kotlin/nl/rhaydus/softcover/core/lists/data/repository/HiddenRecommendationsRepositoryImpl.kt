package nl.rhaydus.softcover.core.lists.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.lists.data.datasource.HiddenRecommendationsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.mapper.toEntity
import nl.rhaydus.softcover.core.lists.data.mapper.toModel
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.core.lists.domain.repository.HiddenRecommendationsRepository

internal class HiddenRecommendationsRepositoryImpl(
    private val localDataSource: HiddenRecommendationsLocalDataSource,
) : HiddenRecommendationsRepository {
    override val hiddenRecommendations: Flow<List<HiddenRecommendation>> =
        localDataSource.hiddenRecommendations.map { entities -> entities.map { it.toModel() } }

    override suspend fun hide(book: HiddenRecommendation) {
        localDataSource.hide(entity = book.toEntity())
    }

    override suspend fun restore(bookId: Int) {
        localDataSource.restore(bookId = bookId)
    }
}
