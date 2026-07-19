package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.computeTagSuggestions
import nl.rhaydus.toad.ActionScope

/**
 * Keeps [BookDetailUiState.tagSuggestions] in sync with the cached tag vocabulary and the editor's
 * current input/category/applied-tags, via the pure `computeTagSuggestions` derivation. The
 * vocabulary is a single user's tag history — small enough that the derivation runs inline on this
 * collector's coroutine rather than being offloaded to a dispatcher.
 */
internal class TagSuggestionsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val queryFlow = scope.state
            .map { state ->
                TagEditorQuery(
                    input = state.tagEditorInput,
                    category = state.tagEditorCategory,
                    applied = state.userTags,
                )
            }
            .distinctUntilChanged()

        combine(dependencies.observeUserTagVocabularyUseCase(), queryFlow) { vocabulary, query ->
            computeTagSuggestions(
                vocabulary = vocabulary,
                input = query.input,
                category = query.category,
                appliedTags = query.applied,
            )
        }.collectLatest { suggestions ->
            scope.setState { it.copy(tagSuggestions = suggestions) }
        }
    }
}
