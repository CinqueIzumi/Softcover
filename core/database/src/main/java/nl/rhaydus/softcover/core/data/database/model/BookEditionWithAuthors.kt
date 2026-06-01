package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookEditionWithAuthors(
    @Embedded
    val edition: BookEditionView,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EditionAuthorCrossRef::class,
            parentColumn = "editionId",
            entityColumn = "authorId"
        )
    )
    val authors: List<AuthorEntity>,
)