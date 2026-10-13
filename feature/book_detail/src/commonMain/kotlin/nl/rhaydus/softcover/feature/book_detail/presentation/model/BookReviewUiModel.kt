package nl.rhaydus.softcover.feature.book_detail.presentation.model

import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/**
 * The presentation-ready shape a community review card renders, mapped once from
 * [nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview] where reviews are fetched.
 * A stand-in is [isLoading], so the card masks it; [isSpoilerGated] withholds the
 * body behind a "Show spoiler" button until the reader reveals it.
 */
internal data class BookReviewUiModel(
    val id: Int,
    val body: RichTextUiModel,
    val isSpoilerGated: Boolean,
    val rating: BadgeUiModel?,
    val reviewedMonthYear: String?,
    val reviewerName: String,
    val reviewerAvatarUrl: String?,
    val isLoading: Boolean,
)
