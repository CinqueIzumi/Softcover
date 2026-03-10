package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookListWithEditions(
    @Embedded
    val bookList: BookListEntity,

    @Relation(
        entity = BookEditionView::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ListBookEntity::class,
            parentColumn = "listId",
            entityColumn = "editionId"
        )
    )
    val editions: List<BookEditionView>,
)