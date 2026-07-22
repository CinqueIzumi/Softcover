package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Refreshes the cached tag vocabulary the first time the tag editor sheet opens on this screen
 * instance — a single background sync per visit is enough to keep suggestions fresh without
 * re-fetching on every open/close. [TagSuggestionsCollector] stays purely reactive to the cache;
 * this collector owns the one-shot side effect that keeps that cache warm.
 */
internal class TagVocabularySyncCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.showTagEditorSheet }
            .distinctUntilChanged()
            .collectLatest { isOpen ->
                if (isOpen.not() || scope.currentLocalVariables.tagVocabularySynced) return@collectLatest

                scope.setLocalVariables { it.copy(tagVocabularySynced = true) }

                dependencies.launch {
                    dependencies.syncUserTagVocabularyUseCase()
                }
            }
    }
}
