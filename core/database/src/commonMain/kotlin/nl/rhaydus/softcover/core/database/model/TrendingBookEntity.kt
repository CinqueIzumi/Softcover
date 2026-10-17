package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "trending_books",
    primaryKeys = ["period", "bookId"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("bookId")],
)
data class TrendingBookEntity(
    val period: String,
    val bookId: Int,
    val rank: Int,
)
