package nl.rhaydus.softcover.feature.explore.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dismissed_continue_series")
data class DismissedContinueSeriesEntity(
    @PrimaryKey val seriesId: Int,
)
