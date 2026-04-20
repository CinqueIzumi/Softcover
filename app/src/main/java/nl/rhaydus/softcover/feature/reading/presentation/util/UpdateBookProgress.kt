package nl.rhaydus.softcover.feature.reading.presentation.util

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookProgressUseCase
import timber.log.Timber

class UpdateBookProgress(
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
        setLoading: (Boolean) -> Unit,
    ) {
        setLoading(true)

        val edition = book.currentEdition
        val finished = when {
            edition == null -> false
            newSeconds != null -> edition.audioSeconds?.let { newSeconds >= it } == true
            newPage != null -> newPage == edition.pages
            else -> false
        }

        if (finished) {
            markBookAsReadUseCase(book = book).onFailure {
                Timber.e("Something went wrong marking book as read! $it")
            }
        } else {
            updateBookProgressUseCase(
                book = book,
                newPage = newPage,
                newSeconds = newSeconds,
            ).onFailure {
                Timber.e("Something went wrong updating book progress! $it")
            }
        }

        setLoading(false)
    }
}
