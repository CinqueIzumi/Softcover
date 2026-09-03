package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel

internal fun BookReview.toBookReviewUiModel(): BookReviewUiModel = BookReviewUiModel(
    id = id,
    body = reviewDocument.toRichTextUiModel(),
    hasSpoilers = hasSpoilers,
    rating = rating,
    reviewedMonthYear = getReviewedMonthYear(),
    reviewerName = reviewer.name?.takeIf { it.isNotBlank() } ?: reviewer.username,
    reviewerAvatarUrl = reviewer.avatarUrl,
)
