package nl.rhaydus.softcover.feature.reading.presentation.util

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies
import timber.log.Timber
import javax.inject.Inject

class UpdateBookProgressUtil @Inject constructor() {
    operator fun invoke(
        book: BookWithProgress,
        newPage: Int,
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent>,
    ) {
        dependencies.launch {
            scope.setState { copy(isLoading = true) }

            if (newPage == book.currentEdition.pages) {
                dependencies.markBookAsReadUseCase(book = book).onFailure {
                    Timber.e("Something went wrong marking book as read! $it")
                }
            } else {
                dependencies.updateBookProgressUseCase(
                    book = book,
                    newPage = newPage,
                ).onFailure {
                    Timber.e("Something went wrong updating book progress! $it")
                }
            }

            scope.setState { copy(isLoading = false) }
        }

        scope.setState {
            copy(
                showProgressSheet = false,
                bookToUpdate = null,
            )
        }
    }
}