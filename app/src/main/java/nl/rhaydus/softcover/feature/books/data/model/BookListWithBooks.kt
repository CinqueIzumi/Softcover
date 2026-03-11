package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookListWithBooks(
    @Embedded
    val bookList: BookListEntity,

    @Relation(
        entity = ListBookEntity::class,
        parentColumn = "id",
        entityColumn = "listId"
    )
    val listBooks: List<ListBookFull>,
)