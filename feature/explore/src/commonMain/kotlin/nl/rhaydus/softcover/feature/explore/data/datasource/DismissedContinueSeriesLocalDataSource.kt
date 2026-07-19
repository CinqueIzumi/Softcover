package nl.rhaydus.softcover.feature.explore.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.DismissedContinueSeriesDao
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity

internal interface DismissedContinueSeriesLocalDataSource {
    val dismissedSeriesIds: Flow<List<Int>>
    val dismissedBooks: Flow<List<DismissedContinueSeriesBookEntity>>
    val dismissedSeries: Flow<List<DismissedContinueSeriesEntity>>

    suspend fun dismissBook(entity: DismissedContinueSeriesBookEntity)

    suspend fun dismissSeries(
        seriesId: Int,
        seriesName: String?,
        coverUrl: String?,
        authorText: String?,
        bookCount: Int?,
    )

    suspend fun undoBookDismissal(bookId: Int)

    suspend fun undoSeriesDismissal(seriesId: Int)
}

internal class DismissedContinueSeriesLocalDataSourceImpl(
    private val dao: DismissedContinueSeriesDao,
) : DismissedContinueSeriesLocalDataSource {
    override val dismissedSeriesIds = dao.observeDismissedSeriesIds()

    override val dismissedBooks = dao.observeDismissedBooks()

    override val dismissedSeries = dao.observeDismissedSeries()

    override suspend fun dismissBook(entity: DismissedContinueSeriesBookEntity) {
        dao.dismissBook(entity = entity)
    }

    override suspend fun dismissSeries(
        seriesId: Int,
        seriesName: String?,
        coverUrl: String?,
        authorText: String?,
        bookCount: Int?,
    ) {
        dao.dismissSeries(
            DismissedContinueSeriesEntity(
                seriesId = seriesId,
                seriesName = seriesName,
                coverUrl = coverUrl,
                authorText = authorText,
                bookCount = bookCount,
            ),
        )
    }

    override suspend fun undoBookDismissal(bookId: Int) {
        dao.undoBookDismissal(bookId = bookId)
    }

    override suspend fun undoSeriesDismissal(seriesId: Int) {
        dao.undoSeriesDismissal(seriesId = seriesId)
    }
}
