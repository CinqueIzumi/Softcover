package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnShowUpdateProgressSheetClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        // The active unit is kept current by LastUsedProgressUnitCollector, so opening the sheet just
        // reveals it — no one-shot preference read here (a suspending read on open could fail/stall).
        scope.setState { it.copy(showUpdateProgressSheet = true) }
    }
}
