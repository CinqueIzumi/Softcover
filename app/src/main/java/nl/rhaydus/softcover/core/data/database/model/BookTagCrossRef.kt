package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "book_tag_cross_ref",
    primaryKeys = ["bookId", "tagId"],
    indices = [Index("tagId")],
)
data class BookTagCrossRef(
    val bookId: Int,
    val tagId: Int,
)
