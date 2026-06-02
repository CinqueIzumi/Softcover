package nl.rhaydus.softcover.feature.book_detail.data.mapper

import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromSlate
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import nl.rhaydus.softcover.fragment.BookReviewFragment

fun BookReviewFragment.toBookReview(): BookReview? {
    val document = reviewDocumentFromSlate(slate = review_slate)

    if (document.isBlank()) return null

    val reviewer = BookReviewer(
        id = user.id,
        username = user.username.toString(),
        name = user.name,
        avatarUrl = user.image?.url,
    )

    return BookReview(
        id = id,
        reviewDocument = document,
        hasSpoilers = review_has_spoilers,
        rating = rating,
        reviewedAt = reviewed_at,
        likesCount = likes_count,
        reviewer = reviewer,
    )
}
