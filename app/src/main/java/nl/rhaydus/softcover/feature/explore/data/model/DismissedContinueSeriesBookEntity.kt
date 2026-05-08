package nl.rhaydus.softcover.feature.explore.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_continue_series_books")
data class DismissedContinueSeriesBookEntity(
    @PrimaryKey val bookId: Int,
)
