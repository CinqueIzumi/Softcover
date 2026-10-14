package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trending_meta")
data class TrendingMetaEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val fetchedAtEpochMillis: Long,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
