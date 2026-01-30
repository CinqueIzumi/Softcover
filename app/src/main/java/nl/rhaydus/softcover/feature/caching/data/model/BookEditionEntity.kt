package nl.rhaydus.softcover.feature.caching.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_editions")
data class BookEditionEntity(
    @PrimaryKey val id: Int,
    val bookId: Int,
    val publisher: String?,
    val title: String?,
    val url: String?,
    val isbn10: String?,
    val pages: Int?,
    val releaseYear: Int,
)