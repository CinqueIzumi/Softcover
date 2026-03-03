package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_list_edition_cross_ref",
    primaryKeys = ["bookListId", "editionId"],
    foreignKeys = [
        ForeignKey(
            entity = BookListEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookListId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = BookEditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["editionId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index("bookListId"),
        Index("editionId"),
    ]
)
data class BookListEditionCrossRef(
    val bookListId: Int,
    val editionId: Int,
)