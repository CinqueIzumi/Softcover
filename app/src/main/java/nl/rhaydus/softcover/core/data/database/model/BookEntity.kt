package nl.rhaydus.softcover.core.data.database.model

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
    val releaseDate: String?,
    val coverUrl: String,
    val usersCount: Int,
    val ratingsCount: Int,
    val positionsInSeries: String,
    val isCompilation: Boolean,
    val seriesId: Int?,
)