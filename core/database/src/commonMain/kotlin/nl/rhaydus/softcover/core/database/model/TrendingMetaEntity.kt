package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trending_meta")
data class TrendingMetaEntity(
    @PrimaryKey val period: String,
    val fetchedAtEpochMillis: Long,
)
