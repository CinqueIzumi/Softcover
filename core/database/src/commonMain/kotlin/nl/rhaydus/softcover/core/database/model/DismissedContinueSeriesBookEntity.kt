package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_continue_series_books")
data class DismissedContinueSeriesBookEntity(
    @PrimaryKey val bookId: Int,
    val bookTitle: String? = null,
    val coverUrl: String? = null,
    val authorText: String? = null,
    val seriesName: String? = null,
    val seriesId: Int? = null,
    val seriesPosition: Double? = null,
)
