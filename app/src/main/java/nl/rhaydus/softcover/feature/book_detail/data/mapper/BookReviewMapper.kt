package nl.rhaydus.softcover.feature.book_detail.data.mapper

import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import nl.rhaydus.softcover.fragment.BookReviewFragment

fun BookReviewFragment.toBookReview(): BookReview? {
    val text = review ?: return null

    val reviewer = BookReviewer(
        id = user.id,
        username = user.username.toString(),
        name = user.name,
        avatarUrl = user.image?.url,
    )

    return BookReview(
        id = id,
        review = text,
        hasSpoilers = review_has_spoilers,
        rating = rating,
        reviewedAt = reviewed_at,
        likesCount = likes_count,
        reviewer = reviewer,
    )
}
