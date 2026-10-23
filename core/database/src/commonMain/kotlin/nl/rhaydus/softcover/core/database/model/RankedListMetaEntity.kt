package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ranked_list_meta")
data class RankedListMetaEntity(
    @PrimaryKey val listKey: String,
    val fetchedAtEpochMillis: Long,
)
