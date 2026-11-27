package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_moments")
data class MoodMomentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val word: String,
    val isOwn: Boolean,
    val strength: Int? = null,
    val percent: Int? = null,
    val page: Int? = null,
    val seconds: Int? = null,
    val recordedAt: Long,
)
