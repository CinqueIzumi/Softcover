package nl.rhaydus.softcover.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookFullEntity(
    @Embedded
    val book: BookEntity,

    @Relation(
        entity = BookAuthorCrossRef::class,
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val bookAuthors: List<BookAuthorFull>,

    @Relation(
        parentColumn = "seriesId",
        entityColumn = "id",
    )
    val series: BookSeriesEntity?,

    @Relation(
        entity = BookEditionView::class,
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val editions: List<BookEditionWithAuthors>,

    @Relation(
        entity = UserBookEntity::class,
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val userBookWithJournals: UserBookWithJournals?,

    @Relation(
        entity = BookTagCrossRef::class,
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val tags: List<BookTagFull>,
)
