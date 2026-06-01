package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_sessions",
    indices = [Index("bookId")],
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val bookId: Int,
    val startedAt: String,
    val endedAt: String?,
    val startPage: Int?,
    val endPage: Int?,
    val startSeconds: Int?,
    val endSeconds: Int?,
    val pausedSeconds: Int = 0,
    val lastPausedAt: String? = null,
)
