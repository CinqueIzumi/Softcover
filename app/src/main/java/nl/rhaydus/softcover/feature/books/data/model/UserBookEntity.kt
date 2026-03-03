package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "user_books",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bookId"),
        Index("statusCode"),
    ]
)
@Serializable
data class UserBookEntity(
    @PrimaryKey
    val id: Int,

    val bookId: Int,

    val statusCode: Int,
    val dateAdded: String,
    val privacySettingId: Int,
    val reviewHasSpoilers: Boolean,
    val editionId: Int?,
    val lastReadDate: String?,
    val rating: Double?,
    val referrerUserId: Int?,
    val reviewedAt: String?,
    val updatedAt: String?,
)