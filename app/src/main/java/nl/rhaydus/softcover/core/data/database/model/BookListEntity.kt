package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_lists")
data class BookListEntity(
    @PrimaryKey
    val id: Int,

    val name: String,
    val slug: String,
)