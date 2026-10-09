package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.richtext.RichTextParagraph
import nl.rhaydus.softcover.core.component.richtext.RichTextRun
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel

private const val STAND_IN_BODY =
    "A few sentences of what a reader made of the book sit here while the reviews load, " +
        "enough to fill about four lines of the card."

private val STAND_IN_REVIEW = BookReviewUiModel(
    id = -1,
    body = RichTextUiModel(
        paragraphs = persistentListOf(
            RichTextParagraph(runs = persistentListOf(RichTextRun(text = STAND_IN_BODY))),
        ),
    ),
    isSpoilerGated = false,
    rating = ratingBadge(label = "4.0"),
    reviewedMonthYear = "January 2025",
    reviewerName = "REVIEWER NAME",
    reviewerAvatarUrl = null,
    isLoading = true,
)

internal val BOOK_REVIEW_STAND_INS: ImmutableList<BookReviewUiModel> = persistentListOf(
    STAND_IN_REVIEW,
    STAND_IN_REVIEW.copy(id = -2),
)
