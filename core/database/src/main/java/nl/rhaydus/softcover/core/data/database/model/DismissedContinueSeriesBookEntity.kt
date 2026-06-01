package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_continue_series_books")
data class DismissedContinueSeriesBookEntity(
    @PrimaryKey val bookId: Int,
)
