package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookEditionWithAuthors(
    @Embedded val edition: BookEditionEntity,

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