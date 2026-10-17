package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.toad.ActionScope

/**
 * Loads the user's own tags once a book resolves *and* is on a shelf (it has a `userBook`) — tagging
 * is offered only for shelved books, so there is nothing to load otherwise. Subsequent edits update
 * state directly from the save response, so this only handles the initial load (and a reload should
 * the user shelve a different book within the same screen). A failed load resolves to
 * [UserTagsLoad.Failed], which unblocks sharing (the card omits tags) but keeps tag edits disabled.
 */
internal class UserTagsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state -> state.book?.takeIf { it.userBook != null }?.id }
            .distinctUntilChanged()
            .collectLatest { shelvedBookId ->
                shelvedBookId ?: return@collectLatest

                val load = dependencies.getUserTagsUseCase(bookId = shelvedBookId)
                    .onFailure { error -> AppLog.e("$error") }
                    .map { tags -> UserTagsLoad.Loaded(tags.toImmutableList()) }
                    .getOrDefault(UserTagsLoad.Failed)

                scope.setState { it.copy(userTags = load) }
            }
    }
}
