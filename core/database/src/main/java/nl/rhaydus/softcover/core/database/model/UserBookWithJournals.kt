package nl.rhaydus.softcover.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserBookWithJournals(
    @Embedded
    val userBook: UserBookEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "userBookId"
    )
    val journals: List<ReadingJournalEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "userBookId",
    )
    val userBookRead: UserBookReadEntity?,
)