package nl.rhaydus.softcover.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

internal data class BookWithUserData(
    @Embedded
    val book: BookEntity,

    @Relation(
        entity = UserBookEntity::class,
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val userBook: UserBookWithRead?,
)
