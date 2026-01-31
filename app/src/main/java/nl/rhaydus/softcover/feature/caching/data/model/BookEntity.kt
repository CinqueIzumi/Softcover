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
    // region book
    @PrimaryKey val id: Int,
    val title: String,
    val defaultEditionId: Int?,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    // endregion

    // region user book
    val userBookId: Int?,
    val statusCode: Int,
    val currentEditionId: Int?,
    val userLastReadDate: String?,
    val userDateAdded: String?,
    val userPrivacySettingId: Int?,
    val userRating: Double?,
    val userReferrerUserId: Int?,
    val userReviewHasSpoilers: Boolean?,
    val userReviewedAt: String?,
    val userUpdatedAt: String?,
    // endregion

    // region user book read
    val currentPage: Int?,
    val progress: Float?,
    val userBookReadId: Int?,
    val startedAt: String?,
    val finishedAt: String?,
    // endregion
)