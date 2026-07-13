package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_continue_series")
data class DismissedContinueSeriesEntity(
    @PrimaryKey val seriesId: Int,
    val seriesName: String? = null,
    val coverUrl: String? = null,
)
