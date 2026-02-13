package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.reading.presentation.enums.ProgressSheetTab

class OnProgressTabClickAction(
    private val tab: ProgressSheetTab,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState {
            it.copy(selectedProgressSheetTab = tab)
        }
    }
}