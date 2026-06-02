package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

/**
 * Loads the user's own tags once a book resolves *and* is on a shelf (it has a `userBook`) — tagging
 * is offered only for shelved books, so there is nothing to load otherwise. Subsequent edits update
 * state directly from the save response, so this only handles the initial load (and a reload should
 * the user shelve a different book within the same screen).
 */
class UserTagsCollector : BookDetailInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state -> state.book?.takeIf { it.userBook != null }?.id }
            .distinctUntilChanged()
            .collectLatest { shelvedBookId ->
                shelvedBookId ?: return@collectLatest

                dependencies.getUserTagsUseCase(bookId = shelvedBookId)
                    .onSuccess { tags -> scope.setState { it.copy(userTags = tags) } }
                    .onFailure { error -> Timber.e("$error") }
            }
    }
}
