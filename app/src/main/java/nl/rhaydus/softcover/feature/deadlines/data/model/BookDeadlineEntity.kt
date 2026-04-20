package nl.rhaydus.softcover.feature.deadlines.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_deadlines")
data class BookDeadlineEntity(
    @PrimaryKey val bookId: Int,
    val deadlineDate: String,
    val setAt: String,
    val initialPagesPerDay: Float,
)
