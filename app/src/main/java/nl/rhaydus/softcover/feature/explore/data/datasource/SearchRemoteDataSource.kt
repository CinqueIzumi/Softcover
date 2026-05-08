package nl.rhaydus.softcover.feature.explore.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface SearchRemoteDataSource {
    val queriedBooks: Flow<List<Book>>

    suspend fun searchForName(
        name: String,
        userId: Int,
    )

    suspend fun fetchTrendingBooks(
        from: String,
        to: String,
        limit: Int,
        offset: Int,
    ): List<Book>

    suspend fun fetchNextInSeries(
        seriesId: Int,
        afterPosition: Double,
    ): Book?
}