package nl.rhaydus.softcover.feature.personal.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "book_highlights",
    indices = [Index("bookId")],
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val bookId: Int,
    val quote: String,
    val page: Int?,
    val note: String?,
    val createdAt: String,
)
