package nl.rhaydus.softcover.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.data.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.data.database.model.DismissedContinueSeriesEntity

@Dao
interface DismissedContinueSeriesDao {
    @Query("SELECT bookId FROM dismissed_continue_series_books")
    fun observeDismissedBookIds(): Flow<List<Int>>

    @Query("SELECT seriesId FROM dismissed_continue_series")
    fun observeDismissedSeriesIds(): Flow<List<Int>>

    @Upsert
    suspend fun dismissBook(entity: DismissedContinueSeriesBookEntity)

    @Upsert
    suspend fun dismissSeries(entity: DismissedContinueSeriesEntity)

    @Query("DELETE FROM dismissed_continue_series_books WHERE bookId = :bookId")
    suspend fun undoBookDismissal(bookId: Int)

    @Query("DELETE FROM dismissed_continue_series WHERE seriesId = :seriesId")
    suspend fun undoSeriesDismissal(seriesId: Int)
}
