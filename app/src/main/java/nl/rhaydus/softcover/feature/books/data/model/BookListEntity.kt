package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_lists")
data class BookListEntity(
    @PrimaryKey
    val id: Int,

    val name: String,
)