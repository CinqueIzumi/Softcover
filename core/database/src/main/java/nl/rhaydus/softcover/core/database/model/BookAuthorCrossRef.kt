package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity

@Entity(
    tableName = "book_author_cross_ref",
    primaryKeys = ["bookId", "authorId"],
)
data class BookAuthorCrossRef(
    val bookId: Int,
    val authorId: Int,
)
