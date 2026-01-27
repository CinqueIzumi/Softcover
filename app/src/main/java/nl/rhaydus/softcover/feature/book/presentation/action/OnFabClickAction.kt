package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies

class OnFabClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent>,
    ) {
        scope.setState {
            copy(fabMenuExpanded = fabMenuExpanded.not())
        }
    }
}