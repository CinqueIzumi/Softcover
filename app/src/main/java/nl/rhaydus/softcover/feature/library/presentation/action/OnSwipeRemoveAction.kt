package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import timber.log.Timber

class OnSwipeRemoveAction(
    private val book: Book,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        dependencies.removeBookFromLibraryUseCase(book = book).onFailure {
            Timber.e("-=- $it")

            scope.setState { state ->
                state.copy(failedSwipeBookIds = state.failedSwipeBookIds + book.id)
            }
        }
    }
}
