package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_series")
data class BookSeriesEntity(
    @PrimaryKey
    val id: Int,

    val name: String,
    val amountOfBooks: Int,
)