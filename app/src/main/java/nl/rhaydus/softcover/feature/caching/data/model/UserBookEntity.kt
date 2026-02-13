package nl.rhaydus.softcover.feature.caching.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserBookEntity(
    val id: Int,
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