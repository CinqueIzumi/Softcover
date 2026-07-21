package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Advances a book's reading progress, auto-completing it when the new position reaches the end of
 * the current edition. Wraps [UpdateBookProgressUseCase] and [MarkBookAsReadUseCase] so every
 * "where am I now" entry point — the reading screen, Focus Mode, and the lock-screen quick update —
 * shares one canonical finished-vs-progress decision. Each of those wrapped use cases marks today's
 * reading activity on success, so the streak strip updates whichever branch runs.
 *
 * The success value reports which branch ran: the [ShelfMutationOutcome] from the finish when the
 * position reached the end (so a caller can fire finish effects — e.g. the verdict prompt — only on
 * a genuine [ShelfMutationOutcome.Applied] transition), or `null` when it merely advanced progress.
 */
class RecordBookProgressUseCase(
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
    ): Result<ShelfMutationOutcome?> {
        val edition = book.currentEdition

        val finished = when {
            edition == null -> false
            newSeconds != null -> edition.audioSeconds?.let { newSeconds >= it } == true
            newPage != null -> newPage == edition.pages
            else -> false
        }

        return if (finished) {
            markBookAsReadUseCase(book = book)
        } else {
            updateBookProgressUseCase(
                book = book,
                newPage = newPage,
                newSeconds = newSeconds,
            ).map { null }
        }
    }
}
