package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.delay
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.model.BookImport
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.exception.BookImportPendingException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val INITIAL_IMPORT_BACKOFF = 1.seconds
private val MAX_IMPORT_BACKOFF = 5.seconds
private val IMPORT_WAIT_BUDGET = 60.seconds

/**
 * Adds a not-yet-catalogued ISBN to Hardcover via `upsert_book`, then hydrates the freshly-created
 * book through [FetchBookByIdUseCase] so it reaches the detail screen identically to a resolved
 * scan. Returns [IsbnLookupResult.Found] — the same outcome a normal resolve yields — so callers
 * navigate through a single path. The edition id falls back to the hydrated book's current edition
 * when the mutation didn't return one.
 *
 * Hardcover imports unknown ISBNs asynchronously, so the call is repeated with doubling backoff
 * while it reports [BookImport.Fetching]; the gaps stay at or above the API's one-request-per-second
 * refill. Still importing once the wait budget is spent fails with [BookImportPendingException].
 */
class AddBookByIsbnUseCase(
    private val booksRepository: BooksRepository,
    private val fetchBookByIdUseCase: FetchBookByIdUseCase,
) {
    suspend operator fun invoke(isbn: String): Result<IsbnLookupResult.Found> = runCatchingLogged {
        val created = importBook(isbn = isbn)

        val book = fetchBookByIdUseCase(id = created.bookId).getOrThrow()

        IsbnLookupResult.Found(
            book = book,
            editionId = created.editionId ?: book.currentEdition?.id,
        )
    }

    private suspend fun importBook(isbn: String): BookImport.Created {
        var backoff = INITIAL_IMPORT_BACKOFF
        var waited = Duration.ZERO

        while (true) {
            when (val import = booksRepository.addBookByIsbn(isbn = isbn)) {
                is BookImport.Created -> return import

                BookImport.Fetching -> Unit
            }

            if (waited >= IMPORT_WAIT_BUDGET) throw BookImportPendingException()

            val pause = minOf(
                a = backoff,
                b = IMPORT_WAIT_BUDGET - waited,
            )
            delay(pause)
            waited += pause
            backoff = minOf(
                a = backoff * 2,
                b = MAX_IMPORT_BACKOFF,
            )
        }
    }
}
