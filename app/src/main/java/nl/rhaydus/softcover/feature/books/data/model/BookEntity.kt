package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "books")
@Serializable
data class BookEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val defaultEditionId: Int?,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    val usersCount: Int,
)