package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class ListBookFull(
    @Embedded
    val listBook: ListBookEntity,

    @Relation(
        entity = BookEntity::class,
        parentColumn = "bookId",
        entityColumn = "id",
    )
    val book: BookFullEntity?,

    @Relation(
        entity = BookEditionView::class,
        parentColumn = "editionId",
        entityColumn = "id",
    )
    val edition: BookEditionWithAuthors?,
)