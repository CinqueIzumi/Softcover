package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "book_author_cross_ref",
    primaryKeys = ["bookId", "authorId"],
    // authorId is the foreign side Room walks when resolving the author relationship; without its own
    // index that resolution is a full table scan (the composite PK only indexes the bookId-leading order).
    indices = [Index("authorId")],
)
data class BookAuthorCrossRef(
    val bookId: Int,
    val authorId: Int,
)
