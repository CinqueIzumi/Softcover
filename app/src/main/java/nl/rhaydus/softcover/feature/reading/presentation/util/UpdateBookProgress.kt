package nl.rhaydus.softcover.feature.reading.presentation.util

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import timber.log.Timber

// TODO: This does not actually seem to automatically update within the details screen
class UpdateBookProgress(
    private val markBookAsReadUseCase: MarkBookAsReadUseCase,
    private val updateBookProgressUseCase: UpdateBookProgressUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int,
        setLoading: (Boolean) -> Unit,
    ) {
        setLoading(true)

        if (newPage == book.currentEdition.pages) {
            markBookAsReadUseCase(book = book).onFailure {
                Timber.e("Something went wrong marking book as read! $it")
            }
        } else {
            updateBookProgressUseCase(
                book = book,
                newPage = newPage,
            ).onFailure {
                Timber.e("Something went wrong updating book progress! $it")
            }
        }

        setLoading(false)
    }
}