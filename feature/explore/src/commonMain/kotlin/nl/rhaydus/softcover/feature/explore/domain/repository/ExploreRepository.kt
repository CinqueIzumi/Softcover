package nl.rhaydus.softcover.feature.explore.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook

interface ExploreRepository {
    val previousSearchQueries: Flow<List<String>>
    val queriedBooks: Flow<List<Book>>
    val dismissedContinueSeriesIds: Flow<List<Int>>
    val dismissedContinueSeriesBooks: Flow<List<DismissedSeriesBook>>
    val dismissedContinueSeries: Flow<List<DismissedSeries>>

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

    suspend fun dismissContinueSeriesBook(book: DismissedSeriesBook)

    suspend fun dismissContinueSeries(
        seriesId: Int,
        seriesName: String? = null,
        coverUrl: String? = null,
    )

    suspend fun undoContinueSeriesBookDismissal(bookId: Int)

    suspend fun undoContinueSeriesDismissal(seriesId: Int)
}
