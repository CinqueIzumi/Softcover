package nl.rhaydus.softcover.feature.explore.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.database.dao.DismissedContinueSeriesDao
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity

interface DismissedContinueSeriesLocalDataSource {
    val dismissedBookIds: Flow<List<Int>>
    val dismissedSeriesIds: Flow<List<Int>>

    suspend fun dismissBook(bookId: Int)

    suspend fun dismissSeries(seriesId: Int)

    suspend fun undoBookDismissal(bookId: Int)

    suspend fun undoSeriesDismissal(seriesId: Int)
}

internal class DismissedContinueSeriesLocalDataSourceImpl(
    private val dao: DismissedContinueSeriesDao,
) : DismissedContinueSeriesLocalDataSource {
    override val dismissedBookIds = dao.observeDismissedBookIds()

    override val dismissedSeriesIds = dao.observeDismissedSeriesIds()

    override suspend fun dismissBook(bookId: Int) {
        dao.dismissBook(DismissedContinueSeriesBookEntity(bookId = bookId))
    }

    override suspend fun dismissSeries(seriesId: Int) {
        dao.dismissSeries(DismissedContinueSeriesEntity(seriesId = seriesId))
    }

    override suspend fun undoBookDismissal(bookId: Int) {
        dao.undoBookDismissal(bookId = bookId)
    }

    override suspend fun undoSeriesDismissal(seriesId: Int) {
        dao.undoSeriesDismissal(seriesId = seriesId)
    }
}
