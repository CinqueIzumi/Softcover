package nl.rhaydus.softcover.feature.caching.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["userBookId"], unique = true),
    ]
)
data class BookEntity(
    @PrimaryKey val id: Int,
    val statusCode: Int,
    val title: String,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    val defaultEditionId: Int?,
    val currentEditionId: Int?,
    val currentPage: Int?,
    val progress: Float?,
    val userBookId: Int?,
    val userBookReadId: Int?,
    val startedAt: String?,
    val finishedAt: String?,
)