package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/** Adds the suggested tag behind [chipKey]; a key the suggestions no longer hold is dropped. */
internal class OnTagSuggestionSelectedAction(
    private val chipKey: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val suggestion = scope.currentState.tagSuggestionChips[chipKey] ?: return

        OnAddUserTagAction(
            name = suggestion.name,
            category = suggestion.category,
        ).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }
}
