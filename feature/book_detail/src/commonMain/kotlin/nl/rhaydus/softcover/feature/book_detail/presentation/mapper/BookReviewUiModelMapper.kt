package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel

internal fun BookReview.toBookReviewUiModel(): BookReviewUiModel = BookReviewUiModel(
    id = id,
    body = reviewDocument.toRichTextUiModel(),
    isSpoilerGated = hasSpoilers,
    rating = rating?.let {
        formatDecimalNumber(
            value = it,
            fractionDigits = 1,
        )
    },
    reviewedMonthYear = getReviewedMonthYear(),
    reviewerName = reviewer.name?.takeIf { it.isNotBlank() } ?: reviewer.username,
    reviewerAvatarUrl = reviewer.avatarUrl,
    isLoading = false,
)
