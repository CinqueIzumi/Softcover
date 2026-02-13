package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

data class UserBook(
    val id: Int,
    val status: BookStatus,
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