package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.canonical
import nl.rhaydus.softcover.core.domain.model.isBlank

/**
 * Saves a book's rating and review together - the single seam the "Verdict" sheet in both
 * `feature/book_detail` and `feature/reading` saves through, so neither re-implements the two-call
 * combined save. Each half is skipped independently when it did not actually change.
 */
class SaveBookVerdictUseCase(
    private val updateBookRatingUseCase: UpdateBookRatingUseCase,
    private val updateBookReviewUseCase: UpdateBookReviewUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        rating: Double?,
        review: ReviewDocument,
        hasSpoilers: Boolean,
    ): Result<Unit> {
        // A null draft rating means the user didn't touch the rating (the sheet always seeds the
        // existing one), so it must never clear an existing rating - only a genuine change saves.
        if (rating != null && rating != book.userBook?.rating) {
            updateBookRatingUseCase(
                book = book,
                rating = rating,
            ).onFailure { return Result.failure(it) }
        }

        // Canonical-compare so identical content sliced into different runs (the editor and the
        // server slice runs differently) doesn't read as a change; a spoiler-only toggle still saves.
        val newReview: ReviewDocument = review.canonical()
        val existingReview: ReviewDocument = (book.userBook?.reviewDocument ?: ReviewDocument.EMPTY).canonical()
        val existingHasSpoilers: Boolean = book.userBook?.reviewHasSpoilers ?: false

        val sameReview: Boolean = if (newReview.isBlank() && existingReview.isBlank()) {
            true
        } else {
            newReview == existingReview
        }

        if (sameReview.not() || hasSpoilers != existingHasSpoilers) {
            updateBookReviewUseCase(
                book = book,
                review = newReview,
                hasSpoilers = hasSpoilers,
            ).onFailure { return Result.failure(it) }
        }

        return Result.success(Unit)
    }
}
