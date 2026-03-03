package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookFullEntity(
    @Embedded
    val book: BookEntity,

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
    val editions: List<BookEditionWithAuthors>,

    @Relation(
        entity = UserBookEntity::class,
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val userBookWithJournals: UserBookWithJournals?,
)