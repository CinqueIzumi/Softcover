package nl.rhaydus.softcover.feature.reading.presentation.util

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookProgressUseCase

class UpdateBookProgress(
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int? = null,
        newSeconds: Int? = null,
        setLoading: (Boolean) -> Unit = {},
    ): Result<Unit> {
        setLoading(true)

        val edition = book.currentEdition
        val finished = when {
            edition == null -> false
            newSeconds != null -> edition.audioSeconds?.let { newSeconds >= it } == true
            newPage != null -> newPage == edition.pages
            else -> false
        }

        val result = if (finished) {
            markBookAsReadUseCase(book = book)
        } else {
            updateBookProgressUseCase(
                book = book,
                newPage = newPage,
                newSeconds = newSeconds,
            )
        }

        setLoading(false)

        return result
    }
}
