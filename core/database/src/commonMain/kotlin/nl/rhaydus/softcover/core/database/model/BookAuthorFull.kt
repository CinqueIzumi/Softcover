package nl.rhaydus.softcover.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookAuthorFull(
    @Embedded
    val crossRef: BookAuthorCrossRef,

    @Relation(
        parentColumn = "authorId",
        entityColumn = "id",
    )
    val author: AuthorEntity?,
)
