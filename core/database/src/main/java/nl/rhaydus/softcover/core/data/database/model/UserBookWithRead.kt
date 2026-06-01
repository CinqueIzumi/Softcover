package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserBookWithRead(
    @Embedded
    val userBook: UserBookEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "userBookId"
    )
    val read: UserBookReadEntity?,
)