package nl.rhaydus.softcover.feature.book_detail.presentation.model

import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/**
 * The presentation-ready shape a community review card renders, mapped once from
 * [nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview] where reviews are fetched.
 * [id] stays on the model because the render keys `revealedSpoilerReviewIds` off it.
 */
internal data class BookReviewUiModel(
    val id: Int,
    val body: RichTextUiModel,
    val hasSpoilers: Boolean,
    val rating: Double?,
    val reviewedMonthYear: String?,
    val reviewerName: String,
    val reviewerAvatarUrl: String?,
)
