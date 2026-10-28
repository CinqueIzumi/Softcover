package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_user_tags",
    primaryKeys = ["bookId", "category", "name"],
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
data class BookUserTagEntity(
    val bookId: Int,
    val category: String,
    val name: String,
    val spoiler: Boolean,
)
