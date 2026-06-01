package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Advances a book's reading progress, auto-completing it when the new position reaches the end of
 * the current edition. Wraps [UpdateBookProgressUseCase] and [MarkBookAsReadUseCase] so every
 * "where am I now" entry point — the reading screen, Focus Mode, and the lock-screen quick update —
 * shares one canonical finished-vs-progress decision.
 */
class RecordBookProgressUseCase(
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Result<Unit> {
        val edition = book.currentEdition

        val finished = when {
            edition == null -> false
            newSeconds != null -> edition.audioSeconds?.let { newSeconds >= it } == true
            newPage != null -> newPage == edition.pages
            else -> false
        }

        return if (finished) {
            markBookAsReadUseCase(book = book).map { }
        } else {
            updateBookProgressUseCase(
                book = book,
                newPage = newPage,
                newSeconds = newSeconds,
            )
        }
    }
}
