package nl.rhaydus.softcover.feature.caching.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookFullEntity(
    @Embedded val book: BookEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookAuthorCrossRef::class,
            parentColumn = "bookId",
            entityColumn = "authorId"
        )
    )
    val bookAuthors: List<AuthorEntity>,

    @Relation(
        entity = BookEditionEntity::class,
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val editions: List<BookEditionWithAuthors>
)

data class BookEditionWithAuthors(
    @Embedded val edition: BookEditionEntity,

    @Relation(
        parentColumn = "id", // BookEditionEntity.id
        entityColumn = "id", // AuthorEntity.id
        associateBy = Junction(
            value = EditionAuthorCrossRef::class,
            parentColumn = "editionId", // cross-ref column referencing edition
            entityColumn = "authorId"   // cross-ref column referencing author
        )
    )
    val authors: List<AuthorEntity>
)
