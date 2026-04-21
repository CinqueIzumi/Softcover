package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "list_books",
    primaryKeys = ["listId", "bookId", "editionId", "listBookId"],
    indices = [
        Index("listId"),
        Index("bookId"),
        Index("editionId")
    ]
)
data class ListBookEntity(
    val listBookId: Int,
    val listId: Int,
    val bookId: Int,
    val editionId: Int,
    val position: Int? = null,
    val addedAt: String? = null,
)
