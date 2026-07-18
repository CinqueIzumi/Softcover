package nl.rhaydus.softcover.feature.explore.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.explore.data.mapper.toDomain
import nl.rhaydus.softcover.feature.explore.data.mapper.toEntity
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

// Matches the mood grid's fixed 2x2 tile layout (explore-3a spec).
private const val MOOD_TAGS_LIMIT = 4

internal class ExploreRepositoryImpl(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchLocalDataSource: SearchLocalDataSource,
    private val dismissedContinueSeriesLocalDataSource: DismissedContinueSeriesLocalDataSource,
) : ExploreRepository {
    override val previousSearchQueries: Flow<List<String>> =
        searchLocalDataSource.previousSearchQueries

    override val queriedBooks: Flow<List<Book>> = searchRemoteDataSource.queriedBooks

    override val queriedBooksHasMore: Flow<Boolean> = searchRemoteDataSource.queriedBooksHasMore

    override val dismissedContinueSeriesIds: Flow<List<Int>> =
        dismissedContinueSeriesLocalDataSource.dismissedSeriesIds

    override val dismissedContinueSeriesBooks: Flow<List<DismissedSeriesBook>> =
        dismissedContinueSeriesLocalDataSource.dismissedBooks.map { entities ->
            entities.map { it.toDomain() }
        }

    override val dismissedContinueSeries: Flow<List<DismissedSeries>> =
        dismissedContinueSeriesLocalDataSource.dismissedSeries.map { entities ->
            entities.map { it.toDomain() }
        }

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
        sortMode: ExploreSortMode,
        page: Int,
    ) {
        searchRemoteDataSource.searchForName(
            name = name,
            userId = userId,
            sortMode = sortMode,
            page = page,
        )
    }

    override suspend fun fetchFeaturedUpcomingRelease(): Book? =
        searchRemoteDataSource.fetchFeaturedUpcomingRelease()

    override suspend fun fetchBooksByGenre(
        genre: String,
        limit: Int,
    ): List<Book> = searchRemoteDataSource.fetchBooksByGenre(
        genre = genre,
        limit = limit,
    )

    override suspend fun fetchMoodTags(): List<MoodTag> = searchRemoteDataSource.fetchMoodTags(limit = MOOD_TAGS_LIMIT)

    override suspend fun searchByMood(
        mood: MoodTag,
        page: Int,
    ) {
        searchRemoteDataSource.searchByMood(
            mood = mood,
            page = page,
        )
    }

    override suspend fun clearSearchResults() {
        searchRemoteDataSource.clearSearchResults()
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

    override suspend fun dismissContinueSeriesBook(book: DismissedSeriesBook) {
        dismissedContinueSeriesLocalDataSource.dismissBook(entity = book.toEntity())
    }

    override suspend fun dismissContinueSeries(
        seriesId: Int,
        seriesName: String?,
        coverUrl: String?,
    ) {
        dismissedContinueSeriesLocalDataSource.dismissSeries(
            seriesId = seriesId,
            seriesName = seriesName,
            coverUrl = coverUrl,
        )
    }

    override suspend fun undoContinueSeriesBookDismissal(bookId: Int) {
        dismissedContinueSeriesLocalDataSource.undoBookDismissal(bookId = bookId)
    }

    override suspend fun undoContinueSeriesDismissal(seriesId: Int) {
        dismissedContinueSeriesLocalDataSource.undoSeriesDismissal(seriesId = seriesId)
    }
}
