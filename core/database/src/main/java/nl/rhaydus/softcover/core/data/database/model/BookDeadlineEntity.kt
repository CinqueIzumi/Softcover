package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_deadlines")
data class BookDeadlineEntity(
    @PrimaryKey val bookId: Int,
    val deadlineDate: String,
    val setAt: String,
    val initialPerDay: Float,
    val unit: String,
)
