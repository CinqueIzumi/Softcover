package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity

@Entity(tableName = "book_moods", primaryKeys = ["bookId", "isOwn", "word"])
data class BookMoodEntity(
    val bookId: Int,
    val word: String,
    val isOwn: Boolean,
    val strength: Int? = null,
)
