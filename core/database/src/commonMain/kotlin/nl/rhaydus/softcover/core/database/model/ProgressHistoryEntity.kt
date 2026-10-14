package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress_history",
    indices = [Index("bookId")],
)
data class ProgressHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val bookId: Int,
    val actionAtEpochMillis: Long,
    val pages: Int?,
    val seconds: Int?,
    val pending: Boolean,
)
