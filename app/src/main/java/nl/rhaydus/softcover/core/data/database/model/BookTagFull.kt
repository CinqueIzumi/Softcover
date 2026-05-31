package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class BookTagFull(
    @Embedded
    val crossRef: BookTagCrossRef,

    @Relation(
        parentColumn = "tagId",
        entityColumn = "id",
    )
    val tag: TagEntity,
)
