package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_log_entries",
    indices = [Index("bookId")],
)
data class ReadingLogEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val bookId: Int,
    val startedAt: String?,
    val finishedAt: String?,
    val rating: Double?,
    val note: String?,
    val createdAt: String,
)
