package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies
import javax.inject.Inject

/*
 * Created by Bart Bos on 27/01/2026 at 20:15
 */

class OnShowUpdateProgressSheetClickAction @Inject constructor() : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent>,
    ) {
        scope.setState { copy(showUpdateProgressSheet = true) }
    }
}