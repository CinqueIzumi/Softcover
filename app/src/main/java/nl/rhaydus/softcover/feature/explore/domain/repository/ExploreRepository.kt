package nl.rhaydus.softcover.feature.explore.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface ExploreRepository {
    val previousSearchQueries: Flow<List<String>>
    val queriedBooks: Flow<List<Book>>
    val dismissedContinueSeriesBookIds: Flow<List<Int>>
    val dismissedContinueSeriesIds: Flow<List<Int>>

    suspend fun fetchTrendingBooks(): List<Book>

    suspend fun fetchNextInSeries(
        seriesId: Int,
        afterPosition: Double,
    ): Book?

    suspend fun searchForName(
        name: String,
        userId: Int,
    )

    suspend fun saveSearchQuery(name: String)

    suspend fun removeSearchQuery(name: String)

    suspend fun removeAllSearchQueries()

    suspend fun dismissContinueSeriesBook(bookId: Int)

    suspend fun dismissContinueSeries(seriesId: Int)

    suspend fun undoContinueSeriesBookDismissal(bookId: Int)

    suspend fun undoContinueSeriesDismissal(seriesId: Int)
}
