package nl.rhaydus.softcover.feature.book_detail.presentation.model

import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/**
 * The presentation-ready shape a community review card renders, mapped once from
 * [nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview] where reviews are fetched.
 * A stand-in is [isLoading], so the card masks it; [spoilerRevealed] only matters while [hasSpoilers].
 */
internal data class BookReviewUiModel(
    val id: Int,
    val body: RichTextUiModel,
    val hasSpoilers: Boolean,
    val spoilerRevealed: Boolean,
    val rating: String?,
    val reviewedMonthYear: String?,
    val reviewerName: String,
    val reviewerAvatarUrl: String?,
    val isLoading: Boolean,
)
