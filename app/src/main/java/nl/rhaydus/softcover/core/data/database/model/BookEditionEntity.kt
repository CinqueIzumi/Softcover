package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_editions")
data class BookEditionEntity(
    @PrimaryKey
    val id: Int,

    val canonicalId: Int?,
    val bookId: Int,
    val publisher: String?,
    val title: String?,
    val url: String?,
    val localImagePath: String?,
    val isbn10: String?,
    val pages: Int?,
    val audioSeconds: Int?,
    val releaseYear: Int,
    val releaseDate: String?,
    val format: String,
)