package nl.rhaydus.softcover.core.domain.model

import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

data class Book(
    val id: Int,
    val title: String,
    val editions: List<BookEdition>,
    val defaultEdition: BookEdition?,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    val authors: List<Author>,

    // region UserBook
    val userBookId: Int?,
    val userStatus: BookStatus,
    val userEditionId: Int?,
    val userLastReadDate: String?,
    val userDateAdded: String?,
    val userPrivacySettingId: Int?,
    val userRating: Double?,
    val userReferrerUserId: Int?,
    val userReviewHasSpoilers: Boolean?,
    val userReviewedAt: String?,
    val userUpdatedAt: String?,
    // endregion

    // region UserBookRead
    val currentPage: Int?,
    val progress: Float?,
    val userBookReadId: Int?,
    val startedAt: String?,
    val finishedAt: String?,
    // endregion
) {
    val currentEdition: BookEdition
        get() {
            return editions.firstOrNull { it.id == userEditionId } ?: defaultEdition ?: editions.first()
        }
}