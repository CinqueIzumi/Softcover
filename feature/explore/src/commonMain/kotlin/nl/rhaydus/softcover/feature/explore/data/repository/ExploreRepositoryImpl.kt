package nl.rhaydus.softcover.feature.explore.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

internal class ExploreRepositoryImpl(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchLocalDataSource: SearchLocalDataSource,
    private val dismissedContinueSeriesLocalDataSource: DismissedContinueSeriesLocalDataSource,
) : ExploreRepository {
    override val previousSearchQueries: Flow<List<String>> =
        searchLocalDataSource.previousSearchQueries

    override val queriedBooks: Flow<List<Book>> = searchRemoteDataSource.queriedBooks

    override val dismissedContinueSeriesBookIds: Flow<List<Int>> =
        dismissedContinueSeriesLocalDataSource.dismissedBookIds

    override val dismissedContinueSeriesIds: Flow<List<Int>> =
        dismissedContinueSeriesLocalDataSource.dismissedSeriesIds

    override suspend fun fetchNextInSeries(
        seriesId: Int,
        afterPosition: Double,
    ): Book? = searchRemoteDataSource.fetchNextInSeries(
        seriesId = seriesId,
        afterPosition = afterPosition,
    )

    override suspend fun searchForName(
        name: String,
        userId: Int,
    ) {
        searchRemoteDataSource.searchForName(
            name = name,
            userId = userId,
        )
    }

    override suspend fun saveSearchQuery(name: String) {
        searchLocalDataSource.saveSearchQuery(name = name)
    }

    override suspend fun removeSearchQuery(name: String) {
        searchLocalDataSource.removeSearchQuery(name = name)
    }

    override suspend fun removeAllSearchQueries() {
        searchLocalDataSource.removeAllSearchQueries()
    }

    override suspend fun dismissContinueSeriesBook(bookId: Int) {
        dismissedContinueSeriesLocalDataSource.dismissBook(bookId = bookId)
    }

    override suspend fun dismissContinueSeries(seriesId: Int) {
        dismissedContinueSeriesLocalDataSource.dismissSeries(seriesId = seriesId)
    }

    override suspend fun undoContinueSeriesBookDismissal(bookId: Int) {
        dismissedContinueSeriesLocalDataSource.undoBookDismissal(bookId = bookId)
    }

    override suspend fun undoContinueSeriesDismissal(seriesId: Int) {
        dismissedContinueSeriesLocalDataSource.undoSeriesDismissal(seriesId = seriesId)
    }
}
