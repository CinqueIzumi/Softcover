package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "list_books",
    primaryKeys = ["listId", "bookId", "editionId"],
    foreignKeys = [
        ForeignKey(
            entity = BookListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
        ),
        ForeignKey(
            entity = BookEditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["editionId"],
        )
    ],
    indices = [
        Index("listId"),
        Index("bookId"),
        Index("editionId")
    ]
)
data class ListBookEntity(
    val listId: Int,
    val bookId: Int,
    val editionId: Int,
    val position: Int? = null,
)